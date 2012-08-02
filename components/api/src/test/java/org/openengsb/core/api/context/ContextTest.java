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

package org.openengsb.core.api.context;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

public class ContextTest {

    private Context context;

    @Before
    public void setup() {
        context = new Context();
    }

    @Test
    public void testEmptyContext_shouldHaveNoKeys() {
        assertThat(context.getKeys().size(), is(0));
    }

    @Test
    public void testEmptyContext_shouldHaveNoChildren() {
        assertThat(context.getChildren().size(), is(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutKeyWithSlash_shouldThrowIllegalArgumentException() {
        context.put("with/slash", "");
    }

    @Test
    public void testGetPuttedValueByKey_shouldReturnPuttedValue() {
        context.put("a", "b");
        assertThat(context.get("a"), is("b"));
    }

    @Test(expected = NullPointerException.class)
    public void testNullKey_shouldThrowNullPointerException() {
        context.put(null, "");
    }

    @Test(expected = NullPointerException.class)
    public void testNullValue_shouldThrowNullPointerException() {
        context.put("a", null);
    }

    @Test
    public void testGetNonExistingKey_shouldReturnNull() {
        assertThat(context.get("non-existing"), nullValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIfGetKeysValuesAreUnmodifiable_shouldBeUnmodifiable() {
        context.getKeys().clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testIfGetChildrenValuesAreUnmodifiable_shouldBeUnmodifiable() {
        context.getChildren().clear();
    }

    @Test
    public void testGetNonExistingChild_shouldReturnNull() {
        assertThat(context.getChild("non-existing"), nullValue());
    }

    @Test
    public void testCreatingNewChild_shouldReturnNewlyCreatedChild() {
        Context child = context.createChild("a");
        assertThat(context.getChild("a"), is(child));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreatingNewChildWithSlashInName_shouldThrowIllegalArgumentException() {
        context.createChild("with/slash");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateChildWithExistingNamedChild_shouldThrowIllegalArgumentException() {
        context.createChild("a");
        context.createChild("a");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testKeysAndNames_shouldNotOverlap() {
        context.createChild("a");
        context.put("a", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNamesAndKeys_shouldNotOverlap() {
        context.put("a", "");
        context.createChild("a");
    }

    @Test
    public void testRemoveOfKeyValue_shouldReturnNullOnGet() {
        context.put("a", "");
        context.remove("a");
        assertThat(context.get("a"), nullValue());
    }

    @Test
    public void testRemoveOfChild_shouldReturnNullOnGetChild() {
        context.createChild("a");
        context.remove("a");
        assertThat(context.getChild("a"), nullValue());
    }
}
