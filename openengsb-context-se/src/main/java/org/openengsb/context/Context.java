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

package org.openengsb.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Context {

	private Map<String, String> values = new HashMap<String, String>();

	private Map<String, Context> children = new HashMap<String, Context>();

	private Context parent;

	public Context() {
	}

	/* copy constructor */
	public Context(Context ctx) {
		parent = ctx.parent;
		values = new HashMap<String, String>(ctx.values);

		for (Entry<String, Context> e : ctx.children.entrySet()) {
			children.put(e.getKey(), new Context(e.getValue()));
		}
	}

	public void set(String key, String value) {
		if (key == null) {
			throw new IllegalArgumentException("Key can not be null");
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
		return children.keySet();
	}

	public void createChild(String name) {
		if (name.contains("/")) {
			throw new IllegalArgumentException("Name must not contain '/'");
		}
		Context child = new Context();
		child.parent = this;
		children.put(name, child);
	}

	@Override
	public String toString() {
		return values.toString();
	}

	public void removeChild(String child) {
		Context childContext = children.get(child);
		if (childContext == null) {
			return;
		}
		childContext.parent = null;
		children.remove(child);
	}
}
