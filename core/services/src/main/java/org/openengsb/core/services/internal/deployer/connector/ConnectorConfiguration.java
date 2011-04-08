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

package org.openengsb.core.services.internal.deployer.connector;

import java.io.IOException;
import java.util.Map;

public final class ConnectorConfiguration {

    private String connectorType;
    private String serviceId;
    private Map<String, String> attributes;

    private ConnectorConfiguration() {
    }

    public static ConnectorConfiguration loadFromFile(ConnectorFile configFile) throws IOException {
        ConnectorConfiguration config = new ConnectorConfiguration();

        config.connectorType = configFile.getConnectorName();
        config.serviceId = configFile.getServiceId();
        config.attributes = configFile.getAttributes();

        return config;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

}
