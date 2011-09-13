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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.aopalliance.intercept.MethodInterceptor;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.ConnectorRegistrationManager;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OpenEngSBService;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.common.util.MapAsDictionary;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.aop.framework.ProxyFactory;

public class ConnectorRegistrationManagerImpl implements ConnectorRegistrationManager {

    private MethodInterceptor interceptor;

    /*
     * These attributes may not be removed from a service.
     */
    private static final List<String> PROTECTED_PROPERTIES = Arrays.asList(
        org.osgi.framework.Constants.SERVICE_ID,
        org.osgi.framework.Constants.SERVICE_PID,
        org.osgi.framework.Constants.OBJECTCLASS,
        Constants.ID_KEY,
        Constants.DOMAIN_KEY,
        Constants.CONNECTOR_KEY,
        "location.root");

    private OsgiUtilsService serviceUtils;
    private BundleContext bundleContext;

    private Map<ConnectorId, ServiceRegistration> registrations = new HashMap<ConnectorId, ServiceRegistration>();
    private Map<ConnectorId, Connector> instances = new HashMap<ConnectorId, Connector>();

    @Override
    public String getInstanceId() {
        return this.getClass().getName();
    }

    @Override
    public void updateRegistration(ConnectorId id, ConnectorDescription connectorDescription)
        throws ConnectorValidationFailedException {
        if (!instances.containsKey(id)) {
            createService(id, connectorDescription);
        } else if (connectorDescription.getAttributes() != null) {
            updateAttributes(id, connectorDescription.getAttributes());
        }

        Map<String, Object> properties = connectorDescription.getProperties();
        if (properties != null) {
            updateProperties(id, properties);
        }
    }

    @Override
    public void forceUpdateRegistration(ConnectorId id, ConnectorDescription connectorDescription) {
        if (!instances.containsKey(id)) {
            forceCreateService(id, connectorDescription);
        } else if (connectorDescription.getAttributes() == null) {
            forceUpdateAttributes(id, connectorDescription.getAttributes());
        }

        Map<String, Object> properties = connectorDescription.getProperties();
        if (properties != null) {
            updateProperties(id, properties);
        }
    };

    @Override
    public void remove(ConnectorId id) {
        registrations.get(id).unregister();
        registrations.remove(id);
        // FIXME: [OPENENGSB-1809] clean way to shutdown the container
        instances.remove(id);
    }

    private void createService(ConnectorId id, ConnectorDescription description)
        throws ConnectorValidationFailedException {
        DomainProvider domainProvider = getDomainProvider(id.getDomainType());
        ConnectorInstanceFactory factory = getConnectorFactory(id);

        Map<String, String> errors = factory.getValidationErrors(description.getAttributes());
        if (!errors.isEmpty()) {
            throw new ConnectorValidationFailedException(errors);
        }

        finishCreatingInstance(id, description, domainProvider, factory);
    }

    private void forceCreateService(ConnectorId id, ConnectorDescription description) {
        DomainProvider domainProvider = getDomainProvider(id.getDomainType());
        ConnectorInstanceFactory factory = getConnectorFactory(id);

        Connector serviceInstance = factory.createNewInstance(id.toString());
        factory.applyAttributes(serviceInstance, description.getAttributes());

        finishCreatingInstance(id, description, domainProvider, factory);
    }

    private void finishCreatingInstance(ConnectorId id, ConnectorDescription description,
            DomainProvider domainProvider, ConnectorInstanceFactory factory) {
        Connector serviceInstance = factory.createNewInstance(id.toString());
        if (serviceInstance == null) {
            throw new IllegalStateException("Factory cannot create a new service for instance id " + id.toString());
        }
        factory.applyAttributes(serviceInstance, description.getAttributes());
        
        serviceInstance.setDomainId(id.getDomainType());
        serviceInstance.setConnectorId(id.getConnectorType());

        String[] clazzes = new String[]{
            OpenEngSBService.class.getName(),
            Domain.class.getName(),
            domainProvider.getDomainInterface().getName(),
        };

        Map<String, Object> properties = populatePropertiesWithRequiredAttributes(description.getProperties(), id);
        Object secureInstance = secureService(serviceInstance);
        ServiceRegistration serviceRegistration =
            bundleContext.registerService(clazzes, secureInstance, MapAsDictionary.wrap(properties));

        registrations.put(id, serviceRegistration);
        instances.put(id, serviceInstance);
    }

    private Object secureService(Domain serviceInstance) {
        ClassLoader orig = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        ProxyFactory factory = new ProxyFactory(serviceInstance);
        factory.addAdvice(interceptor);
        Object result = factory.getProxy();
        Thread.currentThread().setContextClassLoader(orig);
        return result;

    }

    private Map<String, Object> populatePropertiesWithRequiredAttributes(
            final Map<String, Object> properties, ConnectorId id) {
        Map<String, Object> result = new HashMap<String, Object>(properties);
        for (Entry<String, Object> entry : properties.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        result.put(Constants.DOMAIN_KEY, id.getDomainType());
        result.put(Constants.CONNECTOR_KEY, id.getConnectorType());
        result.put(Constants.ID_KEY, id.toFullID());
        return result;
    }

    private void forceUpdateAttributes(ConnectorId id, Map<String, String> attributes) {
        ConnectorInstanceFactory factory = getConnectorFactory(id);
        factory.applyAttributes(instances.get(id), attributes);
    }

    private void updateProperties(ConnectorId id, Map<String, Object> properties) {
        Map<String, Object> newProps = new HashMap<String, Object>(properties);
        ServiceRegistration registration = registrations.get(id);
        ServiceReference reference = registration.getReference();
        for (String key : PROTECTED_PROPERTIES) {
            if (newProps.get(key) == null) {
                Object originalValue = reference.getProperty(key);
                if (originalValue != null) {
                    newProps.put(key, originalValue);
                }
            }
        }
        registration.setProperties(MapAsDictionary.wrap(newProps));
    }

    private void updateAttributes(ConnectorId id, Map<String, String> attributes)
        throws ConnectorValidationFailedException {
        ConnectorInstanceFactory factory = getConnectorFactory(id);
        Map<String, String> validationErrors = factory.getValidationErrors(instances.get(id), attributes);
        if (!validationErrors.isEmpty()) {
            throw new ConnectorValidationFailedException(validationErrors);
        }
        factory.applyAttributes(instances.get(id), attributes);
    }

    protected ConnectorInstanceFactory getConnectorFactory(ConnectorId id) {
        String connectorType = id.getConnectorType();
        Filter connectorFilter =
            serviceUtils.makeFilter(ConnectorInstanceFactory.class,
                String.format("(&(%s=%s)(%s=%s))", Constants.DOMAIN_KEY, id.getDomainType(), Constants.CONNECTOR_KEY,
                    connectorType));
        ConnectorInstanceFactory service =
            serviceUtils.getOsgiServiceProxy(connectorFilter, ConnectorInstanceFactory.class);
        return service;
    }

    private DomainProvider getDomainProvider(String domain) {
        Filter domainFilter =
            serviceUtils.makeFilter(DomainProvider.class, String.format("(%s=%s)", Constants.DOMAIN_KEY, domain));
        DomainProvider domainProvider = serviceUtils.getOsgiServiceProxy(domainFilter, DomainProvider.class);
        return domainProvider;
    }

    public void setServiceUtils(OsgiUtilsService serviceUtils) {
        this.serviceUtils = serviceUtils;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setInterceptor(MethodInterceptor interceptor) {
        this.interceptor = interceptor;
    }
}
