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

package org.openengsb.core.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.EnumerationUtils;
import org.junit.Test;

public class DictonaryUtilTest {

    @Test
    public void testWrapDictionary_shouldWrapDictionaryAsMap() throws Exception {
        Dictionary<String, Object> dict = new Hashtable<String, Object>();
        dict.put("test", 42L);
        dict.put("foo", "bar");
        Map<String, Object> wrapped = DictionaryAsMap.wrap(dict);
        assertThat((Long) wrapped.get("test"), is(42L));
        assertThat((String) wrapped.get("foo"), is("bar"));
    }

    @Test
    public void testIterateWrappedDictionary_shouldReturnKeysAndValues() throws Exception {
        Dictionary<String, Object> dict = new Hashtable<String, Object>();
        dict.put("test", 42L);
        dict.put("foo", "bar");
        Map<String, Object> wrapped = DictionaryAsMap.wrap(dict);
        assertThat(wrapped.keySet(), hasItems("test", "foo"));
        assertThat(wrapped.values(), hasItems((Object) 42L, (Object) "bar"));
    }

    @Test
    public void testIterateEntriesOfWrappedDictionary_shouldWork() throws Exception {
        Dictionary<String, Object> dict = new Hashtable<String, Object>();
        dict.put("test", 42L);
        dict.put("foo", "bar");
        Map<String, Object> wrapped = DictionaryAsMap.wrap(dict);
        Set<Entry<String, Object>> entrySet = wrapped.entrySet();
        Iterator<Entry<String, Object>> iterator = entrySet.iterator();
        iterator.next();
        Entry<String, Object> entry2 = iterator.next();
        assertThat(entry2.getKey(), is("test"));
        assertThat(entry2.getValue(), is((Object) 42L));
    }

    @Test
    public void wrapMapToDictionary_shouldWrapMapAsDictionary() throws Exception {
        Map<String, String> testMap = new HashMap<String, String>();
        testMap.put("test", "42");
        testMap.put("foo", "bar");
        Dictionary<String, String> dict = MapAsDictionary.wrap(testMap);
        @SuppressWarnings("unchecked")
        List<String> list = EnumerationUtils.toList(dict.elements());
        assertThat(list, hasItems("42", "bar"));
    }
}
