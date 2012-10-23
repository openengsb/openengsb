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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.LinkableDomain;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.model.ConnectorConfiguration;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.xlink.exceptions.DomainNotLinkableException;
import org.openengsb.core.api.xlink.service.XLinkConnectorManager;
import org.openengsb.core.api.xlink.model.ModelToViewsTuple;
import org.openengsb.core.api.xlink.model.XLinkConnector;
import org.openengsb.core.api.xlink.model.XLinkConnectorRegistration;
import org.openengsb.core.api.xlink.model.XLinkConnectorView;
import org.openengsb.core.api.xlink.model.XLinkUrlBlueprint;
import org.openengsb.core.services.xlink.XLinkUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import java.util.LinkedList;

public class ConnectorManagerImpl implements XLinkConnectorManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorManagerImpl.class);

    private ConnectorRegistrationManager registrationManager;
    private ConfigPersistenceService configPersistence;
    private OsgiUtilsService utilsService;
    private Map<XLinkRegistrationKey, XLinkConnectorRegistration> xlinkRegistrations
        = new HashMap<XLinkRegistrationKey, XLinkConnectorRegistration>();
    private String xLinkBaseUrl;
    private int xLinkExpiresIn = 3;

    private ExecutorService executor = Executors.newCachedThreadPool();
    private Semaphore semaphore;

    class ConnectorInstaller implements Runnable {
        private ConnectorConfiguration configuration;

        ConnectorInstaller(ConnectorConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    registrationManager.updateRegistration(configuration.getConnectorId(), configuration.getContent());
                    LOGGER.info("successfully recreated connector {} from persistence", configuration.getConnectorId());
                    break;
                } catch (ConnectorValidationFailedException e) {
                    LOGGER.error("connector {} that has been loaded from persistence was found to be invalid",
                            configuration.getConnectorId(), e);
                    break;
                } catch (OsgiServiceNotAvailableException e) {
                    LOGGER.warn("connector {}  that has been loaded from persistence could not be created because the"
                            + "factory-service was missing", configuration.getConnectorId());
                    continue;
                } catch (Exception e) {
                    LOGGER.error("connector {}  that has been loaded from persistence could not be created because of"
                            + "an unexpected Error", e);
                    break;
                }
            }
            semaphore.release();
        }
    }

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
                semaphore = new Semaphore(0);
                for (ConnectorConfiguration c : configs) {
                    executor.submit(new ConnectorInstaller(c));
                }
                try {
                    semaphore.acquire(configs.size());
                } catch (InterruptedException e) {
                    LOGGER.error("interrupted while installing connectors");
                    executor.shutdownNow();
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

    public void setUtilsService(OsgiUtilsService utilsService) {
        this.utilsService = utilsService;
    }
    
    public void setxLinkBaseUrl(String xLinkBaseUrl) {
        this.xLinkBaseUrl = xLinkBaseUrl;
    }

    public void setxLinkExpiresIn(int xLinkExpiresIn) {
        this.xLinkExpiresIn = xLinkExpiresIn;
    }

    @Override
    public void disconnectFromXLink(String id, String hostId) {
        XLinkConnectorRegistration foundReg = null;
        synchronized (xlinkRegistrations) {
            XLinkRegistrationKey key = new XLinkRegistrationKey(id, hostId);
            if (xlinkRegistrations.get(key) != null) {
                foundReg = xlinkRegistrations.get(key);
                xlinkRegistrations.remove(key);
            }
        }  
        if (foundReg != null) {
            notifyAllConnectorsRegisteredOnSameHostAboutDeregistration(foundReg);
        }
    }

    @Override
    public List<XLinkConnectorRegistration> getXLinkRegistration(String hostId) {
        List<XLinkConnectorRegistration> registrationsOfHostId = new ArrayList<XLinkConnectorRegistration>();
        synchronized (xlinkRegistrations) {
            for (XLinkRegistrationKey key : xlinkRegistrations.keySet()) {
                if (key.getRemoteHostIp().equals(hostId)) {
                    registrationsOfHostId.add(xlinkRegistrations.get(key));
                }
            }
        }
        return registrationsOfHostId;
    }
    
    private Map<ModelDescription, XLinkConnectorView[]> convertToMapWithModelDescriptionAsKey(
        List<ModelToViewsTuple> modelsToViews) {
        Map<ModelDescription, XLinkConnectorView[]> convertedMap 
            = new HashMap<ModelDescription, XLinkConnectorView[]>();
        for (ModelToViewsTuple tupel : modelsToViews) {
            convertedMap.put(tupel.getDescription(), tupel.getViews());
        }        
        return convertedMap;
    }
    
    @Override
    public XLinkUrlBlueprint connectToXLink(String connectorIpToLink, String remoteHostIp, 
            String toolName, ModelToViewsTuple[] modelsToViewsArray) {
        checkForConnectorLinkable(connectorIpToLink);
        List<ModelToViewsTuple> modelsToViews = Arrays.asList(modelsToViewsArray);
        Map<ModelDescription, XLinkConnectorView[]> convertedModelsToViews 
            = convertToMapWithModelDescriptionAsKey(modelsToViews);
        List<XLinkConnectorRegistration> registrations = getXLinkRegistration(remoteHostIp);
        XLinkUrlBlueprint template = XLinkUtils.prepareXLinkTemplate(
                xLinkBaseUrl, 
                connectorIpToLink, 
                convertedModelsToViews, 
                xLinkExpiresIn, 
                XLinkUtils.getLocalToolFromRegistrations(registrations));
        XLinkConnectorRegistration newRegistration;
        XLinkRegistrationKey key = new XLinkRegistrationKey(connectorIpToLink, remoteHostIp);
        synchronized (xlinkRegistrations) {
            newRegistration
                = new XLinkConnectorRegistration(remoteHostIp, connectorIpToLink, toolName, 
                        convertedModelsToViews, template);
            xlinkRegistrations.put(key, newRegistration);
        }
        notifyAllConnectorsRegisteredOnSameHostAboutRegistration(key, newRegistration);
        return template;
    }
    
    private void notifyAllConnectorsRegisteredOnSameHostAboutRegistration(
            XLinkRegistrationKey filterKey, XLinkConnectorRegistration newRegistration) {
        List<XLinkConnectorRegistration> hostRegistrations
            = getXLinkRegistration(newRegistration.getHostId());
        for (XLinkConnectorRegistration currentRegistration : hostRegistrations) {
            if (!currentRegistration.getConnectorId().equals(filterKey.getConnectorIpToLink())) {
                XLinkConnector[] registeredTools = addConnectorFromArray(currentRegistration
                    .getxLinkTemplate().getRegisteredTools(), readXLinkConnectorsFromRegistry(newRegistration));            
                currentRegistration.getxLinkTemplate().setRegisteredTools(registeredTools);                
                Object serviceObject 
                    = utilsService.getService("(service.pid=" + currentRegistration.getConnectorId() + ")", 100L);
                if (serviceObject == null) {
                    continue;
                }
                try {
                    LinkableDomain service = (LinkableDomain) serviceObject;
                    service.onRegisteredToolsChanged(registeredTools);
                } catch (ClassCastException e) {
                    
                }
            }
        }
    }
    
    /**
     * Checks if the given ConnectorId is registered and is an instance of a linkable domain.
     */
    private void checkForConnectorLinkable(String connectorId) throws DomainNotLinkableException {
        Object serviceObject 
            = utilsService.getService("(service.pid=" + connectorId + ")", 100L);
        if (serviceObject == null) {
            throw new DomainNotLinkableException("Connector with Id " + connectorId + " was not found.");
        }
        try {
            serviceObject = (LinkableDomain) serviceObject;
        } catch (ClassCastException e) { 
            throw new DomainNotLinkableException("Connector with Id " + connectorId + " was not linkable.");
        }        
    }
    
    private void notifyAllConnectorsRegisteredOnSameHostAboutDeregistration(
            XLinkConnectorRegistration oldRegistration) {
        List<XLinkConnectorRegistration> hostRegistrations
            = getXLinkRegistration(oldRegistration.getHostId());
        for (XLinkConnectorRegistration currentRegistration : hostRegistrations) {
            XLinkConnector[] registeredTools = removeConnectorFromArray(currentRegistration
                .getxLinkTemplate().getRegisteredTools(), readXLinkConnectorsFromRegistry(oldRegistration));            
            currentRegistration.getxLinkTemplate().setRegisteredTools(registeredTools);
            Object serviceObject 
                = utilsService.getService("(service.pid=" + currentRegistration.getConnectorId() + ")", 100L);
            if (serviceObject == null) {
                continue;
            }
            try {
                LinkableDomain service = (LinkableDomain) serviceObject;
                service.onRegisteredToolsChanged(registeredTools);
            } catch (ClassCastException e) {
                
            }
        }
    }
    
    private XLinkConnector[] removeConnectorFromArray(XLinkConnector[] currentArray, 
            XLinkConnector elementToRemove){
        List<XLinkConnector> arrayAsList = new LinkedList<XLinkConnector>();
        for (XLinkConnector currentItem : currentArray) {
            if(!elementToRemove.equals(currentItem)){
                arrayAsList.add(currentItem);
            }
        }
        return arrayAsList.toArray(new XLinkConnector[0]);
    }
    
    private XLinkConnector[] addConnectorFromArray(XLinkConnector[] currentArray, 
            XLinkConnector elementToAdd){
        List<XLinkConnector> arrayAsList = new LinkedList<XLinkConnector>();
        for (XLinkConnector currentItem : currentArray) {
            arrayAsList.add(currentItem);
        }        
        arrayAsList.add(elementToAdd);
        return arrayAsList.toArray(new XLinkConnector[0]);
    }    
    
    private XLinkConnector readXLinkConnectorsFromRegistry(XLinkConnectorRegistration registration) {
        List<XLinkConnectorRegistration> regList = new ArrayList<XLinkConnectorRegistration>();
        regList.add(registration);
        return XLinkUtils.getLocalToolFromRegistrations(regList)[0];
    }
    
    private class XLinkRegistrationKey {
        private String connectorIpToLink;
        private String remoteHostIp;

        public XLinkRegistrationKey(String connectorIpToLink, String remoteHostIp) {
            this.connectorIpToLink = connectorIpToLink;
            this.remoteHostIp = remoteHostIp;
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
            if ((this.connectorIpToLink == null) 
                    ? (other.connectorIpToLink != null) : !this.connectorIpToLink.equals(other.connectorIpToLink)) {
                return false;
            }
            if ((this.remoteHostIp == null) ? (other.remoteHostIp != null) : !this.remoteHostIp.equals(other.remoteHostIp)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + (this.connectorIpToLink != null ? this.connectorIpToLink.hashCode() : 0);
            hash = 59 * hash + (this.remoteHostIp != null ? this.remoteHostIp.hashCode() : 0);
            return hash;
        }

        public String getConnectorIpToLink() {
            return connectorIpToLink;
        }

        public void setConnectorIpToLink(String connectorIpToLink) {
            this.connectorIpToLink = connectorIpToLink;
        }

        public String getRemoteHostIp() {
            return remoteHostIp;
        }

        public void setRemoteHostIp(String remoteHostIp) {
            this.remoteHostIp = remoteHostIp;
        }
        
    }

}
