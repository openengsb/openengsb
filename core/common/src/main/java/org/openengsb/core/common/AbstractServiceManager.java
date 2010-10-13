/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.common;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.openengsb.core.common.connectorsetupstore.ConnectorSetupStore;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.l10n.BundleStrings;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.context.BundleContextAware;

/**
 * Base class for {@link ServiceManager} implementations. Handles all OSGi related stuff and exporting the right service
 * properties that are needed for service discovery. Furthermore this class also persists the connector state and
 * restores all persisted connectors at the next startup.
 *
 * All service-specific action, like descriptor building, service instantiation and service updating are encapsulated in
 * a {@link ServiceInstanceFactory}. Creating a new service manager should be as simple as implementing the
 * {@link ServiceInstanceFactory} and creating a subclass of this class:
 *
 * This class has to be instantiated via Spring, as the BundleContext has to be set as it is BundleContextAware.
 *
 * <pre>
 * public class ExampleServiceManager extends AbstractServiceManager&lt;ExampleDomain, TheInstanceType&gt; {
 *     public ExampleServiceManager(ServiceInstanceFactory&lt;ExampleDomain, TheInstanceType&gt; factory) {
 *         super(factory);
 *     }
 * }
 * </pre>
 *
 * @param <DomainType> interface of the domain this service manages
 * @param <InstanceType> actual service implementation this service manages
 */
public abstract class AbstractServiceManager<DomainType extends Domain, InstanceType extends DomainType> implements
        ServiceManager, BundleContextAware {

    private final class DomainRepresentation {
        private final InstanceType service;
        private final ServiceRegistration registration;

        private DomainRepresentation(InstanceType service, ServiceRegistration registration) {
            this.service = service;
            this.registration = registration;
        }
    }

    private BundleContext bundleContext;
    private BundleStrings strings;
    private final Map<String, DomainRepresentation> services = new HashMap<String, DomainRepresentation>();
    private final ServiceInstanceFactory<DomainType, InstanceType> factory;
    private final Map<String, Map<String, String>> attributeValues = new HashMap<String, Map<String, String>>();
    private ConnectorSetupStore connectorSetupStore;

    public AbstractServiceManager(ServiceInstanceFactory<DomainType, InstanceType> factory) {
        this.factory = factory;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        strings = new BundleStrings(bundleContext.getBundle());
    }

    public void init() {
        Set<String> storedConnectors = connectorSetupStore.getStoredConnectors(getImplementationClass().getName());
        for (String id : storedConnectors) {
            Map<String, String> setup = connectorSetupStore.loadConnectorSetup(getImplementationClass().getName(), id);
            if (setup != null) {
                update(id, setup);
            }
        }
    }

    @Override
    public ServiceDescriptor getDescriptor() {
        return factory.getDescriptor(ServiceDescriptor.builder(strings).id(getImplementationClass().getName())
            .serviceType(getDomainInterface()).implementationType(getImplementationClass()));
    }

    @Override
    public MultipleAttributeValidationResult update(String id, Map<String, String> attributes) {
        synchronized (services) {
            MultipleAttributeValidationResult result;
            if (!services.containsKey(id)) {
                result = createService(id, attributes);
            } else {
                result = updateService(id, attributes);
            }
            if (attributeValues.containsKey(id)) {
                attributeValues.get(id).putAll(attributes);
            } else {
                attributeValues.put(id, new HashMap<String, String>(attributes));
            }
            if (result.isValid()) {
                connectorSetupStore
                    .storeConnectorSetup(getImplementationClass().getName(), id, attributeValues.get(id));
            }
            return result;
        }
    }

    private MultipleAttributeValidationResult updateService(String id, Map<String, String> attributes) {
        MultipleAttributeValidationResult validation = factory.updateValidation(services.get(id).service, attributes);
        if (validation.isValid()) {
            factory.updateServiceInstance(services.get(id).service, attributes);
        }
        return validation;
    }

    private MultipleAttributeValidationResult createService(String id, Map<String, String> attributes) {
        MultipleAttributeValidationResult validation = factory.createValidation(id, attributes);
        if (validation.isValid()) {
            InstanceType instance = factory.createServiceInstance(id, attributes);
            Hashtable<String, String> serviceProperties = createNotificationServiceProperties(id);
            ServiceRegistration registration =
                bundleContext.registerService(new String[]{ getImplementationClass().getName(),
                    getDomainInterface().getName(), Domain.class.getName() }, instance, serviceProperties);
            services.put(id, new DomainRepresentation(instance, registration));
        }
        return validation;
    }

    @Override
    public void delete(String id) {
        synchronized (services) {
            services.get(id).registration.unregister();
            services.remove(id);
            attributeValues.remove(id);
            connectorSetupStore.deleteConnectorSetup(getImplementationClass().getName(), id);
        }
    }

    @SuppressWarnings("unchecked")
    protected Class<DomainType> getDomainInterface() {
        return (Class<DomainType>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    protected Class<InstanceType> getImplementationClass() {
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        Type instanceType = genericSuperclass.getActualTypeArguments()[1];
        return (Class<InstanceType>) instanceType;
    }

    private Hashtable<String, String> createNotificationServiceProperties(String id) {
        Hashtable<String, String> serviceProperties = new Hashtable<String, String>();
        serviceProperties.put("id", id);
        serviceProperties.put("domain", getDomainInterface().getName());
        serviceProperties.put("class", getImplementationClass().getName());
        serviceProperties.put("managerId", getDescriptor().getId());
        return serviceProperties;
    }

    @Override
    public Map<String, String> getAttributeValues(String id) {
        Map<String, String> returnValues = new HashMap<String, String>();
        synchronized (attributeValues) {
            if (attributeValues.containsKey(id)) {
                Map<String, String> attributes = attributeValues.get(id);
                returnValues.putAll(attributes);
            } else {
                throw new IllegalArgumentException("the specified service instance does not exist");
            }
        }
        return returnValues;
    }

    public void setConnectorSetupStore(ConnectorSetupStore connectorSetupStore) {
        this.connectorSetupStore = connectorSetupStore;
    }

}
