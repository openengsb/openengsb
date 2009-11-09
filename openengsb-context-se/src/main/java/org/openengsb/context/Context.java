package org.openengsb.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Context {

	private Map<String, String> values = new HashMap<String, String>();

	private Map<String, Context> children = new HashMap<String, Context>();

	public Context() {
	}

	/* copy constructor */
	public Context(Context ctx) {
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
		return values.get(key);
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
		children.put(name, new Context());
	}

	@Override
	public String toString() {
		return values.toString();
	}

	public void removeChild(String child) {
		children.remove(child);
	}
}
