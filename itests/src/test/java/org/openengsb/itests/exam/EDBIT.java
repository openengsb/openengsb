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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.karaf.tooling.exam.options.KarafDistributionConfigurationFilePutOption;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.model.OpenEngSBFileModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.core.edb.api.EDBCommit;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBException;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.EKBException;
import org.openengsb.core.ekb.api.PersistInterface;
import org.openengsb.core.ekb.api.QueryInterface;
import org.openengsb.itests.exam.models.SubModel;
import org.openengsb.itests.exam.models.TestModel;
import org.openengsb.itests.exam.models.TestModelProvider;
import org.openengsb.itests.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.tinybundles.core.TinyBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.util.tracker.ServiceTracker;

@RunWith(JUnit4TestRunner.class)
public class EDBIT extends AbstractExamTestHelper {

    private EngineeringDatabaseService edbService;
    private QueryInterface query;
    private PersistInterface persist;
    private boolean providerInstalled = false;

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
        testObject.put("testkey", "testvalue");
        commit.insert(testObject);
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
        commit.insert(testObject);

        edbService.commit(commit);

        EDBObject obj = edbService.getObject("newtestobject");
        assertThat(obj, notNullValue());
    }

    @Test
    public void testQueryForObject_shouldWork() throws Exception {
        EDBCommit commit = edbService.createCommit("test", "test");
        EDBObject testObject = new EDBObject("newtestobject1");
        testObject.put("newtestkey1", "newtestvalue1");
        commit.insert(testObject);

        edbService.commit(commit);

        List<EDBObject> objects = edbService.query("newtestkey1", "newtestvalue1");
        assertThat(objects, notNullValue());
        assertThat(objects.size(), not(0));
    }

    @Test(expected = EDBException.class)
    public void testConflictDetection_shouldThrowException() throws Exception {
        EDBCommit commit = edbService.createCommit("test", "test");
        EDBObject testObject = new EDBObject("newtestobject2");
        testObject.put("newtestkey2", "newtestvalue2");
        commit.insert(testObject);

        edbService.commit(commit);

        commit = edbService.createCommit("test", "test");

        EDBObject obj = edbService.getObject("newtestobject2");
        obj.put(EDBConstants.MODEL_VERSION, 0);
        obj.put("test", "test");

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

        String name = (String) obj.get("name");
        Integer version = Integer.parseInt((String) obj.get(EDBConstants.MODEL_VERSION));

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

        List<Object> result = (List<Object>) query.queryForModels(getTestModel(), "name:\"C:\\test\"");
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

        String name1 = (String) obj.get("name");
        Integer version1 = Integer.parseInt((String) obj.get(EDBConstants.MODEL_VERSION));

        setProperty(model, "setName", "test2");
        commit = getTestEKBCommit().addUpdate(model);

        Object model2 = getTestModel().newInstance();
        setProperty(model2, "setName", "test3");
        setProperty(model2, "setEdbId", "batchevent/2");
        commit.addInsert(model2);
        persist.commit(commit);

        obj = edbService.getObject("testdomain/testconnector/batchevent/1");

        String name2 = (String) obj.get("name");
        Integer version2 = Integer.parseInt((String) obj.get(EDBConstants.MODEL_VERSION));

        obj = edbService.getObject("testdomain/testconnector/batchevent/2");

        String name3 = (String) obj.get("name");
        Integer version3 = Integer.parseInt((String) obj.get(EDBConstants.MODEL_VERSION));

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

        String name1 = (String) obj.get("name");
        Integer version1 = Integer.parseInt((String) obj.get(EDBConstants.MODEL_VERSION));

        setProperty(model, "setName", "test2");

        commit = getTestEKBCommit().addUpdate(model);
        persist.commit(commit);

        obj = edbService.getObject("testdomain/testconnector/updateevent/2");

        String name2 = (String) obj.get("name");
        Integer version2 = Integer.parseInt((String) obj.get(EDBConstants.MODEL_VERSION));

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
        Integer version1 = Integer.parseInt((String) obj.get(EDBConstants.MODEL_VERSION));
        OpenEngSBModelEntry entry = new OpenEngSBModelEntry(EDBConstants.MODEL_VERSION, 0, Integer.class);
        ModelUtils.addOpenEngSBModelEntry(model, entry);
        commit = getTestEKBCommit().addUpdate(model);
        persist.commit(commit);

        // results in no conflict because the values are the same even if the version is different
        obj = edbService.getObject("testdomain/testconnector/updateevent/3");
        Integer version2 = Integer.parseInt((String) obj.get(EDBConstants.MODEL_VERSION));

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
        assertThat(mainObject.getString("subs0"), is("testdomain/testconnector/testSub/4"));
        assertThat(mainObject.getString("subs1"), is("testdomain/testconnector/testSub/5"));
    }

    private void setProperty(Object model, String methodName, Object... params) throws Exception {
        Class<?> classes[] = new Class<?>[params.length];
        for (int i = 0; i < params.length; i++) {
            classes[i] = params[i].getClass();
        }
        try {
            Method method = model.getClass().getMethod(methodName, classes);
            method.invoke(model, params);
        } catch (Exception e) {
            for (Method method : model.getClass().getMethods()) {
                if (method.getName().equals(methodName)) {
                    method.invoke(model, params);
                    break;
                }
            }
        }
    }

    private EKBCommit getTestEKBCommit() {
        EKBCommit commit = new EKBCommit().setDomainId("testdomain").setConnectorId("testconnector");
        commit.setInstanceId("testinstance");
        return commit;
    }

    private void registerModelProvider() throws Exception {
        if (providerInstalled) {
            return;
        }
        TinyBundle providerTinyBundle =
            bundle()
                .add(TestModel.class)
                .add(SubModel.class)
                .add(TestModelProvider.class)
                .set(Constants.BUNDLE_ACTIVATOR, TestModelProvider.class.getName())
                .set(Constants.BUNDLE_SYMBOLICNAME, "test.model.provider")
                .set(Constants.BUNDLE_VERSION, "1.0.0")
                .set(Constants.EXPORT_PACKAGE, "org.openengsb.itests.exam.models")
                .set(Constants.IMPORT_PACKAGE,
                    "org.openengsb.core.api.model, org.osgi.framework, org.slf4j, "
                            + "org.openengsb.labs.delegation.service")
                .set(org.openengsb.labs.delegation.service.Constants.PROVIDED_CLASSES_HEADER,
                    "org.openengsb.itests.exam.models.*")
                .set(org.openengsb.core.api.Constants.PROVIDE_MODELS_HEADER, "true");
        Bundle providerBundle =
            getBundleContext().installBundle("test://testlocation/test.provider.jar", providerTinyBundle.build());
        providerBundle.start();
        providerInstalled = true;
    }

    private Class<?> getTestModel() throws Exception {
        Object provider = loadTestModelProvider();
        return (Class<?>) provider.getClass().getMethod("loadTestModel").invoke(provider);
    }

    private Class<?> getSubModel() throws Exception {
        Object provider = loadTestModelProvider();
        return (Class<?>) provider.getClass().getMethod("loadSubModel").invoke(provider);
    }

    private Object loadTestModelProvider() throws Exception {
        String filter = String.format("(%s=%s)", Constants.OBJECTCLASS, TestModelProvider.class.getName());
        Filter osgiFilter = FrameworkUtil.createFilter(filter);
        ServiceTracker tracker = new ServiceTracker(getBundleContext(), osgiFilter, null);
        tracker.open(true);
        return tracker.waitForService(4000);
    }
}
