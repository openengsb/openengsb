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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.openengsb.labs.delegation.service.Provide;

/**
 * Container class that describes a connector. It serves as common model for
 * {@link org.openengsb.core.api.model.ConnectorConfiguration} in
 * {@link org.openengsb.core.api.persistence.ConfigPersistenceService} and the registration
 * {@link org.openengsb.core.api.ConnectorRegistrationManage}.
 * 
 * The description has two components: "attributes" and "properties".
 * 
 * Attributes are used to describe the field-values of the service objects. A
 * {@link org.openengsb.core.api.ConnectorInstanceFactory} defines the mapping of attributes to the corresponding
 * fields.
 * 
 * Properties are a collection of additional Service-properties for the osgi-service-registry (see OSGi 4.2 core
 * specification for more details http://www.osgi.org/Download/Release4V42).
 */
@SuppressWarnings("serial")
@Provide(alias = "ConnectorDescription")
public class ConnectorDescription implements Serializable {

    private Map<String, String> attributes;
    private Map<String, Object> properties;

    public ConnectorDescription() {
        this(new HashMap<String, String>(), new Hashtable<String, Object>());
    }

    /**
     * Both values could be null, since they will be replaced automatically with an empty hashmap
     */
    public ConnectorDescription(Map<String, String> attributes,
            Map<String, Object> properties) {
        if (attributes == null) {
            attributes = new HashMap<String, String>();
        }
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }
        this.attributes = attributes;
        this.properties = properties;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("Attributes: ");
        result.append(attributes);
        result.append(" - ");
        result.append("Properties: ");
        result.append(properties);
        return result.toString();
    }
}
