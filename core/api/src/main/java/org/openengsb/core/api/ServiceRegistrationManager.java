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

import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;

/**
 * Instance provider for a specific service interface.
 */
public interface ServiceRegistrationManager extends OpenEngSBService {

    /**
     * updates a service instance. If the given id does not exist, this creates a new service instance.
     *
     * @param id identifier for a new or already existing service instance.
     * @param attributes updates to maybe already set attributes.
     * @return the result of the validation
     */
    void updateRegistration(ConnectorId id, ConnectorDescription connectorDescpription)
        throws ServiceValidationFailedException;

    void forceUpdateRegistration(ConnectorId id, ConnectorDescription connectorDescpription);

    /**
     * Deletes the service instanced with the given {@code id}.
     *
     * @param id identifier for a service instance provided by this {@code ServiceManager}.
     *
     * @throws IllegalArgumentException if no instance exists for the given id.
     */
    void remove(ConnectorId id);

}
