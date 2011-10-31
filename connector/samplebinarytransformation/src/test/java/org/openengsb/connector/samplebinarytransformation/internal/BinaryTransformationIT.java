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


package org.openengsb.connector.samplebinarytransformation.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.model.OpenEngSBModelEntry;

public class BinaryTransformationIT {

    private SampleBinaryTransformationServiceImpl service;

    @Before
    public void setUp() {
        service = new SampleBinaryTransformationServiceImpl();
        File f = new File("test");
        service.register("testId", TestModel.class, new File[] { f });
    }

    @Test
    public void testTransformationToOpenEngSBModelEntries_shouldWork() {
        TestModel model = new TestModel();
        model.setId(50);
        model.setName("testModel");

        List<OpenEngSBModelEntry> entries = service.convertToOpenEngSBModelEntries("testId", model);

        boolean idExists = false;
        int id = 0;
        boolean nameExists = false;
        String name = "";

        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals("id")) {
                idExists = true;
                id = Integer.parseInt(entry.getValue().toString());
            } else if (entry.getKey().equals("name")) {
                nameExists = true;
                name = entry.getValue().toString();
            }
        }

        assertThat(idExists, is(true));
        assertThat(nameExists, is(true));
        assertThat(id, is(50));
        assertThat(name, is("testModel"));
    }
    
    @Test
    public void testTransformationFromOpenEngSBModelEntries_shouldWork() {
        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
        entries.add(new OpenEngSBModelEntry("id", 50, Integer.class));
        entries.add(new OpenEngSBModelEntry("name", "testModel", String.class));
        
        Object object = service.convertFromOpenEngSBModelEntries("testId", entries);
        TestModel model = (TestModel) object;
        
        assertThat(model.getId(), is(50));
        assertThat(model.getName(), is("testModel"));
    }

}
