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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.ekb.EngineeringKnowlegeBaseService;
import org.openengsb.core.api.model.OpenEngSBModelEntry;

public class EKBServiceTest {
    private EngineeringKnowlegeBaseService service;

    @Before
    public void setup() {
        this.service = new EKBService();
    }

    @Test
    public void testSetterAndGetterWithStringObject_shouldWork() {
        TestModel model = service.createModelObject(TestModel.class);
        model.setId("testId");

        assertThat(model.getId(), is("testId"));
    }

    @Test
    public void testSetterAndGetterWithDateObject_shouldWork() {
        TestModel model = service.createModelObject(TestModel.class);
        Date date = new Date();
        model.setDate(date);

        assertThat(model.getDate(), is(date));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotGetterOrSetterMethod_shouldThrowException() {
        TestModel model = service.createModelObject(TestModel.class);
        model.testMethod();
    }

    @Test
    public void testGetOpenEngSBModelEntries_shouldWork() {
        TestModel model = service.createModelObject(TestModel.class);

        String id = "testId";
        Date date = new Date();

        model.setDate(date);
        model.setId(id);

        List<OpenEngSBModelEntry> entries = model.getOpenEngSBModelEntries();

        boolean idExisting = false;
        boolean dateExisting = false;
        boolean nameExisting = false;
        String tempId = null;
        Date tempDate = null;
        String tempName = null;

        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals("id")) {
                idExisting = true;
                tempId = (String) entry.getValue();
            } else if (entry.getKey().equals("date")) {
                dateExisting = true;
                tempDate = (Date) entry.getValue();
            } else if (entry.getKey().equals("name")) {
                nameExisting = true;
                tempName = (String) entry.getValue();
            }
        }

        assertThat(idExisting, is(true));
        assertThat(dateExisting, is(true));
        assertThat(nameExisting, is(true));

        assertThat(tempId, is(id));
        assertThat(tempDate, is(date));
        assertThat(tempName, nullValue());
    }
}
