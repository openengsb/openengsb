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

package org.openengsb.core.api.model;

import java.io.Serializable;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class ConnectorDescription implements Serializable {

    private Map<String, String> attributes;
    private Dictionary<String, Object> properties;

    public ConnectorDescription() {
        this(new HashMap<String, String>(), new Hashtable<String, Object>());
    }

    public ConnectorDescription(Map<String, String> attributes) {
        this(attributes, new Hashtable<String, Object>());
    }

    public ConnectorDescription(Dictionary<String, Object> properties) {
        this(new HashMap<String, String>(), properties);
    }

    public ConnectorDescription(Map<String, String> attributes,
            Dictionary<String, Object> properties) {
        this.attributes = attributes;
        this.properties = properties;
    }

    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Dictionary<String, Object> getProperties() {
        return this.properties;
    }

    public void setProperties(Dictionary<String, Object> properties) {
        this.properties = properties;
    }

}
