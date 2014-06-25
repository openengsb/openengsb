/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.itests.exam;

import static org.apache.karaf.tooling.exam.options.KarafDistributionOption.editConfigurationFileExtend;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.karaf.tooling.exam.options.KarafDistributionConfigurationFilePutOption;
import org.apache.karaf.tooling.exam.options.configs.FeaturesCfg;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.edbi.api.Index;
import org.openengsb.core.edbi.api.IndexEngine;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.domain.example.model.EOModel;
import org.openengsb.itests.util.AbstractModelUsingExamTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;
import org.springframework.jdbc.core.JdbcTemplate;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class EDBIndexIT extends AbstractModelUsingExamTestHelper {

    public static final String CONTEXT = "testcontext";

    private PersistInterface ekb;
    private IndexEngine indexEngine;

    private DataSource dataSource;

    @Configuration
    public static Option[] myConfiguration() throws Exception {
        Option[] options = new Option[]{
            configFilePut("etc/org.openengsb.ekb.cfg", "modelUpdatePropagationMode", "DEACTIVATED"),
            configFilePut("etc/org.openengsb.ekb.cfg", "persistInterfaceLockingMode", "DEACTIVATED"),
            mavenBundle().groupId("org.ops4j.pax.tinybundles").artifactId("tinybundles").versionAsInProject(),
            feature("openengsb-domain-example"),
            feature("openengsb-edbi") // also loads spring-jdbc
        };
        return combine(baseConfiguration(), options);
    }

    @Before
    public void setup() throws Exception {
        ekb = getOsgiService(PersistInterface.class);
        indexEngine = getOsgiService(IndexEngine.class);
        dataSource = getOsgiService(DataSource.class);
        registerModelProvider();
        ContextHolder.get().setCurrentContextId(CONTEXT);
    }

    @Test
    public void getEngineService_works() throws Exception {
        assertNotNull(indexEngine);
    }

    @Test
    public void ekbCommit_shouldCreateIndexInherently() throws Exception {
        EOModel insert = new EOModel();
        insert.setEdbId("insert/1");

        EKBCommit ekbCommit = getTestEKBCommit();
        ekbCommit.addInsert(insert);

        ekb.commit(ekbCommit);

        Index<?> index = indexEngine.getIndex(insert.getClass());

        assertNotNull(index);
        assertEquals("org.openengsb.domain.example.model.EOModel", index.getName());
        assertEquals(insert.getClass(), index.getModelClass());

        assertNotNull(index.getHeadTableName());
        assertNotNull(index.getHistoryTableName());

        assertEquals(6, index.getFields().size());
    }

    @Test
    public void createdIndexHeadTableShouldHaveCorrectPrimaryKey() throws Exception {
        EOModel insert = new EOModel();
        insert.setEdbId("insert/1");
        ekb.commit(getTestEKBCommit().addInsert(insert));

        String sql = "SELECT * FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_NAME = ?";

        Map<String, Object> record =
            new JdbcTemplate(dataSource).queryForMap(sql, indexEngine.getIndex(EOModel.class).getHeadTableName());

        assertEquals("EDBID", record.get("COLUMN_NAME"));
        assertTrue("Column EDBID is not a primary key", record.get("INDEX_NAME").toString().startsWith("PRIMARY_KEY"));
    }

    @Test
    public void ekbCommit_updateAndDelete_shouldCreateHistoryRecordsCorreclty() throws Exception {
        // model 1 stays the same
        // model 2 gets updated
        // model 3 gets deleted
        EOModel model1 = new EOModel();
        EOModel model2 = new EOModel();
        EOModel model3 = new EOModel();

        model1.setEdbId("eom/1");
        model2.setEdbId("eom/2");
        model3.setEdbId("eom/3");

        model1.setShared("A");
        model2.setShared("B");
        model3.setShared("C");

        EKBCommit insertCommit =
            getTestEKBCommit().addInsert(model1).addInsert(model2).addInsert(model3);
        ekb.commit(insertCommit);

        model2.setShared("B_EDITED");

        EKBCommit updateCommit = getTestEKBCommit().addUpdate(model2).addDelete(model3);
        ekb.commit(updateCommit);

        // assert
        Index<?> index = indexEngine.getIndex(model1.getClass());

        assertEquals(5,
            new JdbcTemplate(dataSource).queryForLong("SELECT COUNT(*) FROM " + index.getHistoryTableName()));

        String sql = "SELECT * FROM " + index.getHistoryTableName() + " ORDER BY REV_OPERATION, EDBID";
        List<Map<String, Object>> records = new JdbcTemplate(dataSource).queryForList(sql);

        Iterator<Map<String, Object>> iterator = records.iterator();
        Map<String, Object> record;

        record = iterator.next();
        assertEquals("eom/3", record.get("EDBID"));
        assertEquals("C", record.get("SHARED"));
        assertEquals("DELETE", record.get("REV_OPERATION"));

        record = iterator.next();
        assertEquals("eom/1", record.get("EDBID"));
        assertEquals("A", record.get("SHARED"));
        assertEquals("INSERT", record.get("REV_OPERATION"));

        record = iterator.next();
        assertEquals("eom/2", record.get("EDBID"));
        assertEquals("B", record.get("SHARED"));
        assertEquals("INSERT", record.get("REV_OPERATION"));

        record = iterator.next();
        assertEquals("eom/3", record.get("EDBID"));
        assertEquals("C", record.get("SHARED"));
        assertEquals("INSERT", record.get("REV_OPERATION"));

        record = iterator.next();
        assertEquals("eom/2", record.get("EDBID"));
        assertEquals("B_EDITED", record.get("SHARED"));
        assertEquals("UPDATE", record.get("REV_OPERATION"));

        assertFalse(iterator.hasNext());
    }

    private static Option configFilePut(String configurationFilePath, String key, String value) {
        return new KarafDistributionConfigurationFilePutOption(configurationFilePath, key, value);
    }

    private static Option feature(String feature) {
        return editConfigurationFileExtend(FeaturesCfg.BOOT, "," + feature);
    }

}
