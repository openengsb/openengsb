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

package org.openengsb.core.weaver.test.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.common.util.ModelUtils;

public class ManipulationUtilsTest {

    @Test
    public void testIfModelAgentIsSet_shouldWork() throws Exception {
        TestModel model = new TestModel();
        assertThat("TestModel isn't enhanced. Maybe you forgot to set the java agent?",
            model instanceof OpenEngSBModel, is(true));
    }

    @Test
    public void testIfGetOpenEngSBModelEntriesWork_shouldWork() throws Exception {
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
    public void testIfAddOpenEngSBModelEntryWork_shouldWork() throws Exception {
        TestModel model = new TestModel();
        OpenEngSBModel bla = (OpenEngSBModel) model;

        ModelUtils.addOpenEngSBModelEntry(bla, new OpenEngSBModelEntry("test", "test", String.class));

        List<OpenEngSBModelEntry> entries = bla.toOpenEngSBModelEntries();
        String test = null;
        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals("test")) {
                test = (String) entry.getValue();
            }
        }
        assertThat(test, is("test"));
    }

    @Test
    public void testIfRemoveOpenEngSBModelEntryWork_shouldWork() throws Exception {
        TestModel model = new TestModel();
        ModelUtils.addOpenEngSBModelEntry(model, new OpenEngSBModelEntry("test", "test", String.class));
        ModelUtils.removeOpenEngSBModelEntry(model, "test");
        // the result has two elements even though the model has three properties,
        // since the third property is ignored
        assertThat(ModelUtils.getOpenEngSBModelEntries(model).size(), is(2));
    }

    @Test
    public void testIfModelIdInsertionWorks_shouldWork() throws Exception {
        TestModel model = new TestModel();
        model.setId("id");
        String id = (String) ModelUtils.getInternalModelId(model);
        assertThat(id, is(model.getId()));
    }

    @Test
    public void testIfModelTailRetrievingWorks_shouldWork() throws Exception {
        TestModel model = new TestModel();
        ModelUtils.addOpenEngSBModelEntry(model, new OpenEngSBModelEntry("test", "test", String.class));
        List<OpenEngSBModelEntry> tail = ModelUtils.getOpenEngSBModelTail(model);
        assertThat(tail.size(), is(1));
        assertThat(tail.get(0).getKey(), is("test"));
    }

    @Test
    public void testIfModelTailSettingWorks_shouldWork() throws Exception {
        TestModel model = new TestModel();
        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
        entries.add(new OpenEngSBModelEntry("test", "test", String.class));
        entries.add(new OpenEngSBModelEntry("test2", "test2", String.class));
        entries.add(new OpenEngSBModelEntry("test3", "test3", String.class));
        ModelUtils.setOpenEngSBModelTail(model, entries);
        assertThat(ModelUtils.getOpenEngSBModelTail(model).size(), is(3));
    }

    @Test
    public void testIfNullModelTailSettingWorks_shouldThrowNoException() throws Exception {
        TestModel model = new TestModel();
        ModelUtils.setOpenEngSBModelTail(model, null);
        assertThat(ModelUtils.getOpenEngSBModelTail(model).size(), is(0));
    }

    @Test
    public void testIfPrimitiveFieldsWork_shouldThrowNoException() throws Exception {
        PrimitiveModel model = new PrimitiveModel();
        model.setValue1(5);
        model.setValue2(42L);
        model.setValue3(true);
        List<OpenEngSBModelEntry> entries = ModelUtils.getOpenEngSBModelEntries(model);
        assertThat((Integer) getEntryByName(entries, "value1"), is(5));
        assertThat((Long) getEntryByName(entries, "value2"), is(42L));
        assertThat((Boolean) getEntryByName(entries, "value3"), is(true));
    }
    
    @Test
    public void testIfPrimitiveModelCreationThroughEntriesWork_shouldWork() throws Exception {
        List<OpenEngSBModelEntry> entries = new ArrayList<OpenEngSBModelEntry>();
        entries.add(new OpenEngSBModelEntry("value1", 5, Integer.class));
        entries.add(new OpenEngSBModelEntry("value2", 42L, Long.class));
        entries.add(new OpenEngSBModelEntry("value3", true, Boolean.class));
        PrimitiveModel model = ModelUtils.createModel(PrimitiveModel.class, entries);
        assertThat(model.getValue1(), is(5));
        assertThat(model.getValue2(), is(42L));
        assertThat(model.isValue3(), is(true));
    }
    
    @Test
    public void testIfFieldWithNoGetterGetIgnored_shouldNotBePresentInTheEntries() throws Exception {
        PrimitiveModel model = new PrimitiveModel();
        model.setValue1(5);
        model.setValue2(42L);
        model.setValue3(true);
        List<OpenEngSBModelEntry> entries = ModelUtils.getOpenEngSBModelEntries(model);
        assertThat(getEntryByName(entries, "ignoredField"), nullValue());
    }
    
    @Test
    public void testIfSuperModelsSupportWork_shouldWork() throws Exception {
        ChildModel model = new ChildModel();
        model.setId("testId");
        model.setName("testName");
        model.setChild("testChild");
        List<OpenEngSBModelEntry> entries = ModelUtils.getOpenEngSBModelEntries(model);
        assertThat((String) getEntryByName(entries, "id"), is("testId"));
        assertThat((String) getEntryByName(entries, "name"), is("testName"));
        assertThat((String) getEntryByName(entries, "child"), is("testChild"));
        assertThat((String) ModelUtils.getInternalModelId(model), is("testId"));
    }

    private Object getEntryByName(List<OpenEngSBModelEntry> entries, String property) {
        for (OpenEngSBModelEntry entry : entries) {
            if (entry.getKey().equals(property)) {
                return entry.getValue();
            }
        }
        return null;
    }
}
