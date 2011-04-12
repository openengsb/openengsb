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
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

@SuppressWarnings("serial")
public class ConnectorId implements Serializable {

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

    public static ConnectorId fromMetaData(Map<String, String> metaData) {
        return new ConnectorId(metaData.get("domainType"), metaData.get("connectorType"), metaData.get("instanceId"));
    }

    public static ConnectorId generate(String domainType, String connectorType) {
        String instanceId = UUID.randomUUID().toString();
        return new ConnectorId(domainType, connectorType, instanceId);
    }

    public static ConnectorId parse(String fullId) {
        Scanner s = new Scanner(fullId);
        s.useDelimiter("\\+");
        String domain = s.next();
        String connector = s.next();
        String instanceId = s.next();
        if (s.hasNext()) {
            s.useDelimiter("\\\n");
            instanceId += s.next();
        }
        return new ConnectorId(domain, connector, instanceId);
    }

    @Override
    public String toString() {
        return domainType + "+" + connectorType + "+" + instanceId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.connectorType == null) ? 0 : this.connectorType.hashCode());
        result = prime * result + ((this.domainType == null) ? 0 : this.domainType.hashCode());
        result = prime * result + ((this.instanceId == null) ? 0 : this.instanceId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ConnectorId other = (ConnectorId) obj;
        if (this.connectorType == null) {
            if (other.connectorType != null) {
                return false;
            }
        } else if (!this.connectorType.equals(other.connectorType)) {
            return false;
        }
        if (this.domainType == null) {
            if (other.domainType != null) {
                return false;
            }
        } else if (!this.domainType.equals(other.domainType)) {
            return false;
        }
        if (this.instanceId == null) {
            if (other.instanceId != null) {
                return false;
            }
        } else if (!this.instanceId.equals(other.instanceId)) {
            return false;
        }
        return true;
    }

}
