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

package org.openengsb.core.ekb.persistence.query.edb;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.model.ModelWrapper;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.QueryParser;
import org.openengsb.core.ekb.common.EDBConverter;
import org.openengsb.core.ekb.persistence.query.edb.internal.DefaultQueryParser;
import org.openengsb.core.ekb.persistence.query.edb.internal.QueryInterfaceService;
import org.openengsb.core.ekb.persistence.query.edb.models.SubModel;
import org.openengsb.core.ekb.persistence.query.edb.models.TestModel;
import org.openengsb.core.ekb.persistence.query.edb.models.TestModel2;
import org.openengsb.core.ekb.persistence.query.edb.models.TestModel2.ENUM;
import org.openengsb.core.util.ModelUtils;

public class QueryInterfaceServiceTest {
    private QueryInterfaceService service;

    @Before
    public void setup() {
        this.service = new QueryInterfaceService();
        EngineeringDatabaseService edbService = mock(EngineeringDatabaseService.class);

        EDBObject edbObject = new EDBObject("testoid");
        edbObject.putEDBObjectEntry("id", "testid");
        edbObject.putEDBObjectEntry("date", new Date());
        edbObject.putEDBObjectEntry("name", "testname");
        edbObject.putEDBObjectEntry("enumeration", "A", ENUM.class);
        edbObject.putEDBObjectEntry(EDBConverter.getEntryNameForList("list", 0), "blub");
        edbObject.putEDBObjectEntry(EDBConverter.getEntryNameForList("list", 1), "blab");
        edbObject.putEDBObjectEntry(EDBConverter.getEntryNameForList("list", 2), "blob");
        edbObject.putEDBObjectEntry("sub", "suboid1", SubModel.class);
        edbObject.putEDBObjectEntry(EDBConverter.getEntryNameForList("subs", 0), "suboid2", SubModel.class);
        edbObject.putEDBObjectEntry(EDBConverter.getEntryNameForList("subs", 1), "suboid3", SubModel.class);
        edbObject.putEDBObjectEntry("number", Integer.valueOf(42));
        edbObject.putEDBObjectEntry(EDBConstants.MODEL_TYPE, TestModel.class.getName());

        EDBObject edbObjectImpl = new EDBObject("testoidimpl");
        edbObjectImpl.putEDBObjectEntry("id", "testid");
        edbObjectImpl.putEDBObjectEntry("date", new Date());
        edbObjectImpl.putEDBObjectEntry("name", "testname");
        edbObjectImpl.putEDBObjectEntry("enumeration", "A", ENUM.class);
        edbObjectImpl.putEDBObjectEntry(EDBConverter.getEntryNameForList("list", 0), "blub");
        edbObjectImpl.putEDBObjectEntry(EDBConverter.getEntryNameForList("list", 1), "blab");
        edbObjectImpl.putEDBObjectEntry(EDBConverter.getEntryNameForList("list", 2), "blob");
        edbObjectImpl.putEDBObjectEntry("sub", "suboid1", SubModel.class);
        edbObjectImpl.putEDBObjectEntry(EDBConverter.getEntryNameForList("subs", 0), "suboid2", SubModel.class);
        edbObjectImpl.putEDBObjectEntry(EDBConverter.getEntryNameForList("subs", 1), "suboid3", SubModel.class);
        edbObjectImpl.putEDBObjectEntry(EDBConstants.MODEL_TYPE, TestModel2.class.getName());

        EDBObject mapTest = new EDBObject("mapoid");
        mapTest.putEDBObjectEntry("id", "testid");
        mapTest.putEDBObjectEntry(EDBConverter.getEntryNameForMapKey("map", 0), "keyA");
        mapTest.putEDBObjectEntry(EDBConverter.getEntryNameForMapValue("map", 0), "valueA");
        mapTest.putEDBObjectEntry(EDBConverter.getEntryNameForMapKey("map", 1), "keyB");
        mapTest.putEDBObjectEntry(EDBConverter.getEntryNameForMapValue("map", 1), "valueB");
        mapTest.putEDBObjectEntry(EDBConverter.getEntryNameForMapKey("map", 2), "keyC");
        mapTest.putEDBObjectEntry(EDBConverter.getEntryNameForMapValue("map", 2), "valueC");
        mapTest.putEDBObjectEntry("number", Integer.valueOf(42));
        mapTest.putEDBObjectEntry(EDBConstants.MODEL_TYPE, TestModel.class.getName());

        EDBObject subObject1 = new EDBObject("suboid1");
        subObject1.putEDBObjectEntry("id", "testid");
        subObject1.putEDBObjectEntry("value", "testvalue");
        subObject1.putEDBObjectEntry(EDBConstants.MODEL_TYPE, SubModel.class.getName());

        EDBObject subObject2 = new EDBObject("suboid2");
        subObject2.putEDBObjectEntry("id", "AAAAA");
        subObject2.putEDBObjectEntry("value", "BBBBB");
        subObject2.putEDBObjectEntry(EDBConstants.MODEL_TYPE, SubModel.class.getName());

        EDBObject subObject3 = new EDBObject("suboid3");
        subObject3.putEDBObjectEntry("id", "CCCCC");
        subObject3.putEDBObjectEntry("value", "DDDDD");
        subObject3.putEDBObjectEntry(EDBConstants.MODEL_TYPE, SubModel.class.getName());

        when(edbService.getObject(eq("testoid"), anyLong())).thenReturn(edbObject);
        when(edbService.getObject(eq("testoidimpl"), anyLong())).thenReturn(edbObjectImpl);
        when(edbService.getObject(eq("mapoid"), anyLong())).thenReturn(mapTest);
        when(edbService.getObject(eq("suboid1"), anyLong())).thenReturn(subObject1);
        when(edbService.getObject(eq("suboid2"), anyLong())).thenReturn(subObject2);
        when(edbService.getObject(eq("suboid3"), anyLong())).thenReturn(subObject3);
        when(edbService.getObject("testoid")).thenReturn(edbObject);
        when(edbService.getObject("testoidimpl")).thenReturn(edbObjectImpl);
        when(edbService.getObject("mapoid")).thenReturn(mapTest);
        when(edbService.getObject("suboid1")).thenReturn(subObject1);
        when(edbService.getObject("suboid2")).thenReturn(subObject2);
        when(edbService.getObject("suboid3")).thenReturn(subObject3);

        service.setEdbService(edbService);
        service.setEdbConverter(new EDBConverter(edbService));
        List<QueryParser> parsers = new ArrayList<>();
        parsers.add(new DefaultQueryParser());
        service.setQueryParsers(parsers);
    }

    @Test
    public void testIfModelAgentIsSet_shouldWork() throws Exception {
        TestModel model = new TestModel();
        assertThat("TestModel isn't enhanced. Maybe you forgot to set the java agent?",
                ModelWrapper.isModel(model.getClass()), is(true));
    }

    @Test
    public void testGetOpenEngSBModelGeneral_shouldWork() throws Exception {
        TestModel model = service.getModel(TestModel.class, "testoid");
        TestModel model2 = ModelUtils.createModel(TestModel.class, ModelWrapper.wrap(model).toOpenEngSBModelEntries());

        assertThat(model.getId().equals(model2.getId()), is(true));
        assertThat(model.getDate().equals(model2.getDate()), is(true));
        assertThat(model.getEnumeration().equals(model2.getEnumeration()), is(true));
        assertThat(model.getName().equals(model2.getName()), is(true));
        assertThat(model.getSub().toString().equals(model2.getSub().toString()), is(true));
        List<SubModel> list = model.getSubs();
        List<SubModel> list2 = model2.getSubs();
        for (int i = 0; i < list.size(); i++) {
            assertThat(list.get(i).toString().equals(list2.get(i).toString()), is(true));
        }
    }

    @Test
    public void testGetOpenEngSBModelEntriesForComplexElementWithProxiedInterface_shouldWork() throws Exception {
        TestModel model = service.getModel(TestModel.class, "testoid");
        List<OpenEngSBModelEntry> entries = ModelWrapper.wrap(model).toOpenEngSBModelEntries();
        SubModel sub = model.getSub();
        boolean subValue = false;

        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals("sub")) {
                SubModel s = (SubModel) entry.getValue();
                if (s.getId().equals(sub.getId()) && s.getValue().equals(sub.getValue())) {
                    subValue = true;
                }
            }
        }

        assertThat(subValue, is(true));
        assertThat(sub.getId(), is("testid"));
        assertThat(sub.getValue(), is("testvalue"));
    }

    @Test
    public void testGetOpenEngSBModelEntriesForListOfComplexElementsWithProxiedInterface_shouldWork() throws Exception {
        TestModel model = service.getModel(TestModel.class, "testoid");
        List<OpenEngSBModelEntry> entries = ModelWrapper.wrap(model).toOpenEngSBModelEntries();

        SubModel subModel1 = null;
        SubModel subModel2 = null;

        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals("subs")) {
                @SuppressWarnings("unchecked")
                List<SubModel> subModels = (List<SubModel>) entry.getValue();
                subModel1 = subModels.get(0);
                subModel2 = subModels.get(1);
            }
        }

        assertThat(subModel1, notNullValue());
        assertThat(subModel2, notNullValue());
        assertThat(subModel1.getId(), is("AAAAA"));
        assertThat(subModel1.getValue(), is("BBBBB"));
        assertThat(subModel2.getId(), is("CCCCC"));
        assertThat(subModel2.getValue(), is("DDDDD"));
    }

    @Test
    public void testGetModelWithProxiedInterface_shouldWork() throws Exception {
        TestModel model = service.getModel(TestModel.class, "testoid");

        assertThat(model.getName(), is("testname"));
        assertThat(model.getId(), is("testid"));
        assertThat(model.getDate(), instanceOf(Date.class));
        assertThat(model.getEnumeration(), is(ENUM.A));
    }

    @Test
    public void testGetModelWithImplementedClass_shouldWork() throws Exception {
        TestModel2 model = service.getModel(TestModel2.class, "testoidimpl");

        assertThat(model.getName(), is("testname"));
        assertThat(model.getId(), is("testid"));
        assertThat(model.getDate(), instanceOf(Date.class));
        assertThat(model.getEnumeration(), is(ENUM.A));
    }

    @Test
    public void testListAsParameterWithImplementedClass_shouldWork() throws Exception {
        TestModel2 model = service.getModel(TestModel2.class, "testoidimpl");
        List<String> testList = model.getList();
        assertThat(testList.size(), is(3));
        assertThat(testList.get(0), is("blub"));
        assertThat(testList.get(1), is("blab"));
        assertThat(testList.get(2), is("blob"));

        List<String> temp = new ArrayList<String>();
        temp.add("test1");
        temp.add("test2");
        temp.add("test3");
        model.setList(temp);
        testList = model.getList();

        assertThat(testList.size(), is(3));
        assertThat(testList.get(0), is("test1"));
        assertThat(testList.get(1), is("test2"));
        assertThat(testList.get(2), is("test3"));
    }

    @Test
    public void testListAsParameterWithProxiedInterface_shouldWork() throws Exception {
        TestModel model = service.getModel(TestModel.class, "testoid");

        List<String> testList = model.getList();
        assertThat(testList.size(), is(3));
        assertThat(testList.get(0), is("blub"));
        assertThat(testList.get(1), is("blab"));
        assertThat(testList.get(2), is("blob"));

        List<String> temp = new ArrayList<String>();
        temp.add("test1");
        temp.add("test2");
        temp.add("test3");
        model.setList(temp);
        testList = model.getList();
        assertThat(testList.size(), is(3));
        assertThat(testList.get(0), is("test1"));
        assertThat(testList.get(1), is("test2"));
        assertThat(testList.get(2), is("test3"));
    }

    @Test
    public void testComplexAsParameterWithImplementedClass_shouldWork() throws Exception {
        TestModel2 model = service.getModel(TestModel2.class, "testoidimpl");
        SubModel sub = model.getSub();
        assertThat(sub.getId(), is("testid"));
        assertThat(sub.getValue(), is("testvalue"));

        sub.setId("blabla");
        sub.setValue("blublub");
        assertThat(sub.getId(), is("blabla"));
        assertThat(sub.getValue(), is("blublub"));
    }

    @Test
    public void testListOfComplexAsParameterWithImplementedClass_shouldWork() throws Exception {
        TestModel2 model = service.getModel(TestModel2.class, "testoidimpl");

        List<SubModel> sub = model.getSubs();
        assertThat(sub.get(0).getId(), is("AAAAA"));
        assertThat(sub.get(0).getValue(), is("BBBBB"));
        assertThat(sub.get(1).getId(), is("CCCCC"));
        assertThat(sub.get(1).getValue(), is("DDDDD"));
    }

    @Test
    public void testListOfComplexAsParameterWithProxiedInterface_shouldWork() throws Exception {
        TestModel model = service.getModel(TestModel.class, "testoid");

        List<SubModel> sub = model.getSubs();
        assertThat(sub.get(0).getId(), is("AAAAA"));
        assertThat(sub.get(0).getValue(), is("BBBBB"));
        assertThat(sub.get(1).getId(), is("CCCCC"));
        assertThat(sub.get(1).getValue(), is("DDDDD"));
    }

    @Test
    public void testComplexAsParameterWithProxiedInterface_shouldWork() throws Exception {
        TestModel model = service.getModel(TestModel.class, "testoid");

        SubModel sub = model.getSub();
        assertThat(sub.getId(), is("testid"));
        assertThat(sub.getValue(), is("testvalue"));

        sub.setId("blabla");
        sub.setValue("blublub");
        assertThat(sub.getId(), is("blabla"));
        assertThat(sub.getValue(), is("blublub"));
    }

    @Test
    public void testInteractionWithEnumValuesWithRealImplementation_shouldWork() throws Exception {
        TestModel2 model = service.getModel(TestModel2.class, "testoidimpl");

        ENUM temp = model.getEnumeration();
        model.setEnumeration(ENUM.B);
        assertThat(temp, is(ENUM.A));
        assertThat(model.getEnumeration(), is(ENUM.B));
    }

    @Test
    public void testInteractionWithEnumValuesWithProxiedInterface_shouldWork() throws Exception {
        TestModel model = service.getModel(TestModel.class, "testoid");

        ENUM temp = model.getEnumeration();
        model.setEnumeration(ENUM.B);
        assertThat(temp, is(ENUM.A));
        assertThat(model.getEnumeration(), is(ENUM.B));
    }

    @Test
    public void testGetModelProxiedInterfaceReturnsReallyAllValues_shouldWork() throws Exception {
        TestModel model = service.getModel(TestModel.class, "testoid");
        List<OpenEngSBModelEntry> entries = ModelWrapper.wrap(model).toOpenEngSBModelEntries();

        boolean testExists = false;
        Object testValue = null;
        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals("test")) {
                testExists = true;
                testValue = entry.getValue();
            }
        }

        assertThat(testExists, is(true));
        assertThat(testValue, nullValue());
    }

    @Test
    public void testLoadEDBObjectWithWrongModel_shouldReturnNull() throws Exception {
        // testoidimpl returns a TestModel2 object
        TestModel model = service.getModel(TestModel.class, "testoidimpl");
        assertThat(model, nullValue());
    }

    @Test
    public void testMapSupportOfProxiedInterface_shouldWork() throws Exception {
        TestModel model = service.getModel(TestModel.class, "mapoid");
        assertThat(model.getMap().get("keyA").toString(), is("valueA"));
        assertThat(model.getMap().get("keyB").toString(), is("valueB"));
        assertThat(model.getMap().get("keyC").toString(), is("valueC"));
    }

}
