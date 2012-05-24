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

package org.openengsb.core.weaver.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.openengsb.core.api.edb.EDBConstants;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.common.util.ModelUtils;

public class ManipulationUtilsTest {

    @Test
    public void testIfModelsGetEnhanced_shouldWork() {
        TestModel model = new TestModel();
        assertThat("The tests aren't started with the model agent", model instanceof OpenEngSBModel, is(true));
    }

    @Test
    public void testIfGetOpenEngSBModelEntriesWork_shouldWork() {
        TestModel model = new TestModel();
        model.setId("testId");
        model.setName("testName");
        List<OpenEngSBModelEntry> entries = ModelUtils.getOpenEngSBModelEntries(model);
        String id = null;
        String name = null;
        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals("id")) {
                id = (String) entry.getValue();
            } else if (entry.getKey().equals("name")) {
                name = (String) entry.getValue();
            }
        }
        assertThat(id, is(model.getId()));
        assertThat(name, is(model.getName()));
    }

    @Test
    public void testIfAddOpenEngSBModelEntryWork_shouldWork() {
        TestModel model = new TestModel();
        OpenEngSBModel bla = (OpenEngSBModel) model;

        ModelUtils.addOpenEngSBModelEntry(bla, new OpenEngSBModelEntry("test", "test", String.class));

        List<OpenEngSBModelEntry> entries = bla.getOpenEngSBModelEntries();
        String test = null;
        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals("test")) {
                test = (String) entry.getValue();
            }
        }
        assertThat(test, is("test"));
    }

    @Test
    public void testIfRemoveOpenEngSBModelEntryWork_shouldWork() {
        TestModel model = new TestModel();
        ModelUtils.addOpenEngSBModelEntry(model, new OpenEngSBModelEntry("test", "test", String.class));
        ModelUtils.removeOpenEngSBModelEntry(model, "test");
        assertThat(ModelUtils.getOpenEngSBModelEntries(model).size(), is(2));
    }

    @Test
    public void testIfModelIdInsertionWorks_shouldWork() {
        TestModel model = new TestModel();
        model.setId("id");
        String id = null;
        for (OpenEngSBModelEntry entry : ModelUtils.getOpenEngSBModelEntries(model)) {
            if (entry.getKey().equals(EDBConstants.MODEL_OID)) {
                id = (String) entry.getValue();
            }
        }
        assertThat(id, is(model.getId()));
    }
}
