/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.common.taskbox.model;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class TaskboxModelTest {
    private ProcessBag pb;

    @Before
    public void init() throws Exception {
        pb = new ProcessBag("test ID", "test context", "testuser");
    }

    @Test
    public void testCreateProcessBag_shouldNotFail() throws Exception {
        pb = new ProcessBag("4711", "c", "test-user");
        pb.addProperty("test", new String("42"));
        Object obj = new String("42");
        assertThat(pb.getProperty("test"), is(obj));
    }

    @Test
    public void testGetPropertyClass_shouldNotFail() throws Exception {
        pb.addProperty("test", new String("42"));
        Class<?> c = pb.getPropertyClass("test");
        assert(c == String.class);
    }

    @Test
    public void testGetPropertyKeyList_shouldbeSize3() throws Exception {
        pb.addProperty("test", new String("42"));
        pb.addProperty("number", new Integer(42));
        Set<String> list = pb.getPropertyKeyList();
        assertThat(list.size(), is(2));
    }

    @Test
    public void testGetPropertyKeyList_shouldContainTestString() throws Exception {
        pb.addProperty("number", new Integer(42));
        pb.addProperty("TestString", new String("42"));
        pb.removeProperty("number");
        Set<String> list = pb.getPropertyKeyList();
        assertTrue(list.contains("TestString"));
    }
}
