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

import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.ConnectorRegistrationManager;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.model.ConnectorConfiguration;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.xlink.model.XLinkModelInformation;
import org.openengsb.core.api.xlink.model.XLinkTemplate;
import org.openengsb.core.api.xlink.model.XLinkToolRegistration;
import org.openengsb.core.api.xlink.model.XLinkToolView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import java.util.ArrayList;
import org.openengsb.core.common.xlink.XLinkUtils;

public class ConnectorManagerImpl implements ConnectorManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorManagerImpl.class);

    private ConnectorRegistrationManager registrationManager;
    private ConfigPersistenceService configPersistence;
    private Map<XLinkRegistrationKey,XLinkToolRegistration> xlinkRegistrations;
    private String xLinkBaseUrl = "http://localhost/openXLink";
    private int xLinkExpiresIn = 3;

    public void init() {
        new Thread() {
            @Override
            public void run() {
                try {
                    xlinkRegistrations = new HashMap<XLinkRegistrationKey,XLinkToolRegistration>();
                    Collection<ConnectorConfiguration> configs;
                    try {
                        Map<String, String> emptyMap = Collections.emptyMap();
                        configs = getConfigPersistence().load(emptyMap);
                    } catch (InvalidConfigurationException e) {
                        throw new IllegalStateException(e);
                    } catch (PersistenceException e) {
                        throw new IllegalStateException(e);
                    }
                    // FIXME Should be refactored when OPENENGSB-1931 is fixed
                    configs = Collections2.filter(configs, new Predicate<ConfigItem<?>>() {
                        @Override
                        public boolean apply(ConfigItem<?> input) {
                            return input instanceof ConnectorConfiguration;
                        }
                    });
                    for (ConnectorConfiguration c : configs) {
                        try {
                            registrationManager.updateRegistration(c.getConnectorId(), c.getContent());
                        } catch (ConnectorValidationFailedException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Exception while restoring connectors", e);
                }
            }
        }.start();

    }

    @Override
    public void create(ConnectorId id, ConnectorDescription connectorDescription)
        throws ConnectorValidationFailedException {
        validateId(id);
        checkForExistingServices(id);
        addDefaultLocations(id, connectorDescription);
        registrationManager.updateRegistration(id, connectorDescription);
        ConnectorConfiguration configuration = new ConnectorConfiguration(id, connectorDescription);
        try {
            getConfigPersistence().persist(configuration);
        } catch (PersistenceException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void addDefaultLocations(ConnectorId id, ConnectorDescription connectorDescription) {
        Map<String, Object> properties = connectorDescription.getProperties();
        if (properties.get("location.root") != null) {
            return;
        }
        Map<String, Object> copy = new HashMap<String, Object>(properties);
        copy.put("location.root", id.getInstanceId());
        connectorDescription.setProperties(copy);
    }

    @Override
    public void forceCreate(ConnectorId id, ConnectorDescription connectorDescription) {
        validateId(id);
        checkForExistingServices(id);
        registrationManager.forceUpdateRegistration(id, connectorDescription);
        ConnectorConfiguration configuration = new ConnectorConfiguration(id, connectorDescription);
        try {
            getConfigPersistence().persist(configuration);
        } catch (PersistenceException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void validateId(ConnectorId id) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(id.getConnectorType());
        Preconditions.checkNotNull(id.getDomainType());
        Preconditions.checkNotNull(id.getInstanceId());
    }

    private void checkForExistingServices(ConnectorId id) {
        try {
            List<ConnectorConfiguration> list = getConfigPersistence().load(id.toMetaData());
            if (!list.isEmpty()) {
                throw new IllegalArgumentException("connector already exists");
            }
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(ConnectorId id, ConnectorDescription connectorDescpription)
        throws ConnectorValidationFailedException, IllegalArgumentException {
        validateId(id);
        ConnectorDescription old = getOldConfig(id);
        registrationManager.updateRegistration(id, connectorDescpription);
        applyConfigChanges(old, connectorDescpription);
        try {
            getConfigPersistence().persist(new ConnectorConfiguration(id, connectorDescpription));
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void forceUpdate(ConnectorId id, ConnectorDescription connectorDescription) throws IllegalArgumentException {
        validateId(id);
        ConnectorDescription old = getOldConfig(id);
        registrationManager.forceUpdateRegistration(id, connectorDescription);
        applyConfigChanges(old, connectorDescription);
        try {
            getConfigPersistence().persist(new ConnectorConfiguration(id, connectorDescription));
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

    private ConnectorDescription getOldConfig(ConnectorId id) {
        List<ConnectorConfiguration> list;
        try {
            list = getConfigPersistence().load(id.toMetaData());
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
    public void delete(ConnectorId id) throws PersistenceException {
        registrationManager.remove(id);
        getConfigPersistence().remove(id.toMetaData());
    }

    @Override
    public ConnectorDescription getAttributeValues(ConnectorId id) {
        try {
            List<ConnectorConfiguration> list = getConfigPersistence().load(id.toMetaData());
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

    public ConfigPersistenceService getConfigPersistence() {
        return configPersistence;
    }

    public void setConfigPersistence(ConfigPersistenceService configPersistence) {
        this.configPersistence = configPersistence;
    }

    public void setRegistrationManager(ConnectorRegistrationManager registrationManager) {
        this.registrationManager = registrationManager;
    }

    @Override
    public void disconnectFromXLink(ConnectorId id) {
        //api change: add hostId to disconnect
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private boolean isRegistered(ConnectorId id, String hostId){
        XLinkRegistrationKey key = new XLinkRegistrationKey(id, hostId);
        return xlinkRegistrations.containsKey(key);
    }

    @Override
    public List<XLinkToolRegistration> getXLinkRegistration(String hostId) {
        List<XLinkToolRegistration> registrationsOfHostId = new ArrayList<XLinkToolRegistration>();
        for(XLinkRegistrationKey key : xlinkRegistrations.keySet()){
            if(key.getHostId().equals(hostId))registrationsOfHostId.add(xlinkRegistrations.get(key));
        }
        return registrationsOfHostId;
    }
    
    public XLinkTemplate connectToXLink(ConnectorId id, String hostId, String toolName, Map<XLinkModelInformation, List<XLinkToolView>> modelsToViews) {
        //XLinkTemplate template = XLinkUtils.prepareXLinkTemplate(xLinkBaseUrl, id.toFullID(), modelsToViews, xLinkExpiresIn, null);
        return null;
    }
    
    private class XLinkRegistrationKey{
        private ConnectorId connectorId;
        private String hostId;

        public XLinkRegistrationKey(ConnectorId connectorId, String hostId) {
            this.connectorId = connectorId;
            this.hostId = hostId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final XLinkRegistrationKey other = (XLinkRegistrationKey) obj;
            if (this.connectorId != other.connectorId && (this.connectorId == null || !this.connectorId.equals(other.connectorId))) {
                return false;
            }
            if ((this.hostId == null) ? (other.hostId != null) : !this.hostId.equals(other.hostId)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 89 * hash + (this.connectorId != null ? this.connectorId.hashCode() : 0);
            hash = 89 * hash + (this.hostId != null ? this.hostId.hashCode() : 0);
            return hash;
        }
        
        public ConnectorId getConnectorId() {
            return connectorId;
        }

        public void setConnectorId(ConnectorId connectorId) {
            this.connectorId = connectorId;
        }

        public String getHostId() {
            return hostId;
        }

        public void setHostId(String hostId) {
            this.hostId = hostId;
        }
        
        
    }

}
