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
package org.openengsb.core.edbi.jdbc.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.openengsb.core.edbi.models.SubTestModel;
import org.openengsb.core.edbi.models.TestModel;

/**
 * IntrospectorTest
 */
public class IntrospectorTest {

    @Test
    public void isModelClass_behavesCorrectly() throws Exception {
        assertTrue(Introspector.isModelClass(TestModel.class));
        assertTrue(Introspector.isModelClass(SubTestModel.class));
        assertFalse(Introspector.isModelClass(IntrospectorTest.class));
        assertFalse(Introspector.isModelClass(int.class));
    }

    @Test
    public void getPropertyTypeMap_returnsCorrectPropertyMap() throws Exception {
        Map<String, Class<?>> map = Introspector.getPropertyTypeMap(TestModel.class);

        assertEquals(4, map.size());
        assertEquals(List.class, map.get("openEngSBModelTail"));
        assertEquals(String.class, map.get("testId"));
        assertEquals(Integer.class, map.get("testInteger"));
        assertEquals(SubTestModel.class, map.get("subModel"));
    }

    @Test
    public void getPropertyTypeMap_withExclude_returnsCorrectPropertyMap() throws Exception {
        Map<String, Class<?>> map = Introspector.getPropertyTypeMap(TestModel.class, "class", "openEngSBModelTail");

        assertEquals(3, map.size());
        assertEquals(String.class, map.get("testId"));
        assertEquals(Integer.class, map.get("testInteger"));
        assertEquals(SubTestModel.class, map.get("subModel"));
    }

    @Test
    public void getOpenEngSBModelIdProperty_returnsCorrectProperty() throws Exception {
        assertEquals("testId", Introspector.getOpenEngSBModelIdProperty(TestModel.class));
        assertEquals("testId", Introspector.getOpenEngSBModelIdProperty(SubTestModel.class));
    }
}
