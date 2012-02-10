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
import static org.ops4j.pax.exam.OptionUtils.combine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.edb.EDBBatchEvent;
import org.openengsb.core.api.edb.EDBCommit;
import org.openengsb.core.api.edb.EDBConstants;
import org.openengsb.core.api.edb.EDBDeleteEvent;
import org.openengsb.core.api.edb.EDBEvent;
import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.edb.EDBInsertEvent;
import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.api.edb.EDBUpdateEvent;
import org.openengsb.core.api.edb.EngineeringDatabaseService;
import org.openengsb.core.api.ekb.PersistInterface;
import org.openengsb.core.api.ekb.QueryInterface;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.OpenEngSBModelId;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.itests.util.AbstractExamTestHelper;
import org.openengsb.labs.paxexam.karaf.options.KarafDistributionConfigurationFilePutOption;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class EDBIT extends AbstractExamTestHelper {

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
                "password", "")
        };
        return combine(baseConfiguration(), options);
    }

    @Before
    public void setup() throws Exception {
        edbService = getOsgiService(EngineeringDatabaseService.class);
        query = getOsgiService(QueryInterface.class);
        persist = getOsgiService(PersistInterface.class);
    }

    @Test
    public void testIfServiceIsFound_shouldWork() throws Exception {
        assertThat(edbService, notNullValue());
    }

    @Test
    public void testInsert_shouldWork() throws Exception {
        EDBCommit commit = edbService.createCommit("test", "test");
        EDBObject testObject = new EDBObject("testobject");
        testObject.put("testkey", "testvalue");
        commit.add(testObject);
        Long testtime = edbService.commit(commit);
        assertThat(testtime.intValue(), not(0));
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
        testObject.put("newtestkey", "newtestvalue");
        commit.add(testObject);

        edbService.commit(commit);

        EDBObject obj = edbService.getObject("newtestobject");
        assertThat(obj, notNullValue());
    }

    @Test
    public void testQueryForObject_shouldWork() throws Exception {
        EDBCommit commit = edbService.createCommit("test", "test");
        EDBObject testObject = new EDBObject("newtestobject");
        testObject.put("newtestkey", "newtestvalue");
        commit.add(testObject);

        edbService.commit(commit);

        List<EDBObject> objects = edbService.query("newtestkey", "newtestvalue");
        assertThat(objects, notNullValue());
        assertThat(objects.size(), not(0));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testFileSaving_shouldWork() throws Exception {
        File f = new File("testfile.txt");
        FileWriter fw = new FileWriter(f);
        fw.write("this is a test");
        fw.flush();
        fw.close();

        TestFileModel model = ModelUtils.createEmptyModelObject(TestFileModel.class);
        model.setTestId("testId");
        model.setFile(f);
        EDBInsertEvent event = ModelUtils.createEmptyModelObject(EDBInsertEvent.class);
        event.setModel(model);
        event.setConnectorId("testconnector");
        event.setDomainId("testdomain");
        event.setInstanceId("testinstance");

        edbService.processEDBInsertEvent(event);
        TestFileModel result = query.getModel(TestFileModel.class, "testdomain/testconnector/testId");

        File newFile = result.getFile();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(newFile)));
        String line = reader.readLine();
        reader.close();
        newFile.delete();

        assertThat(result.getTestId(), is(model.getTestId()));
        assertThat(result.getFile().getName(), is("testfile.txt"));
        assertThat(line, is("this is a test"));
    }

    @Test(expected = EDBException.class)
    public void testSendDoubleEDBCreateEvent_shouldThrowError() throws Exception {
        TestModel model = ModelUtils.createEmptyModelObject(TestModel.class);
        model.setEdbId("createevent/1");
        List<OpenEngSBModel> models = new ArrayList<OpenEngSBModel>();
        models.add(model);
        persist.commit(models, null, null, getTestConnectorId());
        persist.commit(models, null, null, getTestConnectorId());
    }

    @Test
    public void testSendEDBCreateEvent_shouldSaveModel() throws Exception {
        TestModel model = ModelUtils.createEmptyModelObject(TestModel.class);
        model.setName("blub");
        model.setEdbId("createevent/2");
        List<OpenEngSBModel> models = new ArrayList<OpenEngSBModel>();
        models.add(model);
        persist.commit(models, null, null, getTestConnectorId());

        EDBObject obj = edbService.getObject("testdomain/testconnector/createevent/2");

        String name = (String) obj.get("name");
        Integer version = Integer.parseInt((String) obj.get(EDBConstants.MODEL_VERSION));

        assertThat(name, is("blub"));
        assertThat(version, is(1));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSendEDBBatchEvent_shouldWork() throws Exception {
        TestModel model = ModelUtils.createEmptyModelObject(TestModel.class);
        model.setName("blub");
        model.setEdbId("batchevent/1");
        EDBInsertEvent event = ModelUtils.createEmptyModelObject(EDBInsertEvent.class);
        event.setModel(model);
        enrichEDBEvent(event);
        edbService.processEDBInsertEvent(event);

        EDBObject obj = edbService.getObject("testdomain/testconnector/batchevent/1");

        String name1 = (String) obj.get("name");
        Integer version1 = Integer.parseInt((String) obj.get(EDBConstants.MODEL_VERSION));

        model.setName("blab");
        EDBBatchEvent e = ModelUtils.createEmptyModelObject(EDBBatchEvent.class);
        enrichEDBEvent(e);
        e.getUpdates().add(model);
        TestModel model2 = ModelUtils.createEmptyModelObject(TestModel.class);
        model2.setName("blob");
        model2.setEdbId("batchevent/2");

        e.getInserts().add(model2);

        edbService.processEDBBatchEvent(e);

        obj = edbService.getObject("testdomain/testconnector/batchevent/1");

        String name2 = (String) obj.get("name");
        Integer version2 = Integer.parseInt((String) obj.get(EDBConstants.MODEL_VERSION));

        obj = edbService.getObject("testdomain/testconnector/batchevent/2");

        String name3 = (String) obj.get("name");
        Integer version3 = Integer.parseInt((String) obj.get(EDBConstants.MODEL_VERSION));

        assertThat(name1, is("blub"));
        assertThat(version1, is(1));
        assertThat(name2, is("blab"));
        assertThat(version2, is(2));
        assertThat(name3, is("blob"));
        assertThat(version3, is(1));
    }

    @Test(expected = EDBException.class)
    @SuppressWarnings("deprecation")
    public void testSendEDBDeleteEventWithNonExistingOid_shouldThrowError() throws Exception {
        TestModel model = ModelUtils.createEmptyModelObject(TestModel.class);
        model.setEdbId("deleteevent/1");
        EDBDeleteEvent event = ModelUtils.createEmptyModelObject(EDBDeleteEvent.class);
        event.setModel(model);
        edbService.processEDBDeleteEvent(event);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSendEDBUpdateEvent_shouldUpdateModel() throws Exception {
        TestModel model = ModelUtils.createEmptyModelObject(TestModel.class);
        model.setName("blub");
        model.setEdbId("updateevent/2");
        EDBInsertEvent event = ModelUtils.createEmptyModelObject(EDBInsertEvent.class);
        event.setModel(model);
        enrichEDBEvent(event);
        edbService.processEDBInsertEvent(event);

        EDBObject obj = edbService.getObject("testdomain/testconnector/updateevent/2");

        String name1 = (String) obj.get("name");
        Integer version1 = Integer.parseInt((String) obj.get(EDBConstants.MODEL_VERSION));

        model.setName("blab");

        EDBUpdateEvent update = ModelUtils.createEmptyModelObject(EDBUpdateEvent.class);
        update.setModel(model);
        enrichEDBEvent(update);
        edbService.processEDBUpdateEvent(update);

        obj = edbService.getObject("testdomain/testconnector/updateevent/2");

        String name2 = (String) obj.get("name");
        Integer version2 = Integer.parseInt((String) obj.get(EDBConstants.MODEL_VERSION));

        assertThat(name1, is("blub"));
        assertThat(version1, is(1));
        assertThat(name2, is("blab"));
        assertThat(version2, is(2));
    }

    @Test(expected = EDBException.class)
    @SuppressWarnings("deprecation")
    public void testSendEDBUpdateEvent_shouldResolveInNoConflict() throws Exception {
        TestModel model = ModelUtils.createEmptyModelObject(TestModel.class);
        model.setName("blub");
        model.setEdbId("updateevent/3");
        EDBInsertEvent event = ModelUtils.createEmptyModelObject(EDBInsertEvent.class);
        event.setModel(model);
        enrichEDBEvent(event);
        edbService.processEDBInsertEvent(event);

        EDBObject obj = edbService.getObject("testdomain/testconnector/updateevent/3");

        String name1 = (String) obj.get("name");
        Integer version1 = Integer.parseInt((String) obj.get(EDBConstants.MODEL_VERSION));

        model.addOpenEngSBModelEntry(new OpenEngSBModelEntry(EDBConstants.MODEL_VERSION, 0, Integer.class));

        EDBUpdateEvent update = ModelUtils.createEmptyModelObject(EDBUpdateEvent.class);
        update.setModel(model);
        enrichEDBEvent(update);
        edbService.processEDBUpdateEvent(update);

        // results in no conflict because the values are the same even if the version is different
        obj = edbService.getObject("testdomain/testconnector/updateevent/3");

        String name2 = (String) obj.get("name");
        Integer version2 = Integer.parseInt((String) obj.get(EDBConstants.MODEL_VERSION));

        assertThat(name1, is("blub"));
        assertThat(version1, is(1));
        assertThat(name2, is("blab"));
        assertThat(version2, is(2));
    }

    @Test(expected = EDBException.class)
    @SuppressWarnings("deprecation")
    public void testSendEDBUpdateEvent_shouldResolveInConflict() throws Exception {
        TestModel model = ModelUtils.createEmptyModelObject(TestModel.class);
        model.setName("blub");
        model.setEdbId("updateevent/4");
        EDBInsertEvent event = ModelUtils.createEmptyModelObject(EDBInsertEvent.class);
        event.setModel(model);
        enrichEDBEvent(event);
        edbService.processEDBInsertEvent(event);

        model.setName("blab");
        model.addOpenEngSBModelEntry(new OpenEngSBModelEntry(EDBConstants.MODEL_VERSION, 0, Integer.class));

        EDBUpdateEvent update = ModelUtils.createEmptyModelObject(EDBUpdateEvent.class);
        update.setModel(model);
        enrichEDBEvent(update);
        edbService.processEDBUpdateEvent(update);
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSupportOfSimpleSubModels_shouldWork() {
        TestModel model = ModelUtils.createEmptyModelObject(TestModel.class);
        model.setName("blub");
        model.setEdbId("testSub/1");
        SubModel sub = ModelUtils.createEmptyModelObject(SubModel.class);
        sub.setEdbId("testSub/2");
        sub.setName("sub");
        model.setSubModel(sub);

        EDBInsertEvent event = ModelUtils.createEmptyModelObject(EDBInsertEvent.class);
        event.setModel(model);
        enrichEDBEvent(event);
        edbService.processEDBInsertEvent(event);

        EDBObject mainObject = edbService.getObject("testdomain/testconnector/testSub/1");
        EDBObject subObject = edbService.getObject("testdomain/testconnector/testSub/2");

        assertThat(subObject, notNullValue());
        assertThat(mainObject.getString("subModel"), is("testdomain/testconnector/testSub/2"));
    }

    @Test
    @SuppressWarnings("deprecation")
    public void testSupportOfListOfSubModels_shouldWork() {
        TestModel model = ModelUtils.createEmptyModelObject(TestModel.class);
        model.setName("blub");
        model.setEdbId("testSub/3");

        SubModel sub1 = ModelUtils.createEmptyModelObject(SubModel.class);
        sub1.setEdbId("testSub/4");
        sub1.setName("sub1");
        SubModel sub2 = ModelUtils.createEmptyModelObject(SubModel.class);
        sub2.setEdbId("testSub/5");
        sub2.setName("sub2");

        model.setSubs(Arrays.asList(sub1, sub2));

        EDBInsertEvent event = ModelUtils.createEmptyModelObject(EDBInsertEvent.class);
        event.setModel(model);
        enrichEDBEvent(event);
        edbService.processEDBInsertEvent(event);

        EDBObject mainObject = edbService.getObject("testdomain/testconnector/testSub/3");
        EDBObject subObject1 = edbService.getObject("testdomain/testconnector/testSub/4");
        EDBObject subObject2 = edbService.getObject("testdomain/testconnector/testSub/5");

        assertThat(subObject1, notNullValue());
        assertThat(subObject2, notNullValue());
        assertThat(mainObject.getString("subs0"), is("testdomain/testconnector/testSub/4"));
        assertThat(mainObject.getString("subs1"), is("testdomain/testconnector/testSub/5"));
    }
    
    private ConnectorId getTestConnectorId() {
        return new ConnectorId("testdomain", "testconnector", "testinstance");
    }

    private void enrichEDBEvent(EDBEvent event) {
        event.setConnectorId("testconnector");
        event.setDomainId("testdomain");
        event.setInstanceId("testinstance");
    }

    public interface TestModel extends OpenEngSBModel {
        void setName(String name);

        String getName();

        @OpenEngSBModelId
        void setEdbId(String edbId);

        String getEdbId();

        void setSubModel(SubModel subModel);

        SubModel getSubModel();

        void setSubs(List<SubModel> subs);

        List<SubModel> getSubs();

        void setIds(List<Integer> ids);

        List<Integer> getIds();
    }

    public interface SubModel extends OpenEngSBModel {

        void setName(String name);

        String getName();

        void setEdbId(String edbId);

        String getEdbId();
    }

    public interface TestFileModel extends OpenEngSBModel {
        @OpenEngSBModelId
        void setTestId(String testId);

        String getTestId();

        void setFile(File file);

        File getFile();
    }
}
