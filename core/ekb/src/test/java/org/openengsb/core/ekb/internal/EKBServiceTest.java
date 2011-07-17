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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.api.edb.EngineeringDatabaseService;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.ekb.internal.TestModel2.ENUM;

public class EKBServiceTest {
    private EKBService service;
    private EngineeringDatabaseService edbService;

    @Before
    public void setup() {
        this.service = new EKBService();

        edbService = mock(EngineeringDatabaseService.class);

        EDBObject edbObject = new EDBObject("testoid");
        edbObject.put("id", "testid");
        edbObject.put("date", new Date());
        edbObject.put("name", "testname");
        edbObject.put("enumeration", "A");
        edbObject.put("list0", "blub");
        edbObject.put("list1", "blab");
        edbObject.put("list2", "blob");
        edbObject.put("sub.id", "testid");
        edbObject.put("sub.value", "testvalue");
        edbObject.put("subs0.id", "AAAAA");
        edbObject.put("subs0.value", "BBBBB");
        edbObject.put("subs1.id", "CCCCC");
        edbObject.put("subs1.value", "DDDDD");

        when(edbService.getObject("testoid")).thenReturn(edbObject);

        this.service.setEdbService(edbService);
    }

    @Test
    public void testSetterAndGetterWithStringObject_shouldWork() {
        TestModel model = service.createEmptyModelObject(TestModel.class);
        model.setId("testId");

        assertThat(model.getId(), is("testId"));
    }

    @Test
    public void testSetterAndGetterWithDateObject_shouldWork() {
        TestModel model = service.createEmptyModelObject(TestModel.class);
        Date date = new Date();
        model.setDate(date);

        assertThat(model.getDate(), is(date));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotGetterOrSetterMethod_shouldThrowException() {
        TestModel model = service.createEmptyModelObject(TestModel.class);
        model.testMethod();
    }

    @Test
    public void testGetOpenEngSBModelEntries_shouldWork() {
        TestModel model = service.createEmptyModelObject(TestModel.class);
        String id = "testId";
        model.setId(id);
        List<OpenEngSBModelEntry> entries = model.getOpenEngSBModelEntries();

        boolean idExisting = false;
        String tempId = null;

        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals("id")) {
                idExisting = true;
                tempId = (String) entry.getValue();
            }
        }
        assertThat(idExisting, is(true));
        assertThat(tempId, is(id));
    }

    @Test
    public void testGetOpenEngSBModelEntriesNonSimpleObject_shouldWork() {
        TestModel model = service.createEmptyModelObject(TestModel.class);
        Date date = new Date();
        model.setDate(date);

        boolean dateExisting = false;
        Date tempDate = null;

        for (OpenEngSBModelEntry entry : model.getOpenEngSBModelEntries()) {
            if (entry.getKey().equals("date")) {
                dateExisting = true;
                tempDate = (Date) entry.getValue();
            }
        }

        assertThat(dateExisting, is(true));
        assertThat(tempDate, is(date));
    }

    @Test
    public void testGetOpenEngSBModelEntriesListObjects_shouldWork() {
        List<String> test = Arrays.asList("string1", "string2", "string3");
        OpenEngSBModelEntry entry = new OpenEngSBModelEntry("list", test, test.getClass());
        TestModel model = service.createEmptyModelObject(TestModel.class, entry);

        boolean stringEntry1 = false;
        boolean stringEntry2 = false;
        boolean stringEntry3 = false;

        for (OpenEngSBModelEntry e : model.getOpenEngSBModelEntries()) {
            if (e.getKey().equals("list0") && e.getValue().equals("string1")) {
                stringEntry1 = true;
            }
            if (e.getKey().equals("list1") && e.getValue().equals("string2")) {
                stringEntry2 = true;
            }
            if (e.getKey().equals("list2") && e.getValue().equals("string3")) {
                stringEntry3 = true;
            }
        }

        assertThat(stringEntry1, is(true));
        assertThat(stringEntry2, is(true));
        assertThat(stringEntry3, is(true));
    }
    
    @Test
    public void testGetOpenEngSBModelEntriesEnumObjects_shouldWork() {
        ENUM enumeration = ENUM.A;
        OpenEngSBModelEntry entry = new OpenEngSBModelEntry("enumeration", enumeration, ENUM.class);
        TestModel model = service.createEmptyModelObject(TestModel.class, entry);

        boolean enumEntry = false;

        for (OpenEngSBModelEntry e : model.getOpenEngSBModelEntries()) {
            if (e.getKey().equals("enumeration") && e.getValue().equals("A")) {
                enumEntry = true;
            }
        }

        assertThat(enumEntry, is(true));
    }

    @Test
    public void testGetOpenEngSBModelEntriesWhichWerentSettet_shouldWork() {
        TestModel model = service.createEmptyModelObject(TestModel.class);

        List<OpenEngSBModelEntry> entries = model.getOpenEngSBModelEntries();

        // 6 because the model define 6 simple fields
        assertThat(entries.size(), is(6));
    }

    @Test
    public void testGetOpenEngSBModelEntriesForListElementsWithProxiedInterface_shouldWork() {
        TestModel model = service.getModel(TestModel.class, "testoid");

        List<OpenEngSBModelEntry> entries = model.getOpenEngSBModelEntries();

        boolean listValue1 = false;
        boolean listValue2 = false;
        boolean listValue3 = false;

        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals("list0") && entry.getValue().equals("blub")) {
                listValue1 = true;
            }
            if (entry.getKey().equals("list1") && entry.getValue().equals("blab")) {
                listValue2 = true;
            }
            if (entry.getKey().equals("list2") && entry.getValue().equals("blob")) {
                listValue3 = true;
            }
        }

        assertThat(listValue1, is(true));
        assertThat(listValue2, is(true));
        assertThat(listValue3, is(true));
    }

    @Test
    public void testGetOpenEngSBModelEntriesForComplexElementWithProxiedInterface_shouldWork() {
        TestModel model = service.getModel(TestModel.class, "testoid");

        List<OpenEngSBModelEntry> entries = model.getOpenEngSBModelEntries();

        boolean subValue1 = false;
        boolean subValue2 = false;

        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals("sub.id") && entry.getValue().equals("testid")) {
                subValue1 = true;
            }
            if (entry.getKey().equals("sub.value") && entry.getValue().equals("testvalue")) {
                subValue2 = true;
            }
        }

        assertThat(subValue1, is(true));
        assertThat(subValue2, is(true));
    }

    @Test
    public void testGetOpenEngSBModelEntriesForListOfComplexElementsWithProxiedInterface_shouldWork() {
        TestModel model = service.getModel(TestModel.class, "testoid");

        List<OpenEngSBModelEntry> entries = model.getOpenEngSBModelEntries();

        boolean subValue1 = false;
        boolean subValue2 = false;
        boolean subValue3 = false;
        boolean subValue4 = false;

        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals("subs0.id") && entry.getValue().equals("AAAAA")) {
                subValue1 = true;
            }
            if (entry.getKey().equals("subs0.value") && entry.getValue().equals("BBBBB")) {
                subValue2 = true;
            }
            if (entry.getKey().equals("subs1.id") && entry.getValue().equals("CCCCC")) {
                subValue3 = true;
            }
            if (entry.getKey().equals("subs1.value") && entry.getValue().equals("DDDDD")) {
                subValue4 = true;
            }
        }

        assertThat(subValue1, is(true));
        assertThat(subValue2, is(true));
        assertThat(subValue3, is(true));
        assertThat(subValue4, is(true));
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

    @Test
    public void testCreateProxyableObject_shouldWork() {
        TestModel model = service.createEmptyModelObject(TestModel.class);

        SubModel sub = service.createEKBProxyableObject(SubModel.class);
        sub.setId("testid");
        sub.setValue("testvalue");

        model.setSub(sub);

        List<OpenEngSBModelEntry> entries = model.getOpenEngSBModelEntries();

        boolean subValue1 = false;
        boolean subValue2 = false;

        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals("sub.id") && entry.getValue().equals("testid")) {
                subValue1 = true;
            }
            if (entry.getKey().equals("sub.value") && entry.getValue().equals("testvalue")) {
                subValue2 = true;
            }
        }

        assertThat(subValue1, is(true));
        assertThat(subValue2, is(true));
    }
}
