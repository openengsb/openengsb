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
            if (e.getValue().equals(namespaceURI)) {
                return e.getKey();
            }
        }
        return null;
    }

    @Override
    public Iterator<?> getPrefixes(String namespaceURI) {
        return null;
    }
}
