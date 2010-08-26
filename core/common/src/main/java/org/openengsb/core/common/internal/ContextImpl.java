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
package org.openengsb.core.common.internal;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.openengsb.core.common.context.Context;

import com.google.common.base.Preconditions;

public class ContextImpl implements Context {

    private final Map<String, String> values = new TreeMap<String, String>();
    private final Map<String, Context> children = new TreeMap<String, Context>();

    @Override
    public Set<String> getKeys() {
        return Collections.unmodifiableSet(values.keySet());
    }

    @Override
    public String get(String key) {
        Preconditions.checkNotNull(key, "key is null");
        return values.get(key);
    }

    @Override
    public void put(String key, String value) {
        Preconditions.checkNotNull(key, "key is null");
        Preconditions.checkNotNull(value, "value is null");
        Preconditions.checkArgument(key.indexOf('/') == -1, "key must not contain a slash");
        Preconditions.checkArgument(!children.containsKey(key), "key identifies a path, put operation not allowed");
        values.put(key, value);
    }

    @Override
    public Map<String, Context> getChildren() {
        return Collections.unmodifiableMap(children);
    }

    @Override
    public Context getChild(String name) {
        return children.get(name);
    }

    @Override
    public Context createChild(String name) {
        Preconditions.checkArgument(name.indexOf('/') == -1, "name must not contain a slash");
        Preconditions.checkArgument(!children.containsKey(name), "child with name '%s' already exists", name);
        Preconditions.checkArgument(!values.containsKey(name), "name identifies a key-value pair",
                "createChild operation not allowed");
        ContextImpl child = new ContextImpl();
        children.put(name, child);
        return child;
    }

    @Override
    public void remove(String nameOrKey) {
        Preconditions.checkNotNull(nameOrKey, "name or key for removal is null");
        values.remove(nameOrKey);
        children.remove(nameOrKey);
    }

}
