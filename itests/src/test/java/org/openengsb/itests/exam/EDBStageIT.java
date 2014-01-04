/*
 * Copyright 2014 Svetoslav.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.itests.exam;

import org.apache.karaf.tooling.exam.options.KarafDistributionConfigurationFilePutOption;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EDBStage;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.core.ekb.api.QueryInterface;
import static org.openengsb.itests.util.AbstractExamTestHelper.baseConfiguration;
import org.openengsb.itests.util.AbstractModelUsingExamTestHelper;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import org.ops4j.pax.exam.Option;
import static org.ops4j.pax.exam.OptionUtils.combine;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class EDBStageIT extends AbstractModelUsingExamTestHelper {
    private final static String CONTEXT = "testcontext";
    private EngineeringDatabaseService edbService;
    private QueryInterface query;
    private PersistInterface persist;

    @Configuration
    public static Option[] myConfiguration() throws Exception {
        Option[] options = new Option[]{
            new KarafDistributionConfigurationFilePutOption(
                "etc/org.openengsb.ekb.cfg",
                "modelUpdatePropagationMode", "DEACTIVATED"),
            new KarafDistributionConfigurationFilePutOption(
                "etc/org.openengsb.ekb.cfg",
                "persistInterfaceLockingMode", "DEACTIVATED"),
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
        ContextHolder.get().setCurrentContextId(CONTEXT);
    }

    @Test
    public void testInsertStagedObject_shouldWork() throws Exception {
        EDBStage stage = edbService.createEDBStage("stage");
        
        EDBCommit commit = edbService.createEDBCommit(stage, null, null, null);
        EDBObject testObject = new EDBObject("testobject", stage);
        testObject.putEDBObjectEntry("testkey", "staged");
        commit.insert(testObject);
        edbService.commit(commit);
        
        EDBObject stagedObject = edbService.getObject("testobject", "stage");
        assertThat(stagedObject, notNullValue());
    }
    
    @Test(expected = EDBException.class)
    public void testTryToLoadStagedObjectWithoutStage_shouldThrowException() throws Exception {
        EDBStage stage = edbService.createEDBStage("stage1");
        
        EDBCommit commit = edbService.createEDBCommit(stage, null, null, null);
        EDBObject testObject = new EDBObject("testobject", stage);
        testObject.putEDBObjectEntry("testkey", "staged");
        commit.insert(testObject);
        edbService.commit(commit);
        
        EDBObject stagedObject = edbService.getObject("testobject");
    }
    
    @Test
    public void testInsertIntoMultipleStages_shouldWork() throws Exception {
        EDBStage stage1 = edbService.createEDBStage("stage1");
        EDBStage stage2 = edbService.createEDBStage("stage2");
        
        EDBCommit commit1 = edbService.createEDBCommit(stage1, null, null, null);
        EDBObject testObject1 = new EDBObject("testobject", stage1);
        testObject1.putEDBObjectEntry("testkey", "stage1");
        commit1.insert(testObject1);
        edbService.commit(commit1);
        
        EDBCommit commit2 = edbService.createEDBCommit(stage2, null, null, null);
        EDBObject testObject2 = new EDBObject("testobject", stage2);
        testObject2.putEDBObjectEntry("testkey", "stage2");
        commit2.insert(testObject2);
        edbService.commit(commit2);
        
        EDBObject stagedObject1 = edbService.getObject("testobject", "stage1");
        EDBObject stagedObject2 = edbService.getObject("testobject", "stage2");
        assertEquals("stage1", stagedObject1.getString("testkey"));
        assertEquals("stage2", stagedObject2.getString("testkey"));
    }
}
