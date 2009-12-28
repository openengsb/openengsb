/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.openengsb.util.tuple.Pair;


public class Hash {
    /**
     * @see HashMap#HashMap()
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return new HashMap<K, V>();
    }

    /**
     * @see HashMap#HashMap(int)
     */
    public static <K, V> HashMap<K, V> newHashMap(int initialCapacity) {
        return new HashMap<K, V>(initialCapacity);
    }

    /**
     * @see HashMap#HashMap(int, float)
     */
    public static <K, V> HashMap<K, V> newHashMap(int initialCapacity, float loadFactor) {
        return new HashMap<K, V>(initialCapacity, loadFactor);
    }

    /**
     * @see HashMap#HashMap(Map)
     */
    public static <K, V> HashMap<K, V> newHashMap(Map<? extends K, ? extends V> map) {
        return new HashMap<K, V>(map);
    }

    /**
     * Constructs a new {@code HashMap} and insert the specified key-value pair.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> HashMap<K, V> newHashMap(Pair<K, V> a) {
        return newHashMapFromPairs(a);
    }

    /**
     * Constructs a new {@code HashMap} and insert the specified key-value
     * pairs.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> HashMap<K, V> newHashMap(Pair<K, V> a, Pair<K, V> b) {
        return newHashMapFromPairs(a, b);
    }

    /**
     * Constructs a new {@code HashMap} and insert the specified key-value
     * pairs.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> HashMap<K, V> newHashMap(Pair<K, V> a, Pair<K, V> b, Pair<K, V> c) {
        return newHashMapFromPairs(a, b, c);
    }

    /**
     * Constructs a new {@code HashMap} and insert the specified key-value
     * pairs.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> HashMap<K, V> newHashMap(Pair<K, V> a, Pair<K, V> b, Pair<K, V> c, Pair<K, V> d) {
        return newHashMapFromPairs(a, b, c, d);
    }

    /**
     * Constructs a new {@code HashMap} and insert the specified key-value
     * pairs.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> HashMap<K, V> newHashMap(Pair<K, V> a, Pair<K, V> b, Pair<K, V> c, Pair<K, V> d, Pair<K, V> e) {
        return newHashMapFromPairs(a, b, c, d, e);
    }

    private static <K, V> HashMap<K, V> newHashMapFromPairs(Pair<K, V>... pairs) {
        HashMap<K, V> map = newHashMap();
        for (Pair<K, V> p : pairs) {
            map.put(p.fst, p.snd);
        }
        return map;
    }

    /**
     * @see Hashtable#Hashtable()
     */
    public static <K, V> Hashtable<K, V> newHashtable() {
        return new Hashtable<K, V>();
    }

    /**
     * Constructs a new {@code HashMap} and insert the specified key-value
     * pairs.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Hashtable<K, V> newHashtable(Pair<K, V> a) {
        return newHashtableFromPairs(a);
    }

    /**
     * Constructs a new {@code HashMap} and insert the specified key-value
     * pairs.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Hashtable<K, V> newHashtable(Pair<K, V> a, Pair<K, V> b) {
        return newHashtableFromPairs(a, b);
    }

    /**
     * Constructs a new {@code HashMap} and insert the specified key-value
     * pairs.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Hashtable<K, V> newHashtable(Pair<K, V> a, Pair<K, V> b, Pair<K, V> c) {
        return newHashtableFromPairs(a, b, c);
    }

    /**
     * Constructs a new {@code HashMap} and insert the specified key-value
     * pairs.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Hashtable<K, V> newHashtable(Pair<K, V> a, Pair<K, V> b, Pair<K, V> c, Pair<K, V> d) {
        return newHashtableFromPairs(a, b, c, d);
    }

    /**
     * Constructs a new {@code HashMap} and insert the specified key-value
     * pairs.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Hashtable<K, V> newHashtable(Pair<K, V> a, Pair<K, V> b, Pair<K, V> c, Pair<K, V> d,
            Pair<K, V> e) {
        return newHashtableFromPairs(a, b, c, d, e);
    }

    private static <K, V> Hashtable<K, V> newHashtableFromPairs(Pair<K, V>... pairs) {
        Hashtable<K, V> table = newHashtable();
        for (Pair<K, V> p : pairs) {
            table.put(p.fst, p.snd);
        }
        return table;
    }

    private Hash() {
        throw new AssertionError();
    }
}
