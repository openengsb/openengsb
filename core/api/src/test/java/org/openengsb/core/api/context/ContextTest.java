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
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class ContextTest {

    private Context context;

    @Before
    public void setup() {
        context = new Context();
    }

    @Test
    public void emptyContext_shouldHaveNoKeys() {
        assertThat(context.getKeys().size(), is(0));
    }

    @Test
    public void emptyContext_shouldHaveNoChildren() {
        assertThat(context.getChildren().size(), is(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void putKeyWithSlash_shouldThrowIAE() {
        context.put("with/slash", "");
    }

    @Test
    public void putAllowedKey_gettingValue_shouldReturnPuttedValue() {
        context.put("a", "b");
        assertThat(context.get("a"), is("b"));
    }

    @Test(expected = NullPointerException.class)
    public void nullKey_shouldThrowNPE() {
        context.put(null, "");
    }

    @Test(expected = NullPointerException.class)
    public void nullValue_shourdThropNPE() {
        context.put("a", null);
    }

    @Test
    public void getNonExistingKey_shouldReturnNull() {
        assertThat(context.get("non-existing"), nullValue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getKeys_shouldBeUnmodifiable() {
        context.getKeys().clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getChildren_shouldBeUnmodifiable() {
        context.getChildren().clear();
    }

    @Test
    public void getNonExistingChild_shouldReturnNull() {
        assertThat(context.getChild("non-existing"), nullValue());
    }

    @Test
    public void creatingNewChild_shouldReturnNewlyCreatedChild() {
        Context child = context.createChild("a");
        assertThat(context.getChild("a"), is(child));
    }

    @Test(expected = IllegalArgumentException.class)
    public void creatingNewChildWithSlashInName_shouldThrowIAE() {
        context.createChild("with/slash");
    }

    @Test
    public void createChildWithExistingNamedChild_shouldThrowIAE() {
        context.createChild("a");
        try {
            context.createChild("a");
            fail();
        } catch (IllegalArgumentException e) {
            // nop
        }
    }

    @Test
    public void keysAndNames_shouldNotOverlap() {
        context.createChild("a");
        try {
            context.put("a", "");
            fail();
        } catch (IllegalArgumentException e) {
            // nop
        }
    }

    @Test
    public void namesAndKeys_shouldNotOverlap() {
        context.put("a", "");
        try {
            context.createChild("a");
            fail();
        } catch (IllegalArgumentException e) {
            // nop
        }
    }

    @Test
    public void removeOfKeyValue_shouldReturnNullOnGet() {
        context.put("a", "");
        context.remove("a");
        assertThat(context.get("a"), nullValue());
    }

    @Test
    public void removeOfChild_shouldReturnNullOnGetChild() {
        context.createChild("a");
        context.remove("a");
        assertThat(context.getChild("a"), nullValue());
    }
}
