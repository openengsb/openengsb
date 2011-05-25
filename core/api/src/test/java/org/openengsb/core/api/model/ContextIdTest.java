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

package org.openengsb.core.api.model;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ContextIdTest {

    @Test
    public void testContextIdConstructor_shouldInitializeId() {
        ContextId contextId = new ContextId("someId");

        assertThat(contextId.getId(), is("someId"));
    }

    @Test
    public void testFromMetaData_shouldProvideTheRightId() {
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put("id", "metaId");

        ContextId convertedContextId = ContextId.fromMetaData(metaData);

        assertThat(convertedContextId.getId(), is("metaId"));
    }

    @Test
    public void testToMetaData_shouldStoreIdInMap() {
        ContextId contextId = new ContextId("idFromContextId");

        Map<String, String> metaData = contextId.toMetaData();

        assertThat(metaData, hasEntry("id", "idFromContextId"));
    }

    @Test
    public void testEquals_shouldBeTrueForEqualIds() {
        ContextId id1 = new ContextId("theId");
        ContextId id2 = new ContextId("theId");

        assertThat(id1.equals(id2), is(true));
        assertThat(id2.equals(id1), is(true));
    }

    @Test
    public void testGetContextWildCard_shouldReturnEmptyMap() {
        assertThat(ContextId.getContextWildCard().isEmpty(), is(true));
    }

}
