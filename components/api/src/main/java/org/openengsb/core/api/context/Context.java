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

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Preconditions;

/**
 * A Context is a tree-like structure that contains key-value pairs (leafs) and child contexts (inner nodes of the
 * tree).
 */
public class Context {

    private String id;
    private Map<String, String> values = new TreeMap<String, String>();
    private Map<String, Context> children = new TreeMap<String, Context>();

    /**
     * Returns an unmodifiable {@code Set} of keys where values exist at the current {@code Context} level.
     */
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(values.keySet());
    }

    /**
     * Returns the value for the given {@code key} or null if the {@code key} has not been set.
     */
    public String get(String key) {
        Preconditions.checkNotNull(key, "key is null");
        return values.get(key);
    }

    /**
     * Puts the {@code key-value} pair at the current {@code Context} level. The {@code key} must not contain any
     * slashes ({@code '/'}).
     * 
     * @throws NullPointerException if {@code key} or {@code value} is null
     * @throws IllegalArgumentException if a child {@code Context} with the given {@code key} exists.
     */
    public void put(String key, String value) {
        Preconditions.checkNotNull(key, "key is null");
        Preconditions.checkNotNull(value, "value is null");
        Preconditions.checkArgument(key.indexOf('/') == -1, "key must not contain a slash");
        Preconditions.checkArgument(!children.containsKey(key), "key identifies a path, put operation not allowed");
        values.put(key, value);
    }

    /**
     * Returns an unmodifiable {@code Map} of child {@code Context}s.
     */
    public Map<String, Context> getChildren() {
        return Collections.unmodifiableMap(children);
    }

    /**
     * Returns the child {@code Context} for the given {@code name} or null if the child does not exist.
     */
    public Context getChild(String name) {
        return children.get(name);
    }

    /**
     * Create a new child {@code Context}. The name must not contain any slashes ({@code '/'}).
     * 
     * @throws IllegalArgumentException if a child or key-value pair with the given @{code name} exists.
     */
    public Context createChild(String name) {
        Preconditions.checkArgument(name.indexOf('/') == -1, "name must not contain a slash");
        Preconditions.checkArgument(!children.containsKey(name), "child with name '%s' already exists", name);
        Preconditions.checkArgument(!values.containsKey(name), "name identifies a key-value pair",
                "createChild operation not allowed");
        Context child = new Context();
        children.put(name, child);
        return child;
    }

    /**
     * Removes the child {@code Context} or key-value pair the given parameter identifies.
     */
    public void remove(String nameOrKey) {
        Preconditions.checkNotNull(nameOrKey, "name or key for removal is null");
        values.remove(nameOrKey);
        children.remove(nameOrKey);
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        for (Entry<String, Context> ch : children.entrySet()) {
            String child = ch.getValue().toString();
            String elem = ch.getKey() + ":\n" + indent(child) + "\n";
            result.append(elem);
        }
        for (Entry<String, String> val : values.entrySet()) {
            String elem = val.getKey() + " = " + val.getValue() + "\n";
            result.append(elem);
        }
        return result.toString();
    }

    private static final String INDENTION_STRING = "..";

    private static String indent(String arg) {
        String s1 = INDENTION_STRING + arg.replaceAll("\n", "\n" + INDENTION_STRING);
        String s2 = s1.replaceAll("\n" + INDENTION_STRING + "$", "");
        return s2;
    }

}
