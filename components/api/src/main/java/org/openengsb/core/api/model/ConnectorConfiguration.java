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

import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Specific configuration model for the configuration to get up an connector.
 */
@XmlRootElement
public class ConnectorConfiguration extends ConfigItem<ConnectorDescription> {

    public static final String TYPE_ID = "CONNECTOR";

    public ConnectorConfiguration() {
        super();
    }

    public ConnectorConfiguration(Map<String, String> metaData, ConnectorDescription content) {
        super(metaData, content);
    }

    public ConnectorConfiguration(ConnectorId id, ConnectorDescription content) {
        this.metaData = id.toMetaData();
        this.content = content;
    }

    public ConnectorId getConnectorId() {
        return ConnectorId.fromMetaData(metaData);
    }

}
