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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ConnectorId {

    private String domainType;
    private String connectorType;
    private String instanceId;

    public ConnectorId() {
    }

    public ConnectorId(String domainType, String connectorType, String instanceId) {
        this.domainType = domainType;
        this.connectorType = connectorType;
        this.instanceId = instanceId;
    }

    public String getDomainType() {
        return this.domainType;
    }

    public void setDomainType(String domainType) {
        this.domainType = domainType;
    }

    public String getConnectorType() {
        return this.connectorType;
    }

    public void setConnectorType(String connectorType) {
        this.connectorType = connectorType;
    }

    public String getInstanceId() {
        return this.instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public Map<String, String> toMetaData() {
        Map<String, String> metaData = new HashMap<String, String>();
        metaData.put("domainType", domainType);
        metaData.put("connectorType", connectorType);
        metaData.put("instanceId", instanceId);
        return metaData;
    }

    public static ConnectorId generate(String domainType, String connectorType) {
        String instanceId = UUID.randomUUID().toString();
        return new ConnectorId(domainType, connectorType, instanceId);

    }

}
