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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.ConnectorRegistrationManager;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OpenEngSBService;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceRegistration;

public class ConnectorRegistrationManagerImpl implements ConnectorRegistrationManager {

    private OsgiUtilsService serviceUtils = OpenEngSBCoreServices.getServiceUtilsService();
    private BundleContext bundleContext;

    private Map<ConnectorId, ServiceRegistration> registrations = new HashMap<ConnectorId, ServiceRegistration>();
    private Map<ConnectorId, Domain> instances = new HashMap<ConnectorId, Domain>();

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

        Dictionary<String, Object> properties = connectorDescription.getProperties();
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

        Dictionary<String, Object> properties = connectorDescription.getProperties();
        if (properties != null) {
            updateProperties(id, properties);
        }
    };

    @Override
    public void remove(ConnectorId id) {
        registrations.get(id).unregister();
        registrations.remove(id);
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

        Domain serviceInstance = factory.createNewInstance(id.toString());
        factory.applyAttributes(serviceInstance, description.getAttributes());

        finishCreatingInstance(id, description, domainProvider, factory);
    }

    private void finishCreatingInstance(ConnectorId id, ConnectorDescription description,
            DomainProvider domainProvider, ConnectorInstanceFactory factory) {
        Domain serviceInstance = factory.createNewInstance(id.toString());
        factory.applyAttributes(serviceInstance, description.getAttributes());

        String[] clazzes = new String[]{
            OpenEngSBService.class.getName(),
            Domain.class.getName(),
            domainProvider.getDomainInterface().getName(),
        };

        Dictionary<String, Object> properties =
            populatePropertiesWithRequiredAttributes(description.getProperties(), id);
        ServiceRegistration serviceRegistration = bundleContext.registerService(clazzes, serviceInstance, properties);
        registrations.put(id, serviceRegistration);
        instances.put(id, serviceInstance);
    }

    private Dictionary<String, Object> populatePropertiesWithRequiredAttributes(Dictionary<String, Object> properties,
            ConnectorId id) {
        properties.put(Constants.ID_KEY, id.getDomainType());
        properties.put(Constants.CONNECTOR_KEY, id.getConnectorType());
        properties.put(Constants.ID_KEY, id.toFullID());
        if (properties.get("location.root") == null) {
            properties.put("location.root", new String[]{ id.getInstanceId() });
        }
        String currentContextLocation = "location." + ContextHolder.get().getCurrentContextId();
        if (properties.get(currentContextLocation) == null) {
            properties.put(currentContextLocation, new String[0]);
        }
        return properties;
    }

    private void forceUpdateAttributes(ConnectorId id, Map<String, String> attributes) {
        ConnectorInstanceFactory factory = getConnectorFactory(id);
        factory.applyAttributes(instances.get(id), attributes);
    }

    private void updateProperties(ConnectorId id, Dictionary<String, Object> properties) {
        ServiceRegistration registration = registrations.get(id);
        registration.setProperties(properties);
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
        if (connectorType.equals(Constants.EXTERNAL_CONNECTOR_PROXY)) {
            DomainProvider domainProvider = getDomainProvider(id.getDomainType());
            return ProxyServiceFactory.getInstance(domainProvider);
        }
        Filter connectorFilter =
                serviceUtils.makeFilter(ConnectorInstanceFactory.class,
                    String.format("(%s=%s)", Constants.CONNECTOR_KEY, connectorType));
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

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
