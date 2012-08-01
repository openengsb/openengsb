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

package org.openengsb.core.api.workflow.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.workflow.ProcessBagException;

import com.google.common.collect.Maps;

public class ProcessBagTest {
    private ProcessBag pb;

    @Before
    public void init() throws Exception {
        pb = new ProcessBag();
    }

    @Test(expected = ProcessBagException.class)
    public void testAddProperty_shouldReturnException() {
        pb.addProperty("test", "42");
        pb.addProperty("test", "42");
    }

    @Test
    public void testGetPropertyClass_shouldReturnStringClass() {
        pb.addProperty("test", "42");
        assertTrue(pb.getPropertyClass("test") == String.class);
    }

    @Test
    public void testGetPropertyKeyList_shouldReturnTwo() {
        pb.addProperty("test", "42");
        pb.addProperty("number", 42);

        Set<String> list = pb.propertyKeySet();
        assertThat(list.size(), is(2));
    }

    @Test
    public void testRemoveProperty_shouldContainStringOnly() {
        pb.addProperty("number", 42);
        pb.addProperty("string", "42");
        pb.removeProperty("number");

        Set<String> list = pb.propertyKeySet();
        assertTrue(list.contains("string"));
        assertFalse(list.contains("number"));
    }

    @Test
    public void testGetPropertyCount_shouldReturnTwo() {
        pb.addProperty("test", "42");
        pb.addProperty("number", 42);

        assertTrue(pb.propertyCount() == 2);
    }

    @Test
    public void testAddOrReplaceProperty_shouldOverwriteValue() {
        pb.addProperty("test", "42");
        pb.addOrReplaceProperty("test", "43");
        assertEquals(pb.getProperty("test"), "43");
    }

    @Test
    public void testRemoveAllProperties_shouldDeleteEverything() {
        pb.addProperty("test", "42");
        pb.addProperty("number", 42);

        pb.removeAllProperties();
        assertTrue(pb.propertyCount() == 0);
    }

    @Test
    public void testWildeCatComperator_shouldSucceed() {
        ProcessBag processBag = new ProcessBag();
        processBag.setContext("blub");
        processBag.setProcessId("lala");
        processBag.setUser("cool");
        assertTrue(new ProcessBag().equals(processBag));
    }

    @Test
    public void testSameProcessBagsAreEquals_shouldSucceed() {
        ProcessBag processBag = new ProcessBag();
        processBag.setContext("blub");
        processBag.setProcessId("lala");
        processBag.setUser("cool");
        assertTrue(processBag.equals(processBag));
    }

    @Test
    public void testInequalsProcessBagsareEquals_shouldFail() {
        ProcessBag processBag = new ProcessBag();
        processBag.setContext("blub");
        processBag.setProcessId("lala");
        processBag.setUser("uncool");
        ProcessBag processBag2 = new ProcessBag();
        processBag2.setContext("blub");
        processBag2.setProcessId("lala");
        processBag2.setUser("cool");
        assertFalse(processBag.equals(processBag2));
    }

    @Test
    public void testIfNewProcessBagIsEqualsToFilledProcessBag_shouldSucceed() {
        ProcessBag processBag = new ProcessBag();
        HashMap<String, Object> props = Maps.newHashMap();
        props.put("a", "b");
        processBag.setProperties(props);
        assertTrue(new ProcessBag().equals(processBag));
    }

    @Test
    public void testIfFilledProcessBagIsEqualsToNewProcessBag_shouldFail() {
        ProcessBag processBag = new ProcessBag();
        Map<String, Object> props = Maps.newHashMap();
        props.put("a", "b");
        processBag.setProperties(props);
        assertFalse(processBag.equals(new ProcessBag()));
    }
}
