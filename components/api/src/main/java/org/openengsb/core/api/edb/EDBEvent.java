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

package org.openengsb.core.api.edb;

import org.openengsb.core.api.Event;
import org.openengsb.core.api.model.ConnectorDefinition;

/**
 * Interface for easier maintaining of the Events in the DomainEventsProxyFacory. Saves the domain id, connector id and
 * instance id so that the EDB is able to save this detail information.
 */
public abstract class EDBEvent extends Event {

    private String domainId;
    private String connectorId;
    private String instanceId;
    
    /**
     * parses a full connector id (format <domainType>+<connectorType>+<instanceId>) and sets the corresponding values
     * in the event. Example: "scm+git+projectx-main-repo" 
     */
    public void parseConnectorId(String connectorId) {
        ConnectorDefinition id = ConnectorDefinition.fromFullId(connectorId);
        domainId = id.getDomainId();
        connectorId = id.getConnectorId();
        instanceId = id.getInstanceId();
    }

    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    public String getDomainId() {
        return domainId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceId() {
        return instanceId;
    }

}
