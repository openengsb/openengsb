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

package org.openengsb.core.api;

/**
 * Base interface for connector implementations, it makes the transparent setting of domainId and connectorId in
 * background possible. Important for enhancing of EDB events so that the EDB knows from where the change came. See
 * AbstractOpenEngSBConnectorService
 */
public interface Connector extends Domain {

    /**
     * sets the domain id of the domain used by the connector. Mainly used by the ConnectorRegistrationManagerImpl class
     * when a new connector is registered
     */
    void setDomainId(String domainId);

    /**
     * returns the defined id of the used domain by the connector
     */
    String getDomainId();

    /**
     * sets the connector id of the used connector. Mainly used by the ConnectorRegistrationManagerImpl class when a new
     * connector is registered
     */
    void setConnectorId(String connectorId);

    /**
     * returns the defined id of the used connector
     */
    String getConnectorId();
}
