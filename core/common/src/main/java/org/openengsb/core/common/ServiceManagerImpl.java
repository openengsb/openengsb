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

package org.openengsb.core.common;

import java.util.List;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.InternalServiceRegistrationManager;
import org.openengsb.core.api.ServiceManager;
import org.openengsb.core.api.ServiceValidationFailedException;
import org.openengsb.core.api.model.ConnectorConfiguration;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.PersistenceException;

public class ServiceManagerImpl implements ServiceManager {

    private InternalServiceRegistrationManager registrationManager;
    private ConfigPersistenceService configPersistence = OpenEngSBCoreServices
        .getConfigPersistenceService(Constants.CONNECTOR);

    @Override
    public void createService(ConnectorId id, ConnectorDescription connectorDescription)
        throws ServiceValidationFailedException {
        try {
            List<ConnectorConfiguration> list = configPersistence.load(id.toMetaData());
            if (!list.isEmpty()) {
                throw new IllegalArgumentException("connector already exists");
            }
        } catch (PersistenceException e) {
            throw new ServiceValidationFailedException(e);
        }
        registrationManager.createService(id, connectorDescription);
        ConnectorConfiguration configuration = new ConnectorConfiguration(id, connectorDescription);
        try {
            configPersistence.persist(configuration);
        } catch (PersistenceException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void update(ConnectorId id, ConnectorDescription connectorDescpription)
        throws ServiceValidationFailedException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void delete(ConnectorId id) {
        // TODO delete from config (OPENENGSB-1256)
        registrationManager.delete(id);
    }

    @Override
    public ConnectorDescription getAttributeValues(ConnectorId id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void assignLocations(ConnectorId serviceId, String... locations) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void removeLocations(ConnectorId serviceId, String... locations) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void setRegistrationManager(InternalServiceRegistrationManager registrationManager) {
        this.registrationManager = registrationManager;
    }
}
