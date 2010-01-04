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

package org.openengsb.core.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Event {

    private Map<String, Object> elements = new HashMap<String, Object>();

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

}
