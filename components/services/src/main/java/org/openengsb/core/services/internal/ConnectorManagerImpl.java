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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.model.ConnectorConfiguration;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.xlink.model.XLinkTemplate;
import org.openengsb.core.api.xlink.model.XLinkToolRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import org.openengsb.core.api.xlink.model.ModelToViewsTuple;

public class ConnectorManagerImpl implements ConnectorManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorManagerImpl.class);

    private ConnectorRegistrationManager registrationManager;
    private ConfigPersistenceService configPersistence;

    public void init() {
        new Thread() {
            @Override
            public void run() {
                Collection<ConnectorConfiguration> configs;
                try {
                    Map<String, String> emptyMap = Collections.emptyMap();
                    configs = configPersistence.load(emptyMap);
                } catch (InvalidConfigurationException e) {
                    throw new IllegalStateException(e);
                } catch (PersistenceException e) {
                    throw new IllegalStateException(e);
                }
                for (ConnectorConfiguration c : configs) {
                    try {
                        registrationManager.updateRegistration(c.getConnectorId(), c.getContent());
                    } catch (ConnectorValidationFailedException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        }.start();
    }

    @Override
    public String create(ConnectorDescription connectorDescription) throws ConnectorValidationFailedException {
        String id = UUID.randomUUID().toString();
        createWithId(id, connectorDescription);
        return id;
    }

    @Override
    public void createWithId(String id, ConnectorDescription connectorDescription)
        throws ConnectorValidationFailedException {
        checkForExistingServices(id);
        addDefaultLocations(id, connectorDescription);
        registrationManager.updateRegistration(id, connectorDescription);
        ConnectorConfiguration configuration = new ConnectorConfiguration(id, connectorDescription);
        try {
            configPersistence.persist(configuration);
        } catch (PersistenceException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void addDefaultLocations(String id, ConnectorDescription connectorDescription) {
        Map<String, Object> properties = connectorDescription.getProperties();
        if (properties.get("location.root") != null) {
            return;
        }
        Map<String, Object> copy = new HashMap<String, Object>(properties);
        copy.put("location.root", id);
        connectorDescription.setProperties(copy);
    }

    @Override
    public String forceCreate(ConnectorDescription connectorDescription) {
        String id = UUID.randomUUID().toString();
        registrationManager.forceUpdateRegistration(id, connectorDescription);
        ConnectorConfiguration configuration = new ConnectorConfiguration(id, connectorDescription);
        try {
            configPersistence.persist(configuration);
        } catch (PersistenceException e) {
            throw new IllegalArgumentException(e);
        }
        return id;
    }

    private void checkForExistingServices(String id) {
        try {
            List<ConnectorConfiguration> list =
                configPersistence.load(ImmutableMap.of(Constants.CONNECTOR_PERSISTENT_ID, id));
            if (!list.isEmpty()) {
                throw new IllegalArgumentException("connector already exists");
            }
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(String id, ConnectorDescription connectorDescpription)
        throws ConnectorValidationFailedException, IllegalArgumentException {
        ConnectorDescription old = getOldConfig(id);
        registrationManager.updateRegistration(id, connectorDescpription);
        applyConfigChanges(old, connectorDescpription);
        try {
            configPersistence.persist(new ConnectorConfiguration(id, connectorDescpription));
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void forceUpdate(String id, ConnectorDescription connectorDescription) throws IllegalArgumentException {
        ConnectorDescription old = getOldConfig(id);
        registrationManager.forceUpdateRegistration(id, connectorDescription);
        applyConfigChanges(old, connectorDescription);
        try {
            configPersistence.persist(new ConnectorConfiguration(id, connectorDescription));
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    private void applyConfigChanges(ConnectorDescription old, ConnectorDescription diff) {
        Map<String, String> updatedAttributes = updateAttributes(old.getAttributes(), diff.getAttributes());
        old.setAttributes(updatedAttributes);
        updateProperties(old.getProperties(), diff.getProperties());
    }

    private void updateProperties(Map<String, Object> properties, Map<String, Object> diff) {
        properties.putAll(diff);
    }

    private Map<String, String> updateAttributes(Map<String, String> attributes, Map<String, String> diff) {
        Map<String, String> result = new HashMap<String, String>(attributes);
        result.putAll(diff);
        return result;
    }

    private ConnectorDescription getOldConfig(String id) {
        List<ConnectorConfiguration> list;
        try {
            list = configPersistence.load(ImmutableMap.of(Constants.CONNECTOR_PERSISTENT_ID, id));
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
    public void delete(String id) throws PersistenceException {
        registrationManager.remove(id);
        configPersistence.remove(ImmutableMap.of(Constants.CONNECTOR_PERSISTENT_ID, id));
    }

    @Override
    public ConnectorDescription getAttributeValues(String id) {
        try {
            List<ConnectorConfiguration> list =
                configPersistence.load(ImmutableMap.of(Constants.CONNECTOR_PERSISTENT_ID, id));
            if (list.isEmpty()) {
                throw new IllegalArgumentException("no connector with metadata: " + id + " found");
            }
            if (list.size() < 1) {
                LOGGER.error("multiple values found for the same meta-data");
                throw new IllegalStateException("multiple connectors with metadata: " + id + " found");
            }
            return list.get(0).getContent();
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    public void setConfigPersistence(ConfigPersistenceService configPersistence) {
        this.configPersistence = configPersistence;
    }

    public void setRegistrationManager(ConnectorRegistrationManager registrationManager) {
        this.registrationManager = registrationManager;
    }

    @Override
    public void disconnectFromXLink(String id, String hostId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<XLinkToolRegistration> getXLinkRegistration(String hostId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public XLinkTemplate connectToXLink(
            String id, 
            String hostId, 
            String toolName, 
            ModelToViewsTuple[] modelsToViews){
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
