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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.OpenEngSBService;
import org.openengsb.core.api.ServiceInstanceFactory;
import org.openengsb.core.api.ServiceManager;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.persistence.ConnectorDomainPair;
import org.openengsb.core.api.persistence.ConnectorSetupStore;
import org.openengsb.core.api.validation.MultipleAttributeValidationResult;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;

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
public abstract class AbstractServiceManager<DomainType extends Domain, InstanceType extends DomainType> extends
        AbstractServiceManagerParent implements ServiceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServiceManager.class);

    private final ServiceInstanceFactory<DomainType, InstanceType> factory;
    private final Map<String, Map<String, String>> attributeValues = new HashMap<String, Map<String, String>>();
    private ConnectorSetupStore connectorSetupStore;

    private final Map<String, DomainRepresentation> services = new HashMap<String, DomainRepresentation>();
    protected Advice securityInterceptor;

    public AbstractServiceManager(ServiceInstanceFactory<DomainType, InstanceType> factory) {
        this.factory = factory;
    }

    public void init() {
        ConnectorDomainPair connectorDomainPair = getDomainConnectorPair();
        Set<String> storedConnectors = connectorSetupStore.getStoredConnectors(connectorDomainPair);
        for (String id : storedConnectors) {
            Map<String, String> setup = connectorSetupStore.loadConnectorSetup(connectorDomainPair, id);
            if (setup != null) {
                update(id, setup);
            }
        }
    }

    @Override
    public ServiceDescriptor getDescriptor() {
        return factory.getDescriptor(ServiceDescriptor.builder(getStrings()).id(getImplementationClass().getName())
            .serviceType(getDomainInterface()).implementationType(getImplementationClass()));
    }

    @Override
    public synchronized void updateWithoutValidation(String id, Map<String, String> attributes) {
        updateServiceInstance(id, attributes);
    }

    @Override
    public synchronized MultipleAttributeValidationResult update(String id, Map<String, String> attributes) {
        MultipleAttributeValidationResult validateService;
        if (isAlreadyCreated(id)) {
            validateService = factory.updateValidation(getService(id), attributes);
        } else {
            validateService = factory.createValidation(id, attributes);
        }
        if (validateService.isValid()) {
            updateServiceInstance(id, attributes);
        }
        return validateService;
    }

    private void updateServiceInstance(String id, Map<String, String> attributes) {
        synchronized (services) {
            if (isAlreadyCreated(id)) {
                updateService(id, attributes);
            } else {
                createService(id, attributes);
            }
            if (attributeValues.containsKey(id)) {
                attributeValues.get(id).putAll(attributes);
            } else {
                attributeValues.put(id, new HashMap<String, String>(attributes));
            }
            connectorSetupStore.storeConnectorSetup(getDomainConnectorPair(), id, attributeValues.get(id));
        }
    }

    private boolean isAlreadyCreated(String id) {
        return services.containsKey(id);
    }

    private void updateService(String id, Map<String, String> attributes) {
        InstanceType service = getService(id);
        factory.updateServiceInstance(service, attributes);
    }

    private void createService(String id, Map<String, String> attributes) {
        InstanceType instance = factory.createServiceInstance(id, attributes);
        Hashtable<String, String> serviceProperties = createNotificationServiceProperties(id, attributes);
        final String[] interfaces =
            new String[]{getDomainInterface().getName(), Domain.class.getName(), OpenEngSBService.class.getName()};
        ServiceRegistration registration =
            getBundleContext().registerService(interfaces, secureService(instance), serviceProperties);
        addDomainRepresentation(id, instance, registration);

    }

    @SuppressWarnings("unchecked")
    private InstanceType secureService(InstanceType instance) {
        ProxyFactory factory = new ProxyFactory(instance);
        if (securityInterceptor == null) {
            securityInterceptor = new MethodInterceptor() {
                @Override
                public Object invoke(MethodInvocation invocation) throws Throwable {
                    LOGGER.error("This service manager has no security-manager attached");
                    return invocation.proceed();
                }
            };
        }
        factory.addAdvice(securityInterceptor);
        ClassLoader classLoader = getClass().getClassLoader();
        LOGGER.info("creating aop-proxy using classloader {} ({})", classLoader, classLoader.getClass());
        return (InstanceType) factory.getProxy(classLoader);
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

    protected boolean servicesContainsKey(String id) {
        return this.services.containsKey(id);
    }

    protected void addDomainRepresentation(String id, InstanceType instance, ServiceRegistration registration) {
        services.put(id, new DomainRepresentation(instance, registration));
    }

    protected InstanceType getService(String id) {
        synchronized (services) {
            InstanceType service = services.get(id).service;
            return service;
        }
    }

    @Override
    public synchronized void delete(String id) {
        synchronized (services) {
            final DomainRepresentation domainRepresentation = services.get(id);
            domainRepresentation.registration.unregister();
            services.remove(id);
            attributeValues.remove(id);
            connectorSetupStore.deleteConnectorSetup(getDomainConnectorPair(), id);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<DomainType> getDomainInterface() {
        return (Class<DomainType>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Class<InstanceType> getImplementationClass() {
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        Type instanceType = genericSuperclass.getActualTypeArguments()[1];
        return (Class<InstanceType>) instanceType;
    }

    private ConnectorDomainPair getDomainConnectorPair() {
        String domain = getDomainInterface().getName();
        String connector = getImplementationClass().getName();
        return new ConnectorDomainPair(domain, connector);
    }

    protected final class DomainRepresentation {
        private final InstanceType service;
        final ServiceRegistration registration;

        private DomainRepresentation(InstanceType service, ServiceRegistration registration) {
            this.service = service;
            this.registration = registration;
        }
    }

    public void setSecurityInterceptor(Advice securityInterceptor) {
        this.securityInterceptor = securityInterceptor;
    }

    @Override
    public String getInstanceId() {
        return getClass().getName();
    }
}
