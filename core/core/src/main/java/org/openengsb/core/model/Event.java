/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.model;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Event {

    private Map<String, Object> elements = new TreeMap<String, Object>(new EventKeyComparator());

    private String domain;

    private String name;

    private String toolConnector;

    public Event(String domain, String name) {
        this.domain = domain;
        this.name = name;
    }

    @SuppressWarnings("unused")
    private Event() {
        // used by segment transformation framework
    }

    public void setValue(String key, Object value) {
        elements.put(key, value);
    }

    public Object getValue(String key) {
        return elements.get(key);
    }

    public boolean containsKey(String key) {
        return elements.containsKey(key);
    }

    public Set<String> getKeys() {
        return new HashSet<String>(elements.keySet());
    }

    public String getDomain() {
        return domain;
    }

    public String getName() {
        return name;
    }

    public String getToolConnector() {
        return toolConnector;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setToolConnector(String toolConnector) {
        this.toolConnector = toolConnector;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((toolConnector == null) ? 0 : toolConnector.hashCode());
        result = prime * result + ((elements == null) ? 0 : elements.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Event other = (Event) obj;
        if (domain == null) {
            if (other.domain != null)
                return false;
        } else if (!domain.equals(other.domain))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (toolConnector == null) {
            if (other.toolConnector != null)
                return false;
        } else if (!toolConnector.equals(other.toolConnector))
            return false;
        if (elements == null) {
            if (other.elements != null)
                return false;
        } else if (!elements.equals(other.elements))
            return false;
        return true;
    }

    private class EventKeyComparator implements Comparator<String> {

        @Override
        public int compare(String key1, String key2) {
            return key1.compareTo(key2);
        }
    }

}
