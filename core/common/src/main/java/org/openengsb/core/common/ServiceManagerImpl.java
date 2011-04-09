package org.openengsb.core.common;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OpenEngSBService;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.ServiceInstanceFactory;
import org.openengsb.core.api.ServiceManager;
import org.openengsb.core.api.ServiceValidationFailedException;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;

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

public class ServiceManagerImpl implements ServiceManager {

    private OsgiUtilsService serviceUtils = OpenEngSBCoreServices.getServiceUtilsService();
    private BundleContext bundleContext;

    private Map<ConnectorId, ServiceRegistration> registrations = new HashMap<ConnectorId, ServiceRegistration>();

    @Override
    public String getInstanceId() {
        return this.getClass().getName();
    }

    @Override
    public void createService(ConnectorId id, ConnectorDescription description)
        throws ServiceValidationFailedException {
        DomainProvider domainProvider = getDomainProvider(id.getDomainType());
        ServiceInstanceFactory service = getConnectorFactory(id.getConnectorType());

        Domain serviceInstance = service.createServiceInstance(id.getInstanceId(), description.getAttributes());

        String[] clazzes = new String[] {
                OpenEngSBService.class.getName(),
                Domain.class.getName(),
                domainProvider.getDomainInterface().getName(),
        };
        Dictionary<String, Object> properties = description.getProperties();
        ServiceRegistration serviceRegistration = bundleContext.registerService(clazzes, serviceInstance, properties);
        registrations.put(id, serviceRegistration);
    }

    @Override
    public void update(ConnectorId id, ConnectorDescription connectorDescpription)
        throws ServiceValidationFailedException {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void delete(ConnectorId id) {
        registrations.get(id).unregister();
        registrations.remove(id);
    }

    @Override
    public ConnectorDescription getAttributeValues(ConnectorId id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void removeLocations(ConnectorId serviceId, String... locations) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void assignLocations(ConnectorId serviceId, String... locations) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private ServiceInstanceFactory getConnectorFactory(String connectorType) {
        Filter connectorFilter;
        try {
            connectorFilter =
                serviceUtils.makeFilter(ServiceInstanceFactory.class, "(connector=" + connectorType + ")");
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        ServiceInstanceFactory service =
                serviceUtils.getOsgiServiceProxy(connectorFilter, ServiceInstanceFactory.class);
        return service;
    }

    private DomainProvider getDomainProvider(String domain) {
        Filter domainFilter;
        try {
            domainFilter = serviceUtils.makeFilter(DomainProvider.class, "(domain=" + domain + ")");
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        DomainProvider domainProvider = serviceUtils.getOsgiServiceProxy(domainFilter, DomainProvider.class);
        return domainProvider;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}
