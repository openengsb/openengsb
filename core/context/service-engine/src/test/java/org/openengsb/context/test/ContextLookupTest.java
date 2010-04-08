/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.context.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.context.EndpointContextHelperImpl;
import org.openengsb.contextcommon.ContextStore;

public class ContextLookupTest {
    private EndpointContextHelperImpl context = new EndpointContextHelperImpl();

    @Before
    public void setUp() throws IllegalArgumentException, SecurityException, IllegalAccessException,
            NoSuchFieldException {
        setContextStore();
        context.setCurrentId("42");
    }

    /**
     * Delete automatically created configuration file
     */
    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory("data");
    }

    @Test
    public void testValueLookup() {
        assertEquals(context.getValue("domain/key1"), "value1");
        assertEquals(context.getValue("domain/component/key1"), "value1core");

        context.remove(getListOfKey("domain/component/key1"));
        assertEquals(context.getValue("domain/component/key1"), "value1su");

        context.remove(getListOfKey("domain/component/SU/1/key1"));
        assertEquals(context.getValue("domain/component/key1"), "value1su");

        context.remove(getListOfKey("domain/component/SU/2/key1"));
        assertEquals(context.getValue("domain/component/key1"), "value1se");

        context.remove(getListOfKey("domain/component/SE/key1"));
        assertEquals(context.getValue("domain/component/key1"), "value1");
    }

    @Test
    public void testAllValuesLookup() {
        Map<String, String> keys = new HashMap<String, String>();
        keys.put("key1", "value1");
        keys.put("key2", "value2");
        keys.put("key3", "value3");
        keys.put("key4", "value4");

        assertEquals(context.getAllValues("domain2/component"), keys);
    }

    private void setContextStore() throws IllegalArgumentException, IllegalAccessException, SecurityException,
            NoSuchFieldException {
        Field field = EndpointContextHelperImpl.class.getDeclaredField("contextStore");
        field.setAccessible(true);
        field.set(context, new ContextStore(new File("target/test-classes/contextdata.xml")));
    }

    private List<String> getListOfKey(String key) {
        ArrayList<String> list = new ArrayList<String>();
        list.add(key);
        return list;
    }
}
