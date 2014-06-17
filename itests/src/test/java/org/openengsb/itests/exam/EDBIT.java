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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.karaf.tooling.exam.options.KarafDistributionConfigurationFilePutOption;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.ModelWrapper;
import org.openengsb.core.api.model.OpenEngSBFileModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.QueryRequest;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EDBQueryFilter;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.EKBConcurrentException;
import org.openengsb.core.ekb.api.EKBException;
import org.openengsb.core.ekb.api.EKBService;
import org.openengsb.core.ekb.api.SingleModelQuery;
import org.openengsb.itests.exam.models.PrimitivePropertyModelDecorator;
import org.openengsb.itests.exam.models.SubModelDecorator;
import org.openengsb.itests.exam.models.TestModelDecorator;
import org.openengsb.itests.util.AbstractModelUsingExamTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class EDBIT extends AbstractModelUsingExamTestHelper {
    private static final String CONTEXT = "testcontext";
    private EngineeringDatabaseService edbService;
    private EKBService ekbService;

    @Configuration
    public static Option[] myConfiguration() throws Exception {
        Option[] options = new Option[] {
                new KarafDistributionConfigurationFilePutOption("etc/org.openengsb.ekb.cfg",
                        "modelUpdatePropagationMode", "DEACTIVATED"),
                new KarafDistributionConfigurationFilePutOption("etc/org.openengsb.ekb.cfg",
                        "persistInterfaceLockingMode", "DEACTIVATED"),
                mavenBundle().groupId("org.ops4j.pax.tinybundles").artifactId("tinybundles").versionAsInProject() };
        return combine(baseConfiguration(), options);
    }

    @Before
    public void setup() throws Exception {
        edbService = getOsgiService(EngineeringDatabaseService.class);
        ekbService = getOsgiService(EKBService.class);
        registerModelProvider();
        ContextHolder.get().setCurrentContextId(CONTEXT);
    }

    @Test
    public void testIfServiceIsFound_shouldWork() throws Exception {
        assertThat(edbService, notNullValue());
    }

    @Test
    public void testInsert_shouldWork() throws Exception {
        EDBCommit commit = edbService.createEDBCommit(null, null, null);
        EDBObject testObject = new EDBObject("testobject");
        testObject.putEDBObjectEntry("testkey", "testvalue");
        commit.insert(testObject);
        Long testtime = edbService.commit(commit);
        assertThat(testtime.longValue(), not(0L));
    }

    @Test(expected = EDBException.class)
    public void testDoubleCommit_shouldThrowException() throws Exception {
        EDBCommit commit = edbService.createEDBCommit(null, null, null);
        edbService.commit(commit);
        edbService.commit(commit);
    }

    @Test
    public void testRetrieveObject_shouldWork() throws Exception {
        EDBCommit commit = edbService.createEDBCommit(null, null, null);
        EDBObject testObject = new EDBObject("newtestobject");
        testObject.putEDBObjectEntry("newtestkey", "newtestvalue");
        commit.insert(testObject);

        edbService.commit(commit);

        EDBObject obj = edbService.getObject("newtestobject");
        assertThat(obj, notNullValue());
    }

    @Test
    public void testQueryForObject_shouldWork() throws Exception {
        EDBCommit commit = edbService.createEDBCommit(null, null, null);
        EDBObject testObject = new EDBObject("newtestobject1");
        testObject.putEDBObjectEntry("newtestkey1", "newtestvalue1");
        commit.insert(testObject);

        edbService.commit(commit);

        List<EDBObject> objects = edbService.query(QueryRequest.query("newtestkey1", "newtestvalue1"));
        assertThat(objects, notNullValue());
        assertThat(objects.size(), not(0));
    }

    @Test
    public void testOrQueryForModelType_shouldWork() throws Exception {
        TestModelDecorator model1 = getTestModelDecorator();
        model1.setEdbId("orQueryTest/1");
        model1.setName("model1");
        TestModelDecorator model2 = getTestModelDecorator();
        model2.setEdbId("orQueryTest/2");
        model2.setName("model2");
        TestModelDecorator model3 = getTestModelDecorator();
        model3.setEdbId("orQueryTest/3");
        model3.setName("model3");
        model3.setField("test");
        TestModelDecorator model4 = getTestModelDecorator();
        model4.setEdbId("orQueryTest/4");
        model4.setName("model4");
        EKBCommit commit = getTestEKBCommit().addInserts(
                Arrays.asList(model1.getModel(), model2.getModel(), model3.getModel(), model4.getModel()));
        ekbService.commit(commit);

        QueryRequest request = QueryRequest.create().orJoined().addParameter("name", "model1")
                .addParameter("name", "model2").addParameter("field", "test");
        List<Object> result = ekbService.query(new SingleModelQuery(getTestModel(), new EDBQueryFilter(request), null));
        assertThat(result, notNullValue());
        assertThat(result.size(), is(3));
    }

    @Test(expected = EDBException.class)
    public void testConflictDetection_shouldThrowException() throws Exception {
        EDBCommit commit = edbService.createEDBCommit(null, null, null);
        EDBObject testObject = new EDBObject("newtestobject2");
        testObject.putEDBObjectEntry("newtestkey2", "newtestvalue2");
        commit.insert(testObject);

        edbService.commit(commit);

        commit = edbService.createEDBCommit(null, null, null);

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
        ekbService.commit(commit);

        OpenEngSBFileModel result = ekbService.getModel(OpenEngSBFileModel.class, getModelOid("testId"));

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
        TestModelDecorator model = getTestModelDecorator();
        model.setEdbId("createevent/1");
        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());

        ekbService.commit(commit);
        ekbService.commit(commit);
    }

    @Test
    public void testEKBInsertCommit_shouldSaveModel() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setName("test");
        model.setEdbId("createevent/2");
        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);

        EDBObject obj = edbService.getObject(getModelOid("createevent/2"));
        String name = obj.getString("name");
        Integer version = obj.getObject(EDBConstants.MODEL_VERSION, Integer.class);

        assertThat(name, is("test"));
        assertThat(version, is(1));
    }

    @Test
    public void testEKBInsertCommitAndQueryData_shouldReturnModelObject() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setName("C:\\test");
        model.setEdbId("createevent/5");
        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);

        List<Object> result = ekbService.query(new SingleModelQuery(getTestModel(), new EDBQueryFilter(
                "name:\"C:\\test\""), null));
        assertThat(result.isEmpty(), is(false));
        assertThat(result.get(0), is(getTestModel()));
    }

    @Test
    public void testEKBInsertCommitAndQueryDataWithBackslashes_shouldReturnModelObject() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setName("C:\\\\test");
        model.setEdbId("createevent/6");
        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);

        QueryRequest request = QueryRequest.query("name", "C:\\\\test").wildcardUnaware();
        List<Object> result = ekbService.query(new SingleModelQuery(getTestModel(), new EDBQueryFilter(request), null));
        assertThat(result.isEmpty(), is(false));
        assertThat(result.get(0), is(getTestModel()));
    }

    @Test
    public void testEKBInsertCommitAndQueryWithContextId_shouldReturnModelOnlyOnCorrectContext() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setName("testmodel");
        model.setEdbId("createevent/7");
        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);
        ContextHolder.get().setCurrentContextId("othercontext");
        TestModelDecorator model2 = getTestModelDecorator();
        model2.setName("testmodel");
        model2.setEdbId("createevent/8");
        commit = getTestEKBCommit().addInsert(model2.getModel());
        ekbService.commit(commit);

        QueryRequest request = QueryRequest.query("name", "testmodel");
        List<Object> result = ekbService.query(new SingleModelQuery(getTestModel(), new EDBQueryFilter(request), null));
        assertThat(result.size(), is(2));
        request.setContextId(CONTEXT);
        result = ekbService.query(new SingleModelQuery(getTestModel(), new EDBQueryFilter(request), null));
        assertThat(result.size(), is(1));
        assertThat(new TestModelDecorator(result.get(0)).getEdbId(), is("createevent/7"));
        request.setContextId("othercontext");
        result = ekbService.query(new SingleModelQuery(getTestModel(), new EDBQueryFilter(request), null));
        assertThat(result.size(), is(1));
        assertThat(new TestModelDecorator(result.get(0)).getEdbId(), is("createevent/8"));
        request.setContextId("somenonesensecontext");
        result = ekbService.query(new SingleModelQuery(getTestModel(), new EDBQueryFilter(request), null));
        assertThat(result.size(), is(0));
        ContextHolder.get().setCurrentContextId(CONTEXT);
    }

    @Test
    public void testEKBUpdateCommit_shouldWork() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setName("test1");
        model.setEdbId("batchevent/1");
        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);

        EDBObject obj = edbService.getObject(getModelOid("batchevent/1"));
        String name1 = obj.getString("name");
        Integer version1 = obj.getObject(EDBConstants.MODEL_VERSION, Integer.class);

        model.setName("test2");
        commit = getTestEKBCommit().addUpdate(model.getModel());
        TestModelDecorator model2 = getTestModelDecorator();
        model2.setName("test3");
        model2.setEdbId("batchevent/2");
        commit.addInsert(model2.getModel());
        ekbService.commit(commit);

        obj = edbService.getObject(getModelOid("batchevent/1"));
        String name2 = obj.getString("name");
        Integer version2 = obj.getObject(EDBConstants.MODEL_VERSION, Integer.class);
        obj = edbService.getObject(getModelOid("batchevent/2"));
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
        TestModelDecorator model = getTestModelDecorator();
        model.setEdbId("deleteevent/1");
        EKBCommit commit = getTestEKBCommit().addDelete(model.getModel());
        ekbService.commit(commit);
    }

    @Test
    public void testEKBUpdateCommit_shouldUpdateModel() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setName("test1");
        model.setEdbId("updateevent/2");
        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);

        EDBObject obj = edbService.getObject(getModelOid("updateevent/2"));
        String name1 = obj.getString("name");
        Integer version1 = obj.getObject(EDBConstants.MODEL_VERSION, Integer.class);
        model.setName("test2");
        commit = getTestEKBCommit().addUpdate(model.getModel());
        ekbService.commit(commit);

        obj = edbService.getObject(getModelOid("updateevent/2"));
        String name2 = obj.getString("name");
        Integer version2 = obj.getObject(EDBConstants.MODEL_VERSION, Integer.class);

        assertThat(name1, is("test1"));
        assertThat(version1, is(1));
        assertThat(name2, is("test2"));
        assertThat(version2, is(2));
    }

    @Test
    public void testIfLoadingOfCommitsWork_shouldWork() throws Exception {
        EDBCommit commit = edbService.createEDBCommit(null, null, null);
        EDBObject testObject = new EDBObject("commitload/1");
        testObject.putEDBObjectEntry("testkey", "testvalue");
        commit.insert(testObject);
        Long testtime = edbService.commit(commit);
        EDBCommit result = edbService.getCommit(testtime);
        assertThat(result.getInserts().size(), is(1));
    }

    @Test
    public void testEKBConflictCommitEvent_shouldResolveInNoConflict() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setName("test");
        model.setEdbId("updateevent/3");
        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);

        EDBObject obj = edbService.getObject(getModelOid("updateevent/3"));
        Integer version1 = obj.getObject(EDBConstants.MODEL_VERSION, Integer.class);
        OpenEngSBModelEntry entry = new OpenEngSBModelEntry(EDBConstants.MODEL_VERSION, 0, Integer.class);
        ModelWrapper.wrap(model.getModel()).addOpenEngSBModelEntry(entry);
        commit = getTestEKBCommit().addUpdate(model.getModel());
        ekbService.commit(commit);

        // results in no conflict because the values are the same even if the
        // version is different
        obj = edbService.getObject(getModelOid("updateevent/3"));
        Integer version2 = obj.getObject(EDBConstants.MODEL_VERSION, Integer.class);

        assertThat(version1, is(1));
        assertThat(version2, is(2));
    }

    @Test(expected = EKBException.class)
    public void testEKBConflictCommitEvent_shouldResolveInConflict() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setName("test1");
        model.setEdbId("updateevent/4");
        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);

        model.setName("test2");
        OpenEngSBModelEntry entry = new OpenEngSBModelEntry(EDBConstants.MODEL_VERSION, 0, Integer.class);
        ModelWrapper.wrap(model.getModel()).addOpenEngSBModelEntry(entry);
        commit = getTestEKBCommit().addUpdate(model.getModel());
        ekbService.commit(commit);
    }

    @Test
    public void testSupportOfSimpleSubModels_shouldWork() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setName("test");
        model.setEdbId("testSub/1");

        SubModelDecorator sub = getSubModelDecorator();
        sub.setName("sub");
        sub.setEdbId("testSub/2");
        model.setSubModel(sub.getModel());

        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);

        EDBObject mainObject = edbService.getObject(getModelOid("testSub/1"));
        EDBObject subObject = edbService.getObject(getModelOid("testSub/2"));

        assertThat(subObject, notNullValue());
        assertThat(mainObject.getString("subModel"), is(getModelOid("testSub/2")));
    }

    @Test
    public void testSupportOfListOfSubModels_shouldWork() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setName("test");
        model.setEdbId("testSub/3");

        SubModelDecorator sub1 = getSubModelDecorator();
        sub1.setName("sub1");
        sub1.setEdbId("testSub/4");
        SubModelDecorator sub2 = getSubModelDecorator();
        sub2.setName("sub2");
        sub2.setEdbId("testSub/5");

        List<Object> subs = new ArrayList<Object>();
        subs.add(sub1.getModel());
        subs.add(sub2.getModel());
        model.setSubs(subs);

        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);

        EDBObject mainObject = edbService.getObject(getModelOid("testSub/3"));
        EDBObject subObject1 = edbService.getObject(getModelOid("testSub/4"));
        EDBObject subObject2 = edbService.getObject(getModelOid("testSub/5"));

        assertThat(subObject1, notNullValue());
        assertThat(subObject2, notNullValue());
        assertThat(mainObject.getString("subs.0"), is(getModelOid("testSub/4")));
        assertThat(mainObject.getString("subs.1"), is(getModelOid("testSub/5")));
    }

    @Test
    public void testComplexModelComposition_persistsAndResolvesModelsCorrectly() throws Exception {
        // prepare
        TestModelDecorator root;
        TestModelDecorator child1;
        TestModelDecorator child2;
        TestModelDecorator child11;
        TestModelDecorator child12;
        TestModelDecorator rRoot;
        TestModelDecorator rChild1;
        TestModelDecorator rChild2;
        TestModelDecorator rChild11;
        TestModelDecorator rChild12;
        SubModelDecorator leaf;
        SubModelDecorator rLeaf;

        root = getTestModelDecorator();
        child1 = getTestModelDecorator();
        child2 = getTestModelDecorator();
        child11 = getTestModelDecorator();
        child12 = getTestModelDecorator();
        leaf = getSubModelDecorator();

        List<Object> rootChildren = new ArrayList<>();
        rootChildren.add(child1.getModel());
        rootChildren.add(child2.getModel());

        List<Object> child1Children = new ArrayList<>();
        child1Children.add(child11.getModel());
        child1Children.add(child12.getModel());

        root.setEdbId("root");
        root.setName("root");
        child1.setEdbId("child1");
        child1.setName("child1");
        child2.setEdbId("child2");
        child2.setName("child2");
        child11.setEdbId("child11");
        child11.setName("child11");
        child12.setEdbId("child12");
        child12.setName("child12");
        leaf.setEdbId("leaf");
        leaf.setName("leaf");

        child11.setSubModel(leaf.getModel());
        child1.setChildren(child1Children);
        root.setChildren(rootChildren);

        // test
        EKBCommit commit = getTestEKBCommit().addInsert(root.getModel());
        ekbService.commit(commit);

        rRoot = new TestModelDecorator(ekbService.getModel(getTestModel(), getModelOid("root")));

        // assert
        assertThat(rRoot.getEdbId(), is("root"));
        assertThat(rRoot.getChildren(), notNullValue());

        List<?> resultChildren = rRoot.getChildren();
        assertThat(resultChildren.size(), is(2));

        rChild1 = new TestModelDecorator(resultChildren.get(0));
        rChild2 = new TestModelDecorator(resultChildren.get(1));

        assertThat(rChild1.getEdbId(), is("child1"));
        assertThat(rChild1.getChildren(), notNullValue());

        assertThat(rChild2.getEdbId(), is("child2"));

        List<Object> rChild1Children = rChild1.getChildren();
        assertThat(rChild1Children.size(), is(2));

        rChild11 = new TestModelDecorator(rChild1Children.get(0));
        rChild12 = new TestModelDecorator(rChild1Children.get(1));

        assertThat(rChild11.getEdbId(), is("child11"));
        assertThat(rChild12.getEdbId(), is("child12"));

        rLeaf = new SubModelDecorator(rChild11.getSubModel());
        assertThat(rLeaf.getEdbId(), is("leaf"));
    }

    @Test
    public void testComplexModelComposition_cascadesDeleteCorrectly() throws Exception {
        // prepare
        TestModelDecorator root;
        TestModelDecorator child1;
        TestModelDecorator child2;
        TestModelDecorator child11;
        TestModelDecorator child12;
        SubModelDecorator leaf;

        root = getTestModelDecorator();
        child1 = getTestModelDecorator();
        child2 = getTestModelDecorator();
        child11 = getTestModelDecorator();
        child12 = getTestModelDecorator();
        leaf = getSubModelDecorator();

        List<Object> rootChildren = new ArrayList<>();
        rootChildren.add(child1.getModel());
        rootChildren.add(child2.getModel());

        List<Object> child1Children = new ArrayList<>();
        child1Children.add(child11.getModel());
        child1Children.add(child12.getModel());

        root.setEdbId("root/1");
        root.setName("root/1");
        child1.setEdbId("child1/1");
        child1.setName("child1/1");
        child2.setEdbId("child2/1");
        child2.setName("child2/1");
        child11.setEdbId("child11/1");
        child11.setName("child11/1");
        child12.setEdbId("child12/1");
        child12.setName("child12/1");
        leaf.setEdbId("leaf/1");
        leaf.setName("leaf/1");

        child11.setSubModel(leaf.getModel());
        child1.setChildren(child1Children);
        root.setChildren(rootChildren);

        EKBCommit commit = getTestEKBCommit().addInsert(root.getModel());
        ekbService.commit(commit);

        // test
        commit = getTestEKBCommit().addDelete(root.getModel());
        ekbService.commit(commit);

        // assert
        EDBObject rRoot = edbService.getObject(getModelOid("root/1"));
        EDBObject rChild1 = edbService.getObject(getModelOid("child1/1"));
        EDBObject rChild2 = edbService.getObject(getModelOid("child2/1"));
        EDBObject rChild11 = edbService.getObject(getModelOid("child11/1"));
        EDBObject rChild12 = edbService.getObject(getModelOid("child12/1"));
        EDBObject rLeaf = edbService.getObject(getModelOid("leaf/1"));

        assertThat(rRoot.isDeleted(), is(true));
        assertThat(rChild1.isDeleted(), is(true));
        assertThat(rChild2.isDeleted(), is(true));
        assertThat(rChild11.isDeleted(), is(true));
        assertThat(rChild12.isDeleted(), is(true));
        assertThat(rLeaf.isDeleted(), is(true));
    }

    @Test
    public void testModelTailIsLoaded_shouldLoadModelTail() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setEdbId("modeltailtest/1");
        model.setName("blub");

        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);

        ModelWrapper result = ModelWrapper.wrap(ekbService.getModel(getTestModel(), getModelOid("modeltailtest/1")));
        Boolean versionPresent = result.retrieveInternalModelVersion() != null;
        assertThat(versionPresent, is(true));
    }

    @Test
    public void testIfModelMetaDataRetrievingWorks_shouldWork() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setName("blub");
        model.setEdbId("modelmetatest/1");

        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);
        ModelWrapper wrapper = ModelWrapper.wrap(ekbService.getModel(getTestModel(), getModelOid("modelmetatest/1")));
        assertThat(wrapper.toOpenEngSBModelEntries(), notNullValue());
        assertThat(wrapper.retrieveInternalModelId(), notNullValue());
        assertThat(wrapper.retrieveInternalModelVersion(), notNullValue());
        assertThat(wrapper.retrieveInternalModelTimestamp(), notNullValue());
    }

    @Test
    public void testIfSubModelIsPersistedAlso_shouldPersistParentAndSubModel() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setEdbId("submodeltest/1");

        SubModelDecorator sub = getSubModelDecorator();
        sub.setName("test");
        sub.setEdbId("submodeltest/1/1");
        model.setSubModel(sub.getModel());

        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);
        model = loadTestModel("submodeltest/1");
        assertThat(model.getModel(), notNullValue());
        sub = new SubModelDecorator(model.getSubModel());
        assertThat(sub.getModel(), notNullValue());
        assertThat(sub.getName(), is("test"));
    }

    @Test
    public void testIfSubModelsArePersistedAlso_shouldPersistParentAndSubModels() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setEdbId("submodeltest/2");
        List<Object> subs = new ArrayList<Object>();
        SubModelDecorator sub = getSubModelDecorator();
        sub.setName("test1");
        sub.setEdbId("submodeltest/2/1");
        subs.add(sub.getModel());
        sub = getSubModelDecorator();
        sub.setName("test2");
        sub.setEdbId("submodeltest/2/2");
        subs.add(sub.getModel());
        model.setSubs(subs);

        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);
        model = loadTestModel("submodeltest/2");
        assertThat(model.getModel(), notNullValue());
        assertThat(model.getSubs(), notNullValue());
        assertThat(model.getSubs().get(0), notNullValue());
        assertThat(model.getSubs().get(1), notNullValue());
        sub = new SubModelDecorator(model.getSubs().get(0));
        assertThat(sub.getName(), is("test1"));
        sub = new SubModelDecorator(model.getSubs().get(1));
        assertThat(sub.getName(), is("test2"));
        sub = loadSubModel("submodeltest/2/1");
        assertThat(sub.getName(), is("test1"));
        sub = loadSubModel("submodeltest/2/2");
        assertThat(sub.getName(), is("test2"));
    }

    @Test
    public void testIfSubModelIsUpdatedAlso_shouldUpdateParentAndSubModel() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setEdbId("submodeltest/3");

        SubModelDecorator sub = getSubModelDecorator();
        sub.setName("test");
        sub.setEdbId("submodeltest/3/1");
        model.setSubModel(sub.getModel());

        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);

        sub.setName("updated");
        commit = getTestEKBCommit().addUpdate(model.getModel());
        ekbService.commit(commit);

        sub = loadSubModel("submodeltest/3/1");
        assertThat(sub.getModel(), notNullValue());
        assertThat(sub.getName(), is("updated"));
    }

    @Test
    public void testIfSubModelsAreUpdatedAlso_shouldUpdateParentAndSubModels() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setEdbId("submodeltest/4");
        List<Object> subs = new ArrayList<Object>();
        SubModelDecorator sub = getSubModelDecorator();
        sub.setName("test1");
        sub.setEdbId("submodeltest/4/1");
        subs.add(sub.getModel());
        sub = getSubModelDecorator();
        sub.setName("test2");
        sub.setEdbId("submodeltest/4/2");
        subs.add(sub.getModel());
        model.setSubs(subs);

        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);

        subs.clear();
        sub = getSubModelDecorator();
        sub.setName("updatedtest1");
        sub.setEdbId("submodeltest/4/1");
        subs.add(sub.getModel());
        sub = getSubModelDecorator();
        sub.setName("updatedtest2");
        sub.setEdbId("submodeltest/4/2");
        subs.add(sub.getModel());
        sub = getSubModelDecorator();
        sub.setName("insertedtest3");
        sub.setEdbId("submodeltest/4/3");
        subs.add(sub.getModel());
        model.setSubs(subs);
        commit = getTestEKBCommit().addUpdate(model.getModel());
        ekbService.commit(commit);

        model = new TestModelDecorator(ekbService.getModel(getTestModel(), getModelOid("submodeltest/4")));
        assertThat(model.getModel(), notNullValue());
        assertThat(model.getSubs(), notNullValue());
        assertThat(model.getSubs().get(0), notNullValue());
        assertThat(model.getSubs().get(1), notNullValue());
        assertThat(model.getSubs().get(2), notNullValue());
        sub = new SubModelDecorator(model.getSubs().get(0));
        assertThat(sub.getName(), is("updatedtest1"));
        sub = new SubModelDecorator(model.getSubs().get(1));
        assertThat(sub.getName(), is("updatedtest2"));
        sub = new SubModelDecorator(model.getSubs().get(2));
        assertThat(sub.getName(), is("insertedtest3"));
        sub = loadSubModel("submodeltest/4/1");
        assertThat(sub.getName(), is("updatedtest1"));
        sub = loadSubModel("submodeltest/4/2");
        assertThat(sub.getName(), is("updatedtest2"));
        sub = loadSubModel("submodeltest/4/3");
        assertThat(sub.getName(), is("insertedtest3"));
    }

    @Test
    public void testIfSubModelIsDeletedAlso_shouldDeleteParentAndSubModel() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setEdbId("submodeltest/5");

        SubModelDecorator sub = getSubModelDecorator();
        sub.setName("test");
        sub.setEdbId("submodeltest/5/1");
        model.setSubModel(sub.getModel());

        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);
        commit = getTestEKBCommit().addDelete(model.getModel());
        ekbService.commit(commit);

        EDBObject testModel = edbService.getObject(getModelOid("submodeltest/5"));
        EDBObject subModel = edbService.getObject(getModelOid("submodeltest/5/1"));
        assertThat(testModel.isDeleted(), is(true));
        assertThat(subModel.isDeleted(), is(true));
    }

    @Test
    public void testIfSubModelsAreDeletedAlso_shouldDeleteParentAndSubModels() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setEdbId("submodeltest/6");
        List<Object> subs = new ArrayList<Object>();
        SubModelDecorator sub = getSubModelDecorator();
        sub.setName("test1");
        sub.setEdbId("submodeltest/6/1");
        subs.add(sub.getModel());
        sub = getSubModelDecorator();
        sub.setName("test2");
        sub.setEdbId("submodeltest/6/2");
        subs.add(sub.getModel());
        model.setSubs(subs);

        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);
        commit = getTestEKBCommit().addDelete(model.getModel());
        ekbService.commit(commit);

        EDBObject testModel = edbService.getObject(getModelOid("submodeltest/6"));
        EDBObject subModel1 = edbService.getObject(getModelOid("submodeltest/6/1"));
        EDBObject subModel2 = edbService.getObject(getModelOid("submodeltest/6/2"));
        assertThat(testModel.isDeleted(), is(true));
        assertThat(subModel1.isDeleted(), is(true));
        assertThat(subModel2.isDeleted(), is(true));
    }

    @Test
    public void testIfSubModelIsLoadedCorrectly_shouldLoadCorrectVersionOfSubModel() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setEdbId("submodeltest/7");

        SubModelDecorator sub = getSubModelDecorator();
        sub.setName("test");
        sub.setEdbId("submodeltest/7/1");
        model.setSubModel(sub.getModel());

        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);
        Thread.sleep(5);

        sub.setName("test2");
        commit = getTestEKBCommit().addUpdate(sub.getModel());
        ekbService.commit(commit);

        sub = loadSubModel("submodeltest/7/1");
        assertThat(sub.getModel(), notNullValue());
        assertThat(sub.getName(), is("test2"));

        model = loadTestModel("submodeltest/7");
        assertThat(model.getModel(), notNullValue());
        sub = new SubModelDecorator(model.getSubModel());
        assertThat(sub.getModel(), notNullValue());
        assertThat(sub.getName(), is("test"));
    }

    @Test(expected = EKBException.class)
    public void testRevertInvalidCommit_shouldThrowException() throws Exception {
        ekbService.deleteCommit(UUID.randomUUID());
    }

    @Test
    public void testRevertFunctionality_shouldRevertModelsToOldState() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setEdbId("reverttest/1");
        model.setName("before");
        TestModelDecorator model2 = getTestModelDecorator();
        model2.setEdbId("reverttest/2");
        model2.setName("test");
        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);
        UUID revision = commit.getRevisionNumber();

        model.setName("middle");
        commit = getTestEKBCommit().addUpdate(model.getModel());
        ekbService.commit(commit);
        UUID revision2 = commit.getRevisionNumber();

        commit = getTestEKBCommit().addInsert(model2.getModel());
        ekbService.commit(commit);
        model.setName("after");
        commit = getTestEKBCommit().addUpdate(model.getModel());
        ekbService.commit(commit);

        TestModelDecorator result1 = new TestModelDecorator(ekbService.getModel(getTestModel(),
                getModelOid("reverttest/1")));
        ekbService.deleteCommit(revision2);
        TestModelDecorator result2 = new TestModelDecorator(ekbService.getModel(getTestModel(),
                getModelOid("reverttest/1")));
        ekbService.deleteCommit(revision);
        TestModelDecorator result3 = new TestModelDecorator(ekbService.getModel(getTestModel(),
                getModelOid("reverttest/1")));
        assertThat(result1, notNullValue());
        assertThat(result2, notNullValue());
        assertThat(result3, notNullValue());
        assertThat(result1.getName(), is("after"));
        assertThat(result2.getName(), is("middle"));
        assertThat(result3.getName(), is("before"));
    }

    @Test
    public void testLoadingOfEKBCommits_shouldLoadAllInformation() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setEdbId("ekbload/1");
        model.setName("test");
        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        commit.setComment("This is the first comment");
        ekbService.commit(commit);
        UUID revision = commit.getRevisionNumber();
        model.setName("test2");
        commit = getTestEKBCommit().addUpdate(model.getModel());
        commit.setComment("This is the second comment");
        ekbService.commit(commit);
        UUID revision2 = commit.getRevisionNumber();

        commit = ekbService.loadCommit(revision);
        assertThat(commit, notNullValue());
        assertThat(commit.getInserts().size(), is(1));
        assertThat(commit.getRevisionNumber(), is(revision));
        assertThat(commit.getDomainId(), is(getTestEKBCommit().getDomainId()));
        assertThat(commit.getConnectorId(), is(getTestEKBCommit().getConnectorId()));
        assertThat(commit.getInstanceId(), is(getTestEKBCommit().getInstanceId()));
        assertThat(commit.getComment(), is("This is the first comment"));

        commit = ekbService.loadCommit(revision2);
        assertThat(commit, notNullValue());
        assertThat(commit.getUpdates().size(), is(1));
        assertThat(commit.getRevisionNumber(), is(revision2));
        assertThat(commit.getParentRevisionNumber(), is(revision));
        assertThat(commit.getDomainId(), is(getTestEKBCommit().getDomainId()));
        assertThat(commit.getConnectorId(), is(getTestEKBCommit().getConnectorId()));
        assertThat(commit.getInstanceId(), is(getTestEKBCommit().getInstanceId()));
        assertThat(commit.getComment(), is("This is the second comment"));
    }

    @Test
    public void testPrimitivePropertyTypeConversion_shouldConvertAndPersistWithCorrectType() throws Exception {
        PrimitivePropertyModelDecorator model = getPrimitivePropertyModelDecorator();
        model.setId("ppm/0");
        model.setBooleanByGet(true);
        model.setBooleanByIs(true);
        model.setPrimitiveChar(Character.MAX_VALUE);
        model.setPrimitiveDouble(Double.MAX_VALUE);
        model.setPrimitiveFloat(Float.MAX_VALUE);
        model.setPrimitiveInt(Integer.MAX_VALUE);
        model.setPrimitiveLong(Long.MAX_VALUE);
        model.setPrimitiveShort(Short.MAX_VALUE);

        assertThat(model.isBooleanByIs(), is(true));
        assertThat(model.getBooleanByGet(), is(true));

        EKBCommit commit = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit);

        // check edb object
        EDBObject edbObject = edbService.getObject(getModelOid("ppm/0"));
        // check entry types
        assertThat(edbObject.get("booleanByGet").getType(), is(Boolean.class.getName()));
        assertThat(edbObject.get("booleanByIs").getType(), is(Boolean.class.getName()));
        assertThat(edbObject.get("primitiveChar").getType(), is(Character.class.getName()));
        assertThat(edbObject.get("primitiveShort").getType(), is(Short.class.getName()));
        assertThat(edbObject.get("primitiveInt").getType(), is(Integer.class.getName()));
        assertThat(edbObject.get("primitiveLong").getType(), is(Long.class.getName()));
        assertThat(edbObject.get("primitiveFloat").getType(), is(Float.class.getName()));
        assertThat(edbObject.get("primitiveDouble").getType(), is(Double.class.getName()));

        // check values
        assertThat(edbObject.getBoolean("booleanByGet"), is(true));
        assertThat(edbObject.getBoolean("booleanByIs"), is(true));
        assertThat(edbObject.getChar("primitiveChar"), is(Character.MAX_VALUE));
        assertThat(edbObject.getShort("primitiveShort"), is(Short.MAX_VALUE));
        assertThat(edbObject.getInteger("primitiveInt"), is(Integer.MAX_VALUE));
        assertThat(edbObject.getLong("primitiveLong"), is(Long.MAX_VALUE));
        assertThat(edbObject.getFloat("primitiveFloat"), is(Float.MAX_VALUE));
        assertThat(edbObject.getDouble("primitiveDouble"), is(Double.MAX_VALUE));
    }

    @Test
    public void testDeleteCommitWithLastRevision_shouldDeleteCommit() throws Exception {
        UUID preCommit1Revision = ekbService.getLastRevisionId();
        TestModelDecorator model1 = getTestModelDecorator();
        model1.setEdbId("deleteCommitTest/1");
        EKBCommit commit = getTestEKBCommit().addInsert(model1.getModel());
        ekbService.commit(commit);

        UUID preCommit2Revision = ekbService.getLastRevisionId();
        TestModelDecorator model2 = getTestModelDecorator();
        model2.setEdbId("deleteCommitTest/2");
        commit = getTestEKBCommit().addInsert(model2.getModel());
        commit.addDelete(model1.getModel());
        ekbService.commit(commit);

        UUID postCommit2Revision = ekbService.getLastRevisionId();
        ekbService.deleteCommit(postCommit2Revision);

        UUID postDelete1Revision = ekbService.getLastRevisionId();
        assertThat(postDelete1Revision, is(preCommit2Revision));
        QueryRequest request = QueryRequest.query("edbId", "deleteCommitTest/1");
        List<Object> result = ekbService.query(new SingleModelQuery(getTestModel(), new EDBQueryFilter(request), null));
        assertThat(result.size(), is(1));
        ekbService.deleteCommit(preCommit2Revision);

        UUID postDelete2Revision = ekbService.getLastRevisionId();
        assertThat(postDelete2Revision, is(preCommit1Revision));
        request = QueryRequest.query("edbId", "deleteCommitTest/1");
        result = ekbService.query(new SingleModelQuery(getTestModel(), new EDBQueryFilter(request), null));
        assertThat(result.size(), is(0));
    }

    @Test(expected = EKBConcurrentException.class)
    public void testDeleteCommitWithOldRevision_shouldThrowException() throws Exception {
        TestModelDecorator model = getTestModelDecorator();
        model.setEdbId("deleteCommitTest/3");
        EKBCommit commit1 = getTestEKBCommit().addInsert(model.getModel());
        ekbService.commit(commit1);

        UUID postFirstCommitRevision = ekbService.getLastRevisionId();

        TestModelDecorator model2 = getTestModelDecorator();
        model.setEdbId("deleteCommitTest/4");
        EKBCommit commit2 = getTestEKBCommit().addInsert(model2.getModel());
        ekbService.commit(commit2);

        ekbService.deleteCommit(postFirstCommitRevision);
    }

    @Test(expected = EKBException.class)
    public void testDeleteCommitWithWrongRevision_shouldThrowException() {
        ekbService.deleteCommit(UUID.randomUUID());
    }

    private TestModelDecorator loadTestModel(String oid) throws Exception {
        return new TestModelDecorator(ekbService.getModel(getTestModel(), getModelOid(oid)));
    }

    private SubModelDecorator loadSubModel(String oid) throws Exception {
        return new SubModelDecorator(ekbService.getModel(getSubModel(), getModelOid(oid)));
    }

    private TestModelDecorator getTestModelDecorator() throws Exception {
        return new TestModelDecorator(getTestModel().newInstance());
    }

    private SubModelDecorator getSubModelDecorator() throws Exception {
        return new SubModelDecorator(getSubModel().newInstance());
    }

    private PrimitivePropertyModelDecorator getPrimitivePropertyModelDecorator() throws Exception {
        return new PrimitivePropertyModelDecorator(getPrimitivePropertyModel().newInstance());
    }
}
