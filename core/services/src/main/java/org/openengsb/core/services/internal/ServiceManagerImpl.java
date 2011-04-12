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

package org.openengsb.core.services.internal;

import java.util.Collections;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.ServiceManager;
import org.openengsb.core.api.ServiceRegistrationManager;
import org.openengsb.core.api.ServiceValidationFailedException;
import org.openengsb.core.api.model.ConnectorConfiguration;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.common.OpenEngSBCoreServices;

import com.google.common.base.Preconditions;

public class ServiceManagerImpl implements ServiceManager {

    private ServiceRegistrationManager registrationManager;
    private ConfigPersistenceService configPersistence = OpenEngSBCoreServices
        .getConfigPersistenceService(Constants.CONNECTOR);

    @Override
    public void createService(ConnectorId id, ConnectorDescription connectorDescription)
        throws ServiceValidationFailedException {
        Preconditions.checkNotNull(id.getConnectorType());
        Preconditions.checkNotNull(id.getDomainType());
        Preconditions.checkNotNull(id.getInstanceId());
        try {
            List<ConnectorConfiguration> list = configPersistence.load(id.toMetaData());
            if (!list.isEmpty()) {
                throw new IllegalArgumentException("connector already exists");
            }
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
        registrationManager.updateRegistration(id, connectorDescription);
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
        ConnectorDescription old = getOldConfig(id);
        registrationManager.updateRegistration(id, connectorDescpription);
        applyConfigChanges(old, connectorDescpription);
        try {
            configPersistence.persist(new ConnectorConfiguration(id, connectorDescpription));
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    private void applyConfigChanges(ConnectorDescription old, ConnectorDescription diff) {
        updateAttributes(old.getAttributes(), diff.getAttributes());
        updateProperties(old.getProperties(), diff.getProperties());
    }

    private void updateProperties(Dictionary<String, Object> properties, Dictionary<String, Object> diff) {
        for (String key : Collections.list(diff.keys())) {
            properties.put(key, diff.get(key));
        }
    }

    private void updateAttributes(Map<String, String> attributes, Map<String, String> diff) {
        for (Map.Entry<String, String> entry : diff.entrySet()) {
            if (entry.getValue() == null) {
                attributes.remove(entry.getKey());
            } else {
                attributes.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private ConnectorDescription getOldConfig(ConnectorId id) {
        List<ConnectorConfiguration> list;
        try {
            list = configPersistence.load(id.toMetaData());
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
        if (list.isEmpty()) {
            throw new IllegalArgumentException("no connector with id " + id + " found");
        }
        if (list.size() > 1) {
            throw new IllegalStateException("multiple connectors with id " + id + " found");
        }
        return list.get(0).getContent();
    }

    @Override
    public void delete(ConnectorId id) {
        // TODO delete from config (OPENENGSB-1256)
        registrationManager.remove(id);
    }

    @Override
    public ConnectorDescription getAttributeValues(ConnectorId id) {
        try {
            List<ConnectorConfiguration> list = configPersistence.load(id.toMetaData());
            if (list.size() != 1) {
                return null;
            }
            return list.get(0).getContent();
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void assignLocations(ConnectorId serviceId, String... locations) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void removeLocations(ConnectorId serviceId, String... locations) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void setRegistrationManager(ServiceRegistrationManager registrationManager) {
        this.registrationManager = registrationManager;
    }
}
