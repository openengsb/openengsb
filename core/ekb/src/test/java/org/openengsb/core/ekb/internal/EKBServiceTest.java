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
    public void testGetOpenEngSBModelEntriesWhichWerentSettet_shouldWork() {
        TestModel model = service.createEmptyModelObject(TestModel.class);

        List<OpenEngSBModelEntry> entries = model.getOpenEngSBModelEntries();

        // 8 because the model define 8 fields
        assertThat(entries.size(), is(8));
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
