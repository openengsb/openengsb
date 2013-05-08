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

package org.openengsb.core.ekb.common;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.annotation.OpenEngSBForeignKey;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.common.models.EngineeringObjectModel;
import org.openengsb.core.ekb.common.models.RecursiveModel;
import org.openengsb.core.ekb.common.models.SubModel;
import org.openengsb.core.ekb.common.models.TestModel;
import org.openengsb.core.ekb.common.models.TestModel2.ENUM;
import org.openengsb.core.util.ModelUtils;

/**
 * The EDBConverter test file only tests the converting Model -> EDBObject since the other way round is tested by the
 * QueryInterfaceService test file.
 */
public class EDBConverterTest {
    private EDBConverter converter;
    private String contextId;

    private EngineeringDatabaseService mockedService;

    @Before
    public void setUp() {
        mockedService = mock(EngineeringDatabaseService.class);
        converter = new EDBConverter(mockedService);
        contextId = "testcontext";
        ContextHolder.get().setCurrentContextId(contextId);
    }

    @Test
    public void testIfModelAgentIsSet_shouldWork() throws Exception {
        TestModel model = new TestModel();
        assertThat("TestModel isn't enhanced. Maybe you forgot to set the java agent?",
                model instanceof OpenEngSBModel, is(true));
    }

    @Test
    public void testSimpleModelToEDBObjectConversion_shouldWork() throws Exception {
        TestModel model = new TestModel();
        model.setId("test");
        Date date = new Date();
        model.setDate(date);
        model.setEnumeration(ENUM.A);
        model.setName("testobject");

        ConnectorInformation id = getTestConnectorInformation();

        List<EDBObject> objects = converter.convertModelToEDBObject(model, id);
        EDBObject object = objects.get(0);

        assertThat(object.getString("connectorId"), is("testconnector"));
        assertThat(object.getString("id"), is("test"));
        assertThat(object.getString("oid"), is(EDBConverterUtils.createOID(model, contextId)));
        assertThat(object.getString("domainId"), is("testdomain"));
        assertThat(object.getString("name"), is("testobject"));
        assertThat(object.getString("instanceId"), is("testinstance"));
        assertThat(object.getObject("enumeration", ENUM.class), is(ENUM.A));
        assertThat(object.getObject("date", Date.class), is(date));
        assertThat(object.getString(EDBConstants.MODEL_TYPE), is(TestModel.class.getName()));
    }

    @Test
    public void testComplexModelToEDBObjectConversion_shouldWork() throws Exception {
        TestModel model = new TestModel();
        model.setId("test");

        SubModel sub = new SubModel();
        sub.setId("sub");
        sub.setValue("teststring");
        model.setSub(sub);

        ConnectorInformation id = getTestConnectorInformation();

        List<EDBObject> objects = converter.convertModelToEDBObject(model, id);
        EDBObject object = objects.get(1);
        assertThat(object.getString("sub"), is(EDBConverterUtils.createOID(sub, contextId)));
        EDBObject subObject = objects.get(0);
        assertThat(subObject.getString("id"), is("sub"));
        assertThat(subObject.getString(EDBConstants.MODEL_TYPE), is(SubModel.class.getName()));
    }

    @Test
    public void testRecursiveModelToEDBObjectConversion_shouldWork() throws Exception {
        // prepare
        RecursiveModel root = new RecursiveModel();
        root.setId("root");

        RecursiveModel rootChild = new RecursiveModel();
        rootChild.setId("root_child");

        RecursiveModel rootChildChild = new RecursiveModel();
        rootChildChild.setId("root_child_child");

        RecursiveModel rootChildChildChild = new RecursiveModel();
        rootChildChildChild.setId("root_child_child_child");

        root.setChild(rootChild);
        rootChild.setChild(rootChildChild);
        rootChildChild.setChild(rootChildChildChild);

        ConnectorInformation id = getTestConnectorInformation();

        // test
        List<EDBObject> objects = converter.convertModelToEDBObject(root, id);

        // assert
        EDBObject obj;

        obj = objects.get(3);
        assertEquals("root", obj.getString("id"));
        assertEquals(RecursiveModel.class.getName(), obj.getString(EDBConstants.MODEL_TYPE));
        assertEquals("testcontext/root_child", obj.getString("child"));

        obj = objects.get(2);
        assertEquals("root_child", obj.getString("id"));
        assertEquals(RecursiveModel.class.getName(), obj.getString(EDBConstants.MODEL_TYPE));
        assertEquals("testcontext/root_child_child", obj.getString("child"));

        obj = objects.get(1);
        assertEquals("root_child_child", obj.getString("id"));
        assertEquals(RecursiveModel.class.getName(), obj.getString(EDBConstants.MODEL_TYPE));
        assertEquals("testcontext/root_child_child_child", obj.getString("child"));

        obj = objects.get(0);
        assertEquals("root_child_child_child", obj.getString("id"));
        assertEquals(RecursiveModel.class.getName(), obj.getString(EDBConstants.MODEL_TYPE));
        assertNull(obj.get("child"));
    }

    @Test
    public void testRecursiveModelWithCompositionToEDBObjectConversion_shouldWork() throws Exception {
        // prepare
        RecursiveModel root = new RecursiveModel("root");

        RecursiveModel rootChild1 = new RecursiveModel("root_child1");
        RecursiveModel rootChild2 = new RecursiveModel("root_child2");

        RecursiveModel child1Child1 = new RecursiveModel("child1_child1");

        RecursiveModel child2Child1 = new RecursiveModel("child2_child1");
        RecursiveModel child2Child2 = new RecursiveModel("child2_child2");

        List<RecursiveModel> rootChildren = Arrays.asList(new RecursiveModel[] { rootChild1, rootChild2 });
        List<RecursiveModel> child2Children = Arrays.asList(new RecursiveModel[] { child2Child1, child2Child2 });

        root.setChildren(rootChildren);
        rootChild1.setChild(child1Child1);
        rootChild2.setChildren(child2Children);

        ConnectorInformation id = getTestConnectorInformation();

        // test
        List<EDBObject> objects = converter.convertModelToEDBObject(root, id);

        // assert
        EDBObject obj;

        obj = objects.get(5);
        assertEquals("root", obj.getString("id"));
        assertEquals(RecursiveModel.class.getName(), obj.getString(EDBConstants.MODEL_TYPE));
        assertEquals("testcontext/root_child1", obj.getString("children.0"));
        assertEquals("testcontext/root_child2", obj.getString("children.1"));

        obj = objects.get(4);
        assertEquals("root_child2", obj.getString("id"));
        assertEquals(RecursiveModel.class.getName(), obj.getString(EDBConstants.MODEL_TYPE));
        assertEquals("testcontext/child2_child1", obj.getString("children.0"));
        assertEquals("testcontext/child2_child2", obj.getString("children.1"));

        obj = objects.get(3);
        assertEquals("child2_child2", obj.getString("id"));
        assertEquals(RecursiveModel.class.getName(), obj.getString(EDBConstants.MODEL_TYPE));

        obj = objects.get(2);
        assertEquals("child2_child1", obj.getString("id"));
        assertEquals(RecursiveModel.class.getName(), obj.getString(EDBConstants.MODEL_TYPE));

        obj = objects.get(1);
        assertEquals("root_child1", obj.getString("id"));
        assertEquals(RecursiveModel.class.getName(), obj.getString(EDBConstants.MODEL_TYPE));
        assertEquals("testcontext/child1_child1", obj.getString("child"));

        obj = objects.get(0);
        assertEquals("child1_child1", obj.getString("id"));
        assertEquals(RecursiveModel.class.getName(), obj.getString(EDBConstants.MODEL_TYPE));
    }

    @Test
    public void testComplexListModelToEDBObjectConversion_shouldWork() throws Exception {
        TestModel model = new TestModel();
        model.setId("test");

        SubModel sub1 = new SubModel();
        sub1.setId("sub1");
        sub1.setValue("teststring1");
        SubModel sub2 = new SubModel();
        sub2.setId("sub2");
        sub2.setValue("teststring2");
        List<SubModel> subs = new ArrayList<SubModel>();
        subs.add(sub1);
        subs.add(sub2);

        model.setSubs(subs);

        ConnectorInformation id = getTestConnectorInformation();

        List<EDBObject> objects = converter.convertModelToEDBObject(model, id);
        EDBObject object = objects.get(2);

        assertThat(object.getString(EDBConverterUtils.getEntryNameForList("subs", 0)),
                is(EDBConverterUtils.createOID(sub1, contextId)));
        assertThat(object.getString(EDBConverterUtils.getEntryNameForList("subs", 1)),
                is(EDBConverterUtils.createOID(sub2, contextId)));

        EDBObject subObject1 = objects.get(0);
        assertThat(subObject1.getString("id"), is("sub1"));
        assertThat(subObject1.getString(EDBConstants.MODEL_TYPE), is(SubModel.class.getName()));
        EDBObject subObject2 = objects.get(1);
        assertThat(subObject2.getString("id"), is("sub2"));
        assertThat(subObject2.getString(EDBConstants.MODEL_TYPE), is(SubModel.class.getName()));
    }

    @Test
    public void testMapModelToEDBObjectConversion_shouldWork() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("keyA", "valueA");
        map.put("keyB", "valueB");
        TestModel model = new TestModel();
        model.setId("test");
        model.setMap(map);

        ConnectorInformation id = getTestConnectorInformation();

        EDBObject object = converter.convertModelToEDBObject(model, id).get(0);

        assertThat(object.getString(EDBConverterUtils.getEntryNameForMapKey("map", 0)), is("keyA"));
        assertThat(object.getString(EDBConverterUtils.getEntryNameForMapValue("map", 0)), is("valueA"));
        assertThat(object.getString(EDBConverterUtils.getEntryNameForMapKey("map", 1)), is("keyB"));
        assertThat(object.getString(EDBConverterUtils.getEntryNameForMapValue("map", 1)), is("valueB"));
    }

    @Test
    public void testConversionInBothDirections_shouldWork() throws Exception {
        TestModel model = new TestModel();
        model.setId("test");
        Date date = new Date();
        model.setDate(date);
        model.setEnumeration(ENUM.A);
        model.setName("testobject");

        EDBObject object = converter.convertModelToEDBObject(model, getTestConnectorInformation()).get(0);
        TestModel result = converter.convertEDBObjectToModel(TestModel.class, object);

        assertThat(model.getId(), is(result.getId()));
        assertThat(model.getDate(), is(result.getDate()));
        assertThat(model.getEnumeration(), is(result.getEnumeration()));
        assertThat(model.getName(), is(result.getName()));
    }

    @Test
    public void testIfArraysAreSupported_shouldWork() throws Exception {
        TestModel model = new TestModel();
        Integer[] numbers = new Integer[] { 1, 2, 3, 4 };
        model.setNumbers(numbers);
        EDBObject object = converter.convertModelToEDBObject(model, getTestConnectorInformation()).get(0);
        TestModel result = converter.convertEDBObjectToModel(TestModel.class, object);

        assertThat(result.getNumbers(), notNullValue());
        assertThat(numbers[0], is(result.getNumbers()[0]));
        assertThat(numbers[1], is(result.getNumbers()[1]));
        assertThat(numbers[2], is(result.getNumbers()[2]));
        assertThat(numbers[3], is(result.getNumbers()[3]));
    }

    @Test
    public void testEDBObjectToModelConversion_shouldWork() throws Exception {
        EDBObject object = new EDBObject("test");
        object.putEDBObjectEntry(EDBConstants.MODEL_TYPE, TestModel.class.getName());
        object.putEDBObjectEntry(EDBConstants.MODEL_OID, "test");
        object.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(1));
        object.putEDBObjectEntry("id", "test");
        object.putEDBObjectEntry("name", "testname");
        object.putEDBObjectEntry("number", 42);
        TestModel model = converter.convertEDBObjectToModel(TestModel.class, object);
        assertThat(model.getId(), is("test"));
        assertThat(model.getName(), is("testname"));
        List<OpenEngSBModelEntry> entries = ModelUtils.getOpenEngSBModelEntries(model);
        Integer version = null;
        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals(EDBConstants.MODEL_VERSION)) {
                version = (Integer) entry.getValue();
                break;
            }
        }
        assertThat(version, notNullValue());
        assertThat(version, is(1));
    }

    @Test
    public void testEDBObjectToRecursiveModelConversion_shouldWork() throws Exception {
        // prepare
        EDBObject root = new EDBObject("root");
        root.putEDBObjectEntry(EDBConstants.MODEL_TYPE, RecursiveModel.class.getName());
        root.putEDBObjectEntry(EDBConstants.MODEL_OID, "root");
        root.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(1));
        root.putEDBObjectEntry("id", "root");
        root.putEDBObjectEntry("child", "child1");

        EDBObject child1 = new EDBObject("child1");
        child1.putEDBObjectEntry(EDBConstants.MODEL_TYPE, RecursiveModel.class.getName());
        child1.putEDBObjectEntry(EDBConstants.MODEL_OID, "child1");
        child1.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(1));
        child1.putEDBObjectEntry("id", "child1");
        child1.putEDBObjectEntry("child", "child2");

        EDBObject child2 = new EDBObject("child2");
        child2.putEDBObjectEntry(EDBConstants.MODEL_TYPE, RecursiveModel.class.getName());
        child2.putEDBObjectEntry(EDBConstants.MODEL_OID, "child2");
        child2.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(1));
        child2.putEDBObjectEntry("id", "child2");
        child2.putEDBObjectEntry("child", "child3");

        EDBObject child3 = new EDBObject("child3");
        child3.putEDBObjectEntry(EDBConstants.MODEL_TYPE, RecursiveModel.class.getName());
        child3.putEDBObjectEntry(EDBConstants.MODEL_OID, "child3");
        child3.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(1));
        child3.putEDBObjectEntry("id", "child3");

        when(mockedService.getObject("child1")).thenReturn(child1);
        when(mockedService.getObject("child2")).thenReturn(child2);
        when(mockedService.getObject("child3")).thenReturn(child3);

        // test
        RecursiveModel mRoot = converter.convertEDBObjectToModel(RecursiveModel.class, root);

        // assert
        assertEquals("root", mRoot.getId());

        RecursiveModel mChild1 = mRoot.getChild();
        assertNotNull(mChild1);
        assertEquals("child1", mChild1.getId());

        RecursiveModel mChild2 = mChild1.getChild();
        assertNotNull(mChild2);
        assertEquals("child2", mChild2.getId());

        RecursiveModel mChild3 = mChild2.getChild();
        assertNotNull(mChild3);
        assertEquals("child3", mChild3.getId());
    }

    @Test
    public void testEDBObjectToRecursiveModelWithCompositionConversion_shouldWork() throws Exception {
        // prepare
        EDBObject root = new EDBObject("root");
        root.putEDBObjectEntry(EDBConstants.MODEL_TYPE, RecursiveModel.class.getName());
        root.putEDBObjectEntry(EDBConstants.MODEL_OID, "root");
        root.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(1));
        root.putEDBObjectEntry("id", "root");
        root.putEDBObjectEntry("children.0", "child1");
        root.putEDBObjectEntry("children.1", "child2");

        EDBObject child1 = new EDBObject("child1");
        child1.putEDBObjectEntry(EDBConstants.MODEL_TYPE, RecursiveModel.class.getName());
        child1.putEDBObjectEntry(EDBConstants.MODEL_OID, "child1");
        child1.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(1));
        child1.putEDBObjectEntry("id", "child1");
        child1.putEDBObjectEntry("children.0", "child3");
        child1.putEDBObjectEntry("children.1", "child4");

        EDBObject child2 = new EDBObject("child2");
        child2.putEDBObjectEntry(EDBConstants.MODEL_TYPE, RecursiveModel.class.getName());
        child2.putEDBObjectEntry(EDBConstants.MODEL_OID, "child2");
        child2.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(1));
        child2.putEDBObjectEntry("id", "child2");

        EDBObject child3 = new EDBObject("child3");
        child3.putEDBObjectEntry(EDBConstants.MODEL_TYPE, RecursiveModel.class.getName());
        child3.putEDBObjectEntry(EDBConstants.MODEL_OID, "child3");
        child3.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(1));
        child3.putEDBObjectEntry("id", "child3");

        EDBObject child4 = new EDBObject("child4");
        child4.putEDBObjectEntry(EDBConstants.MODEL_TYPE, RecursiveModel.class.getName());
        child4.putEDBObjectEntry(EDBConstants.MODEL_OID, "child4");
        child4.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(1));
        child4.putEDBObjectEntry("id", "child4");

        when(mockedService.getObject("child1")).thenReturn(child1);
        when(mockedService.getObject("child2")).thenReturn(child2);
        when(mockedService.getObject("child3")).thenReturn(child3);
        when(mockedService.getObject("child4")).thenReturn(child4);

        // test
        RecursiveModel mRoot = converter.convertEDBObjectToModel(RecursiveModel.class, root);

        // assert
        RecursiveModel mChild1;
        RecursiveModel mChild2;
        RecursiveModel mChild3;
        RecursiveModel mChild4;
        List<RecursiveModel> rootChildren;
        List<RecursiveModel> child1Children;

        assertEquals("root", mRoot.getId());
        rootChildren = mRoot.getChildren();
        assertNotNull(rootChildren);
        assertEquals(2, rootChildren.size());

        mChild1 = rootChildren.get(0);
        mChild2 = rootChildren.get(1);

        assertEquals("child1", mChild1.getId());
        assertEquals("child2", mChild2.getId());

        child1Children = mChild1.getChildren();
        assertNotNull(child1Children);
        assertEquals(2, child1Children.size());

        mChild3 = child1Children.get(0);
        mChild4 = child1Children.get(1);

        assertEquals("child3", mChild3.getId());
        assertEquals("child4", mChild4.getId());
    }

    @Test
    public void testIfEngineeringObjectInformationIsAdded_shouldAddEOInformation() throws Exception {
        EngineeringObjectModel model = new EngineeringObjectModel();
        model.setModelAId("testReferenceToModelA");
        model.setModelBId("testReferenceToModelB");

        List<EDBObject> objects = converter.convertModelToEDBObject(model, getTestConnectorInformation());
        assertThat(objects.size(), is(1));
        EDBObject result = objects.get(0);
        String key1 = getReferenceString(model.getClass(), "modelAId");
        String key2 = getReferenceString(model.getClass(), "modelBId");
        assertThat(result.getString(key1), is(contextId + "/testReferenceToModelA"));
        assertThat(result.getString(key2), is(contextId + "/testReferenceToModelB"));
    }

    @Test
    public void testIfEngineeringObjectInformationIsDeleted_shouldDeleteEOInformation() throws Exception {
        EDBObject object = new EDBObject("test");
        object.putEDBObjectEntry(EDBConstants.MODEL_TYPE, EngineeringObjectModel.class.getName());
        object.putEDBObjectEntry(EDBConstants.MODEL_OID, "test");
        object.putEDBObjectEntry(EDBConstants.MODEL_VERSION, Integer.valueOf(1));
        String key1 = getReferenceString(EngineeringObjectModel.class, "modelAId");
        String key2 = getReferenceString(EngineeringObjectModel.class, "modelBId");
        object.putEDBObjectEntry(key1, "testReferenceA");
        object.putEDBObjectEntry(key2, "testReferenceB");
        converter.convertEDBObjectToModel(EngineeringObjectModel.class, object);
        assertThat(object.get(key1), nullValue());
        assertThat(object.get(key2), nullValue());
    }

    private String getReferenceString(Class<?> model, String field) throws Exception {
        return EDBConverterUtils.getEOReferenceStringFromAnnotation(model.getDeclaredField(field).getAnnotation(
                OpenEngSBForeignKey.class));
    }

    private ConnectorInformation getTestConnectorInformation() {
        return new ConnectorInformation("testdomain", "testconnector", "testinstance");
    }
}
