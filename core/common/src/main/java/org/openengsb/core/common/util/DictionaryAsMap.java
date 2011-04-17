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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A wrapper around a dictionary access it as a Map.
 *
 * This class is a Map-implementation that delegates operations to a backing {@link Dictionary}. The resulting Map can
 * be accessed an manipulated like any other Map with a limitation: When assigning a null-value to a key (map.put(key,
 * null)), the key is removed from the dictionary, since it does not support null-values.
 *
 * Adapted code from apache felix utils.collections
 *
 * As opposed to the original felix-implementation this Map is not immutable and can be manipulated, with the
 * restriction(s) described above.
 */
public class DictionaryAsMap<K, V> extends AbstractMap<K, V> {

    private Dictionary<K, V> dictionary;

    /**
     * creates a new instance backed by the given dictionary. Please use {@link DictionaryAsMap#wrap} to prevent nesting
     * of {@link MapAsDictionary} and {@link DictionaryAsMap}
     */
    public DictionaryAsMap(Dictionary<K, V> dict) {
        this.dictionary = dict;
    }

    /**
     * creates a Map-representation of the dictionary. If the dictionary is an instance of {@link MapAsDictionary} the
     * original map is returned to prevent deeper nesting.
     */
    public static <K, V> Map<K, V> wrap(Dictionary<K, V> dictionary) {
        if (dictionary instanceof MapAsDictionary) {
            return ((MapAsDictionary<K, V>) dictionary).getMap();
        }
        return new DictionaryAsMap<K, V>(dictionary);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                final Enumeration<K> e = dictionary.keys();
                return new Iterator<Entry<K, V>>() {
                    private K key;

                    public boolean hasNext() {
                        return e.hasMoreElements();
                    }

                    public Entry<K, V> next() {
                        key = e.nextElement();
                        return new KeyEntry(key);
                    }

                    public void remove() {
                        if (key == null) {
                            throw new IllegalStateException();
                        }
                        dictionary.remove(key);
                    }
                };
            }

            @Override
            public int size() {
                return dictionary.size();
            }
        };
    }

    @Override
    public V put(K key, V value) {
        if (value == null) {
            dictionary.remove(key);
            return value;
        }
        return dictionary.put(key, value);
    }

    class KeyEntry implements Map.Entry<K, V> {

        private final K key;

        KeyEntry(K key) {
            this.key = key;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return dictionary.get(key);
        }

        public V setValue(V value) {
            if (value == null) {
                DictionaryAsMap.this.dictionary.remove(key);
                return null;
            }
            return DictionaryAsMap.this.put(key, value);
        }
    }

    public Dictionary<K, V> getDictionary() {
        return this.dictionary;
    }
}
