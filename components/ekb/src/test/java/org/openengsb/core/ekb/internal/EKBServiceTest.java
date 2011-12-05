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

package org.openengsb.core.ekb.internal;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.api.edb.EngineeringDatabaseService;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.OpenEngSBModelWrapper;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.core.ekb.internal.TestModel2.ENUM;

public class EKBServiceTest {
    private QueryInterfaceService service;

    @Before
    public void setup() {
        this.service = new QueryInterfaceService();
        EngineeringDatabaseService edbService = mock(EngineeringDatabaseService.class);

        EDBObject edbObject = new EDBObject("testoid");
        edbObject.put("id", "testid");
        edbObject.put("date", new Date());
        edbObject.put("name", "testname");
        edbObject.put("enumeration", "A");
        edbObject.put("list0", "blub");
        edbObject.put("list1", "blab");
        edbObject.put("list2", "blob");
        edbObject.put("sub", "suboid1");
        edbObject.put("subs0", "suboid2");
        edbObject.put("subs1", "suboid3");

        EDBObject subObject1 = new EDBObject("suboid1");
        subObject1.put("id", "testid");
        subObject1.put("value", "testvalue");

        EDBObject subObject2 = new EDBObject("suboid2");
        subObject2.put("id", "AAAAA");
        subObject2.put("value", "BBBBB");

        EDBObject subObject3 = new EDBObject("suboid3");
        subObject3.put("id", "CCCCC");
        subObject3.put("value", "DDDDD");

        when(edbService.getObject("testoid")).thenReturn(edbObject);
        when(edbService.getObject("suboid1")).thenReturn(subObject1);
        when(edbService.getObject("suboid2")).thenReturn(subObject2);
        when(edbService.getObject("suboid3")).thenReturn(subObject3);

        service.setEdbService(edbService);
    }

    @Test
    public void testGetOpenEngSBModelGeneral_shouldWork() {
        TestModel model = service.getModel(TestModel.class, "testoid");
        TestModel model2 =
            ModelUtils.createEmptyModelObject(TestModel.class,
                model.getOpenEngSBModelEntries().toArray(new OpenEngSBModelEntry[0]));

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
        assertThat(model.toString().equals(model2.toString()), is(true));
    }

    @Test
    public void testGetOpenEngSBModelEntriesForComplexElementWithProxiedInterface_shouldWork() {
        TestModel model = service.getModel(TestModel.class, "testoid");

        List<OpenEngSBModelEntry> entries = model.getOpenEngSBModelEntries();

        SubModel sub = model.getSub();

        boolean subValue = false;

        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals("sub")) {
                OpenEngSBModelWrapper wrapper = (OpenEngSBModelWrapper) entry.getValue();
                SubModel s = ModelUtils.generateModelOutOfWrapper(wrapper, SubModel.class);
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
    public void testGetOpenEngSBModelEntriesForListOfComplexElementsWithProxiedInterface_shouldWork() {
        TestModel model = service.getModel(TestModel.class, "testoid");

        List<OpenEngSBModelEntry> entries = model.getOpenEngSBModelEntries();

        SubModel subModel1 = null;
        SubModel subModel2 = null;

        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals("subs")) {
                @SuppressWarnings("unchecked")
                List<OpenEngSBModelWrapper> subModels = (List<OpenEngSBModelWrapper>) entry.getValue();
                
                subModel1 = ModelUtils.generateModelOutOfWrapper(subModels.get(0), SubModel.class);
                subModel2 = ModelUtils.generateModelOutOfWrapper(subModels.get(1), SubModel.class);
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
    public void testGetModelWithProxiedInterface_shouldWork() {
        TestModel model = service.getModel(TestModel.class, "testoid");

        assertThat(model.getName(), is("testname"));
        assertThat(model.getId(), is("testid"));
        assertThat(model.getDate(), instanceOf(Date.class));
        assertThat(model.getEnumeration(), is(ENUM.A));
    }

    @Test
    public void testGetModelWithImplementedClass_shouldWork() {
        TestModel2 model = service.getModel(TestModel2.class, "testoid");

        assertThat(model.getName(), is("testname"));
        assertThat(model.getId(), is("testid"));
        assertThat(model.getDate(), instanceOf(Date.class));
        assertThat(model.getEnumeration(), is(ENUM.A));
    }

    @Test
    public void testListAsParameterWithImplementedClass_shouldWork() {
        TestModel2 model = service.getModel(TestModel2.class, "testoid");

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
    public void testListAsParameterWithProxiedInterface_shouldWork() {
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
    public void testComplexAsParameterWithImplementedClass_shouldWork() {
        TestModel2 model = service.getModel(TestModel2.class, "testoid");

        SubModel sub = model.getSub();

        assertThat(sub.getId(), is("testid"));
        assertThat(sub.getValue(), is("testvalue"));

        sub.setId("blabla");
        sub.setValue("blublub");

        assertThat(sub.getId(), is("blabla"));
        assertThat(sub.getValue(), is("blublub"));
    }

    @Test
    public void testListOfComplexAsParameterWithImplementedClass_shouldWork() {
        TestModel2 model = service.getModel(TestModel2.class, "testoid");

        List<SubModel> sub = model.getSubs();

        assertThat(sub.get(0).getId(), is("AAAAA"));
        assertThat(sub.get(0).getValue(), is("BBBBB"));
        assertThat(sub.get(1).getId(), is("CCCCC"));
        assertThat(sub.get(1).getValue(), is("DDDDD"));
    }

    @Test
    public void testListOfComplexAsParameterWithProxiedInterface_shouldWork() {
        TestModel model = service.getModel(TestModel.class, "testoid");

        List<SubModel> sub = model.getSubs();

        assertThat(sub.get(0).getId(), is("AAAAA"));
        assertThat(sub.get(0).getValue(), is("BBBBB"));
        assertThat(sub.get(1).getId(), is("CCCCC"));
        assertThat(sub.get(1).getValue(), is("DDDDD"));
    }

    @Test
    public void testComplexAsParameterWithProxiedInterface_shouldWork() {
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
    public void testInteractionWithEnumValuesWithRealImplementation_shouldWork() {
        TestModel2 model = service.getModel(TestModel2.class, "testoid");

        ENUM temp = model.getEnumeration();
        model.setEnumeration(ENUM.B);

        assertThat(temp, is(ENUM.A));
        assertThat(model.getEnumeration(), is(ENUM.B));
    }

    @Test
    public void testInteractionWithEnumValuesWithProxiedInterface_shouldWork() {
        TestModel model = service.getModel(TestModel.class, "testoid");

        ENUM temp = model.getEnumeration();
        model.setEnumeration(ENUM.B);

        assertThat(temp, is(ENUM.A));
        assertThat(model.getEnumeration(), is(ENUM.B));
    }

    @Test
    public void testGetModelProxiedInterfaceReturnsReallyAllValues_shouldWork() {
        TestModel model = service.getModel(TestModel.class, "testoid");

        List<OpenEngSBModelEntry> entries = model.getOpenEngSBModelEntries();

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
}
