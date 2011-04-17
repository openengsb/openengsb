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
import org.openengsb.core.api.persistence.PersistenceException;

/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The AASTI licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

public interface ServiceManager {

    /**
     * creates a new connector with the given id and description. The new connectorDescription is validated before the
     * connector is created. In this validation-step only the combination of the attributes is validated. Each valid is
     * assumed to be valid by itself (e.g. number-attributes)
     *
     * The connector is then registered in the OSGi-registry and persisted using
     * {@link org.openengsb.core.api.persistence.ConfigPersistenceService}
     *
     * @throws ServiceValidationFailedException if the attributes supplied with the connectorDescription are invalid
     */
    void createService(ConnectorId id, ConnectorDescription connectorDescription)
        throws ServiceValidationFailedException;

    /**
     * creates a new connector with the given id and description. It works similar to
     * {@link ServiceManager#createService} but skips the validation step. However this method still assumes that each
     * attribute is valid by itself (e.g. number-attributes)
     *
     * The connector is then registered in the OSGi-registry and persisted using
     * {@link org.openengsb.core.api.persistence.ConfigPersistenceService}
     *
     * @throws ServiceValidationFailedException if the attributes supplied with the connectorDescription are invalid
     */
    void forceCreateService(ConnectorId id, ConnectorDescription connectorDescription);

    /**
     * Updates an existing connector instance. The list of attributes and the properties are OVERWRITTEN. This means
     * that attributes and properties that are not present in the new description are removed.
     *
     * If the attributes are invalid, the connector remains unchanged.
     *
     * @throws ServiceValidationFailedException if the combination of the new attributes are not valid
     * @throws IllegalArgumentException if no connector with the given id is available
     */
    void update(ConnectorId id, ConnectorDescription connectorDescription)
        throws ServiceValidationFailedException, IllegalArgumentException;

    /**
     * Updates an existing connector instance. The list of attributes and the properties are OVERWRITTEN. This means
     * that attributes and properties that are not present in the new description are removed.
     *
     * Unlike {@link ServiceManager#update} this method skips the attribute validation before the update.
     *
     * @throws IllegalArgumentException if no connector with the given id is available
     */
    void forceUpdate(ConnectorId id, ConnectorDescription connectorDescription) throws IllegalArgumentException;

    /**
     * Deletes the connector instanced with the given {@code id}.
     *
     * @throws IllegalArgumentException if no instance exists for the given id.
     */
    void delete(ConnectorId id) throws PersistenceException;

    /**
     * Returns the description for the specified connector instance.
     *
     * @param id identifier for a already existing connector instance
     */
    ConnectorDescription getAttributeValues(ConnectorId id);

}
