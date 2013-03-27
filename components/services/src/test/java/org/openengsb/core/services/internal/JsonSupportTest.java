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

package org.openengsb.core.services.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.services.internal.model.NullModel;
import org.openengsb.core.services.internal.model.SubModel;
import org.openengsb.core.util.JsonUtils;
import org.openengsb.core.util.ModelUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSupportTest {
    private ObjectMapper mapper;

    public JsonSupportTest() {
        mapper = new ObjectMapper();
    }

    private NullModel createTestModel() {
        NullModel model = new NullModel();
        model.setId(20);
        model.setValue("test");
        SubModel sub1 = new SubModel();
        sub1.setId("sub1");
        sub1.setName("test1");
        SubModel sub2 = new SubModel();
        sub2.setId("sub2");
        sub2.setName("test2");
        model.setSubs(Arrays.asList(sub1, sub2));

        ModelUtils.addOpenEngSBModelEntry(model, new OpenEngSBModelEntry("test", "test", String.class));
        ModelUtils.addOpenEngSBModelEntry(model, new OpenEngSBModelEntry("test2", "test2", String.class));
        return model;
    }

    @Test(expected = IOException.class)
    public void tryConvertJSONIntoModelWithoutAdditionalSerializer_shouldThrowException() throws Exception {
        NullModel model = createTestModel();
        String result = mapper.writeValueAsString(model);
        // the read value call without the additional serialzer throws an IOException because it can not
        // deserialize the model tail
        mapper.readValue(result, NullModel.class);
    }

    @Test
    public void tryConvertJSONIntoModelWithAdditionalSerializer_shouldThrowNoException() throws Exception {
        NullModel model = createTestModel();
        ModelUtils.addOpenEngSBModelEntry(model, new OpenEngSBModelEntry("number", 42, Integer.class));
        String result = mapper.writeValueAsString(model);

        NullModel other = JsonUtils.convertObject(result, NullModel.class);
        List<OpenEngSBModelEntry> entries = ModelUtils.getOpenEngSBModelTail(other);
        assertThat(model.getId(), is(other.getId()));
        assertThat(model.getValue(), is(other.getValue()));
        assertThat(model.getSubs().size(), is(other.getSubs().size()));
        assertThat(model.getSubs().get(0).getId(), is(other.getSubs().get(0).getId()));
        assertThat(model.getSubs().get(0).getName(), is(other.getSubs().get(0).getName()));
        assertThat(model.getSubs().get(1).getId(), is(other.getSubs().get(1).getId()));
        assertThat(model.getSubs().get(1).getName(), is(other.getSubs().get(1).getName()));
        assertThat(entries.size(), is(3));
        assertThat(entries.contains(new OpenEngSBModelEntry("test", "test", String.class)), is(true));
        assertThat(entries.contains(new OpenEngSBModelEntry("test2", "test2", String.class)), is(true));
        assertThat(entries.contains(new OpenEngSBModelEntry("number", 42, Integer.class)), is(true));
    }
}
