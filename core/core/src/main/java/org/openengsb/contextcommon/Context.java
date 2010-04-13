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

package org.openengsb.contextcommon;

import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

public class Context {

    private SortedMap<String, String> values = new TreeMap<String, String>();

    private SortedMap<String, Context> children = new TreeMap<String, Context>();

    private Context parent;

    public Context() {
    }

    /* copy constructor */
    public Context(Context ctx) {
        parent = ctx.parent;
        values = new TreeMap<String, String>(ctx.values);

        for (Entry<String, Context> e : ctx.children.entrySet()) {
            Context context = new Context(e.getValue());
            context.parent = this;
            children.put(e.getKey(), context);
        }
    }

    public void set(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("Key can not be null");
        }
        if (key.contains("/")) {
            throw new IllegalArgumentException("Key must not contain '/'");
        }
        if (children.containsKey(key)) {
            throw new ContextNameClashException(String.format("A folder with name '%s' already exists", key));
        }
        values.put(key, value);
    }

    public String get(String key) {
        String value = values.get(key);

        if (value == null && parent != null) {
            return parent.get(key);
        }

        return value;
    }

    public Context getChild(String name) {
        ContextPath contextPath = new ContextPath(name);
        Context ctx = this;

        for (String child : contextPath.getElements()) {
            ctx = ctx.children.get(child);

            if (ctx == null) {
                return null;
            }
        }

        return ctx;
    }

    public Set<String> getChildrenNames() {
        return new TreeSet<String>(children.keySet());
    }
    
    public boolean containsKey(String key) {
        return values.containsKey(key);
    }

    public Set<String> getKeys() {
        return new TreeSet<String>(values.keySet());
    }

    void createChild(String name) {
        if (name.contains("/")) {
            throw new IllegalArgumentException("Name must not contain '/'");
        }
        Context child = new Context();
        child.parent = this;
        children.put(name, child);
    }

    void removeChild(String child) {
        Context childContext = children.get(child);
        if (childContext == null) {
            return;
        }
        childContext.parent = null;
        children.remove(child);
    }

    public void remove(String key) {
        values.remove(key);
    }
    
    public Context getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return String.format("[Context, values=%s, children=%s]", values.toString(), children.toString());
    }

}
