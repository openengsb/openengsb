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

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.core.common.util;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

/**
 * Adapted code from apache felix utils.collections
 */
public class MapAsDictionary<K, V> extends Dictionary<K, V> {

    private Map<K, V> map;

    public MapAsDictionary(Map<K, V> map) {
        this.map = map;
    }

    public static <K, V> Dictionary<K, V> wrap(Map<K, V> map) {
        if (map instanceof DictionaryAsMap) {
            return ((DictionaryAsMap<K, V>) map).getDictionary();
        }
        return new MapAsDictionary<K, V>(map);
    }

    public void setSourceMap(Map<K, V> map) {
        this.map = map;
    }

    @Override
    public Enumeration<V> elements() {
        return new IteratorToEnumeration<V>(map.values().iterator());
    }

    @Override
    public V get(Object key) {
        return map.get(key);
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Enumeration<K> keys() {
        return new IteratorToEnumeration<K>(map.keySet().iterator());
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(Object key, Object value) {
        return map.put((K) key, (V) value);
    }

    @Override
    public V remove(Object key) {
        return map.remove(key);
    }

    @Override
    public int size() {
        if (map == null) {
            return 0;
        }
        return map.size();
    }

    public Map<K, V> getMap() {
        return this.map;
    }

    class IteratorToEnumeration<T> implements Enumeration<T> {
        private final Iterator<T> iter;

        public IteratorToEnumeration(Iterator<T> iter) {
            this.iter = iter;
        }

        public boolean hasMoreElements() {
            if (iter == null) {
                return false;
            }
            return iter.hasNext();
        }

        public T nextElement() {
            if (iter == null) {
                return null;
            }
            return iter.next();
        }
    }
}
