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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.model.ConnectorConfiguration;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.xlink.model.XLinkTemplate;
import org.openengsb.core.api.xlink.model.XLinkToolRegistration;
import org.openengsb.core.api.xlink.model.XLinkToolView;
import org.openengsb.core.common.xlink.XLinkUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import org.openengsb.core.api.ekb.ModelDescription;
import org.openengsb.core.services.internal.exceptions.XLinkConnectException;

public class ConnectorManagerImpl implements ConnectorManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorManagerImpl.class);

    private ConnectorRegistrationManager registrationManager;
    private ConfigPersistenceService configPersistence;
    private Map<XLinkRegistrationKey, XLinkToolRegistration> xlinkRegistrations
        = new HashMap<XLinkRegistrationKey, XLinkToolRegistration>();
    private String xLinkBaseUrl;
    private int xLinkExpiresIn = 3;

    public void init() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Collection<ConnectorConfiguration> configs;
                    try {
                        Map<String, String> emptyMap = Collections.emptyMap();
                        configs = configPersistence.load(emptyMap);
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
        synchronized (xlinkRegistrations) {
            XLinkRegistrationKey key = new XLinkRegistrationKey(id, hostId);
            if (xlinkRegistrations.get(key) != null) {
                notifyAboutDeRegistration(xlinkRegistrations.get(key));
                xlinkRegistrations.remove(key);
            }
        }       
    }
    
    private boolean isRegistered(String id, String hostId) {
        XLinkRegistrationKey key = new XLinkRegistrationKey(id, hostId);
        return xlinkRegistrations.containsKey(key);
    }

    @Override
    public List<XLinkToolRegistration> getXLinkRegistration(String hostId) {
        List<XLinkToolRegistration> registrationsOfHostId = new ArrayList<XLinkToolRegistration>();
        synchronized (xlinkRegistrations) {
            for (XLinkRegistrationKey key : xlinkRegistrations.keySet()) {
                if (key.getHostId().equals(hostId)) {
                    registrationsOfHostId.add(xlinkRegistrations.get(key));
                }
            }
        }
        return registrationsOfHostId;
    }
    
    private Map<ModelDescription, List<XLinkToolView>> convertMapToModelDescription(
            Map<String, List<XLinkToolView>> modelsToViews) throws XLinkConnectException{
        Map<ModelDescription, List<XLinkToolView>> convertedMap 
                = new HashMap<ModelDescription, List<XLinkToolView>>();
        for (String key : modelsToViews.keySet()) {
            try{
                String modelClass = key.substring(0,key.indexOf(":"));
                String version = key.substring(key.indexOf(":"), key.length());
                convertedMap.put(new ModelDescription(modelClass, version), modelsToViews.get(key));
            }catch(Exception e){
                throw new XLinkConnectException("Malformed modelToViews key.");
            }
        }        
        return convertedMap;
    }
    
    @Override
    public XLinkTemplate connectToXLink(
            String id, 
            String hostId, 
            String toolName, 
            Map<String, List<XLinkToolView>> modelsToViews)  throws XLinkConnectException{
        Map<ModelDescription, List<XLinkToolView>> convertedModelsToViews 
                = convertMapToModelDescription(modelsToViews);
        List<XLinkToolRegistration> registrations = getXLinkRegistration(hostId);
        XLinkTemplate template = XLinkUtils.prepareXLinkTemplate(
                xLinkBaseUrl, 
                id, 
                convertedModelsToViews, 
                xLinkExpiresIn, 
                XLinkUtils.getLocalToolFromRegistrations(registrations));
        XLinkToolRegistration newRegistration;
        synchronized (xlinkRegistrations) {
            XLinkRegistrationKey key = new XLinkRegistrationKey(id, hostId);
            newRegistration
                = new XLinkToolRegistration(hostId, id, toolName, convertedModelsToViews, template);
            xlinkRegistrations.put(key, newRegistration);
        }
        notifyAboutRegistration(newRegistration);
        return template;
    }
    
    private void notifyAboutRegistration(XLinkToolRegistration newRegistration) {
        //TODO notify other tools of Host about registration here
    }
    
    private void notifyAboutDeRegistration(XLinkToolRegistration oldRegistration) {
        //TODO notify other tools of Host about deregistration here
    }

    public void setxLinkBaseUrl(String xLinkBaseUrl) {
        this.xLinkBaseUrl = xLinkBaseUrl;
    }

    public void setxLinkExpiresIn(int xLinkExpiresIn) {
        this.xLinkExpiresIn = xLinkExpiresIn;
    }
    
    private class XLinkRegistrationKey {
        private String connectorId;
        private String hostId;

        public XLinkRegistrationKey(String connectorId, String hostId) {
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
            if ((this.connectorId == null) 
                    ? (other.connectorId != null) : !this.connectorId.equals(other.connectorId)) {
                return false;
            }
            if ((this.hostId == null) ? (other.hostId != null) : !this.hostId.equals(other.hostId)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + (this.connectorId != null ? this.connectorId.hashCode() : 0);
            hash = 67 * hash + (this.hostId != null ? this.hostId.hashCode() : 0);
            return hash;
        }
        
        public String getConnectorId() {
            return connectorId;
        }

        public void setConnectorId(String connectorId) {
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
