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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.apache.karaf.tooling.exam.options.KarafDistributionConfigurationFilePutOption;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.model.OpenEngSBFileModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.EKBException;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.core.ekb.api.QueryInterface;
import org.openengsb.core.util.ModelUtils;
import org.openengsb.itests.util.AbstractModelUsingExamTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class EDBIT extends AbstractModelUsingExamTestHelper {
    private EngineeringDatabaseService edbService;
    private QueryInterface query;
    private PersistInterface persist;

    @Configuration
    public static Option[] myConfiguration() throws Exception {
        Option[] options = new Option[]{
            new KarafDistributionConfigurationFilePutOption(
                "/etc/org.openengsb.infrastructure.jpa",
                "url", "jdbc:h2:mem:itests"),
            new KarafDistributionConfigurationFilePutOption(
                "/etc/org.openengsb.infrastructure.jpa",
                "driverClassName", "org.h2.jdbcx.JdbcDataSource"),
            new KarafDistributionConfigurationFilePutOption(
                "/etc/org.openengsb.infrastructure.jpa",
                "username", ""),
            new KarafDistributionConfigurationFilePutOption(
                "/etc/org.openengsb.infrastructure.jpa",
                "password", ""),
            mavenBundle().groupId("org.ops4j.pax.tinybundles").artifactId("tinybundles").versionAsInProject()
        };
        return combine(baseConfiguration(), options);
    }

    @Before
    public void setup() throws Exception {
        edbService = getOsgiService(EngineeringDatabaseService.class);
        query = getOsgiService(QueryInterface.class);
        persist = getOsgiService(PersistInterface.class);
        registerModelProvider();
    }

    @Test
    public void testIfServiceIsFound_shouldWork() throws Exception {
        assertThat(edbService, notNullValue());
    }

    @Test
    public void testInsert_shouldWork() throws Exception {
        EDBCommit commit = edbService.createCommit("test", "test");
        EDBObject testObject = new EDBObject("testobject");
        testObject.putEDBObjectEntry("testkey", "testvalue");
        commit.insert(testObject);
        Long testtime = edbService.commit(commit);
        assertThat(testtime.longValue(), not(0L));
    }

    @Test(expected = EDBException.class)
    public void testDoubleCommit_shouldThrowException() throws Exception {
        EDBCommit commit = edbService.createCommit("test", "test");
        edbService.commit(commit);
        edbService.commit(commit);
    }

    @Test
    public void testRetrieveObject_shouldWork() throws Exception {
        EDBCommit commit = edbService.createCommit("test", "test");
        EDBObject testObject = new EDBObject("newtestobject");
        testObject.putEDBObjectEntry("newtestkey", "newtestvalue");
        commit.insert(testObject);

        edbService.commit(commit);

        EDBObject obj = edbService.getObject("newtestobject");
        assertThat(obj, notNullValue());
    }

    @Test
    public void testQueryForObject_shouldWork() throws Exception {
        EDBCommit commit = edbService.createCommit("test", "test");
        EDBObject testObject = new EDBObject("newtestobject1");
        testObject.putEDBObjectEntry("newtestkey1", "newtestvalue1");
        commit.insert(testObject);

        edbService.commit(commit);

        List<EDBObject> objects = edbService.queryByKeyValue("newtestkey1", "newtestvalue1");
        assertThat(objects, notNullValue());
        assertThat(objects.size(), not(0));
    }

    @Test(expected = EDBException.class)
    public void testConflictDetection_shouldThrowException() throws Exception {
        EDBCommit commit = edbService.createCommit("test", "test");
        EDBObject testObject = new EDBObject("newtestobject2");
        testObject.putEDBObjectEntry("newtestkey2", "newtestvalue2");
        commit.insert(testObject);

        edbService.commit(commit);

        commit = edbService.createCommit("test", "test");

        EDBObject obj = edbService.getObject("newtestobject2");
        obj.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(0));
        obj.putEDBObjectEntry("test", "test");

        commit.update(obj);
        edbService.commit(commit);
    }

    @Test
    public void testFileSaving_shouldWork() throws Exception {
        File f = new File("testfile.txt");
        FileWriter fw = new FileWriter(f);
        fw.write("this is a test");
        fw.flush();
        fw.close();

        OpenEngSBFileModel model = new OpenEngSBFileModel();
        model.setFile(f);
        model.setId("testId");

        EKBCommit commit = getTestEKBCommit().addInsert(model);
        persist.commit(commit);

        OpenEngSBFileModel result = query.getModel(OpenEngSBFileModel.class, "testdomain/testconnector/testId");

        File newFile = result.getFile();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(newFile)));
        String line = reader.readLine();
        reader.close();
        newFile.delete();

        assertThat(result.getFile().getName(), is("testfile.txt"));
        assertThat(line, is("this is a test"));
    }

    @Test(expected = EKBException.class)
    public void testDoubleModelCommit_shouldThrowException() throws Exception {
        Object model = getTestModel().newInstance();
        setProperty(model, "setEdbId", "createevent/1");
        EKBCommit commit = getTestEKBCommit().addInsert(model);

        persist.commit(commit);
        persist.commit(commit);
    }

    @Test
    public void testEKBInsertCommit_shouldSaveModel() throws Exception {
        Object model = getTestModel().newInstance();
        setProperty(model, "setName", "test");
        setProperty(model, "setEdbId", "createevent/2");
        EKBCommit commit = getTestEKBCommit().addInsert(model);
        persist.commit(commit);

        EDBObject obj = edbService.getObject("testdomain/testconnector/createevent/2");

        String name = obj.getString("name");
        Integer version = obj.getObject(EDBConstants.MODEL_VERSION, Integer.class);

        assertThat(name, is("test"));
        assertThat(version, is(1));
    }

    @Test
    public void testEKBInsertCommitAndQueryData_shouldReturnModelObject() throws Exception {
        Object model = getTestModel().newInstance();
        setProperty(model, "setName", "C:\\test");
        setProperty(model, "setEdbId", "createevent/5");
        EKBCommit commit = getTestEKBCommit().addInsert(model);
        persist.commit(commit);
        
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) query.queryForModels(getTestModel(), "name:\"C:\\test\"");
        assertThat(result.isEmpty(), is(false));
        assertThat(result.get(0), is(getTestModel()));
    }
    
    @Test
    public void testEKBInsertCommitAndQueryDataWithBackslashes_shouldReturnModelObject() throws Exception {
        Object model = getTestModel().newInstance();
        setProperty(model, "setName", "C:\\\\test");
        setProperty(model, "setEdbId", "createevent/6");
        EKBCommit commit = getTestEKBCommit().addInsert(model);
        persist.commit(commit);
        
        @SuppressWarnings("unchecked")
        List<Object> result = (List<Object>) query.queryForModels(getTestModel(), "name:\"C:\\\\test\"");
        assertThat(result.isEmpty(), is(false));
        assertThat(result.get(0), is(getTestModel()));
    }

    @Test
    public void testEKBUpdateCommit_shouldWork() throws Exception {
        Object model = getTestModel().newInstance();
        setProperty(model, "setName", "test1");
        setProperty(model, "setEdbId", "batchevent/1");
        EKBCommit commit = getTestEKBCommit().addInsert(model);
        persist.commit(commit);

        EDBObject obj = edbService.getObject("testdomain/testconnector/batchevent/1");

        String name1 = obj.getString("name");
        Integer version1 = obj.getObject(EDBConstants.MODEL_VERSION, Integer.class);

        setProperty(model, "setName", "test2");
        commit = getTestEKBCommit().addUpdate(model);

        Object model2 = getTestModel().newInstance();
        setProperty(model2, "setName", "test3");
        setProperty(model2, "setEdbId", "batchevent/2");
        commit.addInsert(model2);
        persist.commit(commit);

        obj = edbService.getObject("testdomain/testconnector/batchevent/1");

        String name2 = obj.getString("name");
        Integer version2 = obj.getObject(EDBConstants.MODEL_VERSION, Integer.class);

        obj = edbService.getObject("testdomain/testconnector/batchevent/2");

        String name3 = obj.getString("name");
        Integer version3 = obj.getObject(EDBConstants.MODEL_VERSION, Integer.class);

        assertThat(name1, is("test1"));
        assertThat(version1, is(1));
        assertThat(name2, is("test2"));
        assertThat(version2, is(2));
        assertThat(name3, is("test3"));
        assertThat(version3, is(1));
    }

    @Test(expected = EKBException.class)
    public void testEKBDeleteCommitWithNonExistingOid_shouldThrowError() throws Exception {
        Object model = getTestModel().newInstance();
        setProperty(model, "setEdbId", "deleteevent/1");
        EKBCommit commit = getTestEKBCommit().addDelete(model);
        persist.commit(commit);
    }

    @Test
    public void testEKBUpdateCommit_shouldUpdateModel() throws Exception {
        Object model = getTestModel().newInstance();
        setProperty(model, "setName", "test1");
        setProperty(model, "setEdbId", "updateevent/2");
        EKBCommit commit = getTestEKBCommit().addInsert(model);
        persist.commit(commit);

        EDBObject obj = edbService.getObject("testdomain/testconnector/updateevent/2");

        String name1 = obj.getString("name");
        Integer version1 = obj.getObject(EDBConstants.MODEL_VERSION, Integer.class);

        setProperty(model, "setName", "test2");

        commit = getTestEKBCommit().addUpdate(model);
        persist.commit(commit);

        obj = edbService.getObject("testdomain/testconnector/updateevent/2");

        String name2 = obj.getString("name");
        Integer version2 = obj.getObject(EDBConstants.MODEL_VERSION, Integer.class);

        assertThat(name1, is("test1"));
        assertThat(version1, is(1));
        assertThat(name2, is("test2"));
        assertThat(version2, is(2));
    }

    @Test
    public void testEKBConflictCommitEvent_shouldResolveInNoConflict() throws Exception {
        Object model = getTestModel().newInstance();
        setProperty(model, "setName", "test");
        setProperty(model, "setEdbId", "updateevent/3");
        EKBCommit commit = getTestEKBCommit().addInsert(model);
        persist.commit(commit);

        EDBObject obj = edbService.getObject("testdomain/testconnector/updateevent/3");
        Integer version1 = obj.getObject(EDBConstants.MODEL_VERSION, Integer.class);
        OpenEngSBModelEntry entry = new OpenEngSBModelEntry(EDBConstants.MODEL_VERSION, 0, Integer.class);
        ModelUtils.addOpenEngSBModelEntry(model, entry);
        commit = getTestEKBCommit().addUpdate(model);
        persist.commit(commit);

        // results in no conflict because the values are the same even if the version is different
        obj = edbService.getObject("testdomain/testconnector/updateevent/3");
        Integer version2 = obj.getObject(EDBConstants.MODEL_VERSION, Integer.class);

        assertThat(version1, is(1));
        assertThat(version2, is(2));
    }

    @Test(expected = EKBException.class)
    public void testEKBConflictCommitEvent_shouldResolveInConflict() throws Exception {
        Object model = getTestModel().newInstance();
        setProperty(model, "setName", "test1");
        setProperty(model, "setEdbId", "updateevent/4");
        EKBCommit commit = getTestEKBCommit().addInsert(model);
        persist.commit(commit);

        setProperty(model, "setName", "test2");
        OpenEngSBModelEntry entry = new OpenEngSBModelEntry(EDBConstants.MODEL_VERSION, 0, Integer.class);
        ModelUtils.addOpenEngSBModelEntry(model, entry);

        commit = getTestEKBCommit().addUpdate(model);
        persist.commit(commit);
    }

    @Test
    public void testSupportOfSimpleSubModels_shouldWork() throws Exception {
        Object model = getTestModel().newInstance();
        setProperty(model, "setName", "test");
        setProperty(model, "setEdbId", "testSub/1");

        Object sub = getSubModel().newInstance();
        setProperty(sub, "setName", "sub");
        setProperty(sub, "setEdbId", "testSub/2");

        setProperty(model, "setSubModel", sub);

        EKBCommit commit = getTestEKBCommit().addInsert(model);
        persist.commit(commit);

        EDBObject mainObject = edbService.getObject("testdomain/testconnector/testSub/1");
        EDBObject subObject = edbService.getObject("testdomain/testconnector/testSub/2");

        assertThat(subObject, notNullValue());
        assertThat(mainObject.getString("subModel"), is("testdomain/testconnector/testSub/2"));
    }

    @Test
    public void testSupportOfListOfSubModels_shouldWork() throws Exception {
        Object model = getTestModel().newInstance();
        setProperty(model, "setName", "test");
        setProperty(model, "setEdbId", "testSub/3");

        Object sub1 = getSubModel().newInstance();
        setProperty(sub1, "setName", "sub1");
        setProperty(sub1, "setEdbId", "testSub/4");
        Object sub2 = getSubModel().newInstance();
        setProperty(sub2, "setName", "sub2");
        setProperty(sub2, "setEdbId", "testSub/5");

        List<?> subs = Arrays.asList(sub1, sub2);

        setProperty(model, "setSubs", subs);

        EKBCommit commit = getTestEKBCommit().addInsert(model);
        persist.commit(commit);

        EDBObject mainObject = edbService.getObject("testdomain/testconnector/testSub/3");
        EDBObject subObject1 = edbService.getObject("testdomain/testconnector/testSub/4");
        EDBObject subObject2 = edbService.getObject("testdomain/testconnector/testSub/5");

        assertThat(subObject1, notNullValue());
        assertThat(subObject2, notNullValue());
        assertThat(mainObject.getString("subs.0"), is("testdomain/testconnector/testSub/4"));
        assertThat(mainObject.getString("subs.1"), is("testdomain/testconnector/testSub/5"));
    }

    @Test
    public void testModelTailIsLoaded_shouldLoadModelTail() throws Exception {
        Object model = getTestModel().newInstance();
        setProperty(model, "setName", "blub");
        setProperty(model, "setEdbId", "modeltailtest/1");

        EKBCommit commit = getTestEKBCommit().addInsert(model);
        persist.commit(commit);

        Object result = (Object) query.getModel(getTestModel(), "testdomain/testconnector/modeltailtest/1");
        Boolean versionPresent = false;
        for (OpenEngSBModelEntry entry : ModelUtils.getOpenEngSBModelTail(result)) {
            if (entry.getKey().equals(EDBConstants.MODEL_VERSION)) {
                versionPresent = true;
            }
        }
        assertThat(versionPresent, is(true));
    }
    
    @Test
    public void testIfModelMetaDataRetrievingWorks_shouldWork() throws Exception {
        Object model = getTestModel().newInstance();
        setProperty(model, "setName", "blub");
        setProperty(model, "setEdbId", "modelmetatest/1");

        EKBCommit commit = getTestEKBCommit().addInsert(model);
        persist.commit(commit);
        Object result = (Object) query.getModel(getTestModel(), "testdomain/testconnector/modelmetatest/1");
        assertThat(ModelUtils.getOpenEngSBModelEntries(result), notNullValue());
        assertThat(ModelUtils.getInternalModelId(result), notNullValue());
        assertThat(ModelUtils.retrieveInternalModelVersion(result), notNullValue());
        assertThat(ModelUtils.retrieveInternalModelTimestamp(result), notNullValue());
    }

    private EKBCommit getTestEKBCommit() {
        EKBCommit commit = new EKBCommit().setDomainId("testdomain").setConnectorId("testconnector");
        commit.setInstanceId("testinstance");
        return commit;
    }
}
