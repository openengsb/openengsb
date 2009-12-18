package org.openengsb.config.jbi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

public class MapNamespaceContext implements NamespaceContext {
    private Map<String, String> namespaces = new HashMap<String, String>();

    public MapNamespaceContext() {
    }

    public MapNamespaceContext(final Map<String, String> ns) {
        this.namespaces = ns;
    }

    public void addNamespace(final String prefix, final String namespaceURI) {
        this.namespaces.put(prefix, namespaceURI);
    }

    public void addNamespaces(final Map<String, String> ns) {
        this.namespaces.putAll(ns);
    }

    public Map<String, String> getUsedNamespaces() {
        return namespaces;
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return namespaces.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        for (Map.Entry<String, String> e : namespaces.entrySet()) {
            if (e.getValue().equals(namespaceURI))
                return e.getKey();
        }
        return null;
    }

    @Override
    public Iterator<?> getPrefixes(String namespaceURI) {
        return null;
    }

}
