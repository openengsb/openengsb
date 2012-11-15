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

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.lang.ClassUtils;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.MixinDomain;
import org.openengsb.core.api.OpenEngSBService;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.security.model.SecurityAttributeEntry;
import org.openengsb.core.common.SecurityAttributeProviderImpl;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.core.util.DefaultOsgiUtilsService;
import org.openengsb.core.util.FilterUtils;
import org.openengsb.core.util.MapAsDictionary;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ConnectorRegistrationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectorRegistrationManager.class);

    /*
     * These attributes may not be removed from a service.
     */
    private static final List<String> PROTECTED_PROPERTIES = Arrays.asList(
        org.osgi.framework.Constants.SERVICE_ID,
        org.osgi.framework.Constants.SERVICE_PID,
        org.osgi.framework.Constants.OBJECTCLASS,
        Constants.DOMAIN_KEY,
        Constants.CONNECTOR_KEY,
        "location.root");

    private static final Set<String> INTERCEPTOR_BLACKLIST = Sets.newHashSet("authentication", "authorization");

    private static final Class<?>[] CONNECTOR_INSTANCE_COMMON_INTERFACES =
            new Class<?>[]{ OpenEngSBService.class, Domain.class, Connector.class };

    private OsgiUtilsService serviceUtils;
    private BundleContext bundleContext;
    private SecurityAttributeProviderImpl attributeStore;

    private Map<String, ServiceRegistration> registrations = Maps.newHashMap();
    private Map<String, Connector> instances = Maps.newHashMap();

    private MethodInterceptor securityInterceptor;
    private TransformationEngine transformationEngine;

    public ConnectorRegistrationManager(BundleContext bundleContext, TransformationEngine transformationEngine,
            MethodInterceptor securityInterceptor, SecurityAttributeProviderImpl attributeStore) {
        this.bundleContext = bundleContext;
        serviceUtils = new DefaultOsgiUtilsService(bundleContext);
        this.transformationEngine = transformationEngine;
        this.securityInterceptor = securityInterceptor;
        this.attributeStore = attributeStore;
    }

    public void updateRegistration(String id, ConnectorDescription connectorDescription)
        throws ConnectorValidationFailedException {
        if (!instances.containsKey(id)) {
            createService(id, connectorDescription);
        } else if (connectorDescription.getAttributes() != null) {
            updateAttributes(id, connectorDescription);
        }
        updateProperties(id, connectorDescription.getProperties());
    }

    public void forceUpdateRegistration(String id, ConnectorDescription connectorDescription) {
        if (!instances.containsKey(id)) {
            forceCreateService(id, connectorDescription);
        } else if (connectorDescription.getAttributes() == null) {
            forceUpdateAttributes(id, connectorDescription);
        }
        updateProperties(id, connectorDescription.getProperties());
    };

    public void remove(String id) {
        registrations.get(id).unregister();
        registrations.remove(id);
        // FIXME: [OPENENGSB-1809] clean way to shutdown the container
        instances.remove(id);
    }

    private void createService(String id, ConnectorDescription description)
        throws ConnectorValidationFailedException {
        ConnectorInstanceFactory factory = getConnectorFactoryForDescription(description);
        Map<String, String> errors = factory.getValidationErrors(description.getAttributes());
        if (!errors.isEmpty()) {
            throw new ConnectorValidationFailedException(errors);
        }
        finishCreatingInstance(id, description, factory);
    }

    private void forceCreateService(String id, ConnectorDescription description) {
        ConnectorInstanceFactory factory = getConnectorFactoryForDescription(description);
        finishCreatingInstance(id, description, factory);
    }

    private void finishCreatingInstance(String id, ConnectorDescription description,
            ConnectorInstanceFactory factory) {
        Connector serviceInstance = factory.createNewInstance(id.toString());
        if (serviceInstance == null) {
            throw new IllegalStateException("Factory cannot create a new service for instance id " + id.toString());
        }
        serviceInstance = factory.applyAttributes(serviceInstance, description.getAttributes());
        if (!description.getAttributes().containsKey(Constants.SKIP_SET_DOMAIN_TYPE)) {
            serviceInstance.setDomainId(description.getDomainType());
            serviceInstance.setConnectorId(description.getConnectorType());
        }
        instances.put(id, serviceInstance);
        doRegisterServiceInstance(id, description, serviceInstance);
    }

    private void doRegisterServiceInstance(String id, ConnectorDescription description, Connector serviceInstance) {
        if (registrations.containsKey(id)) {
            registrations.get(id).unregister();
            registrations.remove(id);
        }
        Class<? extends Domain> domainInterface = getDomainProvider(description.getDomainType()).getDomainInterface();
        Class<?>[] clazzes = generateInterfaceListForRegistration(domainInterface, serviceInstance);
        Connector proxiedInstance = proxyForTransformation(serviceInstance, domainInterface, clazzes);
        proxiedInstance = proxyForSecurity(id, description, proxiedInstance, clazzes);
        Map<String, Object> properties = populatePropertiesWithRequiredAttributes(id, description);
        ServiceRegistration serviceRegistration =
                bundleContext.registerService(convertClassesToNames(clazzes), proxiedInstance,
                        MapAsDictionary.wrap(properties));
        registrations.put(id, serviceRegistration);
    }

    private Class<?>[] generateInterfaceListForRegistration(Class<? extends Domain> domainInterface,
            Connector serviceInstance) {
        Set<Class<?>> classSet = Sets.newHashSet(CONNECTOR_INSTANCE_COMMON_INTERFACES);
        classSet.add(domainInterface);
        classSet.addAll(getMetaDomainInterfacesFromInstance(serviceInstance));
        return classSet.toArray(new Class<?>[classSet.size()]);
    }

    private Set<Class<?>> getMetaDomainInterfacesFromInstance(Connector serviceInstance) {
        Set<Class<?>> result = Sets.newHashSet();
        for (Class<?> interfaze : serviceInstance.getClass().getInterfaces()) {
            if (interfaze.getAnnotation(MixinDomain.class) != null) {
                result.add(interfaze);
            }
        }
        return result;
    }

    private Connector proxyForSecurity(String id, ConnectorDescription description, Connector serviceInstance,
            Class<?>[] clazzes) {
        if (INTERCEPTOR_BLACKLIST.contains(description.getDomainType())) {
            LOGGER.info("not proxying service because domain is blacklisted: {} ", serviceInstance);
            return serviceInstance;
        }
        if (securityInterceptor == null) {
            LOGGER.warn("security interceptor is not available yet");
            return serviceInstance;
        }
        // @extract-start register-secure-service-code
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setInterfaces(clazzes);
        proxyFactory.addAdvice(securityInterceptor);
        proxyFactory.setTarget(serviceInstance);
        Connector result = (Connector) proxyFactory.getProxy(this.getClass().getClassLoader());
        // @extract-end
        attributeStore.replaceAttributes(serviceInstance, new SecurityAttributeEntry("name", id));
        return result;
    }

    private Connector proxyForTransformation(Connector serviceInstance, Class<? extends Domain> domainInterface,
                                             Class<?>[] clazzes) {
        if (domainInterface.isAssignableFrom(serviceInstance.getClass())) {
            return serviceInstance;
        }
        LOGGER.info("creating transforming proxy for connector-service");
        return (Connector) Proxy.newProxyInstance(domainInterface.getClassLoader(),
                clazzes, new TransformingConnectorHandler(transformationEngine, serviceInstance));

    }

    private String[] convertClassesToNames(Class<?>[] clazzes) {
        List<Class<?>> classList = Arrays.asList(clazzes);
        List<String> list = ClassUtils.convertClassesToClassNames(classList);
        return list.toArray(new String[list.size()]);
    }

    private Map<String, Object> populatePropertiesWithRequiredAttributes(
            String id, ConnectorDescription description) {
        Map<String, Object> properties = description.getProperties();
        Map<String, Object> result = new HashMap<String, Object>(properties);
        for (Entry<String, Object> entry : properties.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        result.put(Constants.DOMAIN_KEY, description.getDomainType());
        result.put(Constants.CONNECTOR_KEY, description.getConnectorType());
        result.put(org.osgi.framework.Constants.SERVICE_PID, id);
        return result;
    }

    private void forceUpdateAttributes(String id, ConnectorDescription description) {
        ConnectorInstanceFactory factory = getConnectorFactoryForDescription(description);
        Connector current = instances.get(id);
        doUpdateAttributes(id, description, factory, current);
    }

    private void updateAttributes(String id, ConnectorDescription description)
        throws ConnectorValidationFailedException {
        ConnectorInstanceFactory factory = getConnectorFactoryForDescription(description);
        Connector current = instances.get(id);
        Map<String, String> validationErrors = factory.getValidationErrors(current, description.getAttributes());
        if (!validationErrors.isEmpty()) {
            throw new ConnectorValidationFailedException(validationErrors);
        }
        doUpdateAttributes(id, description, factory, current);
    }

    private void doUpdateAttributes(String id, ConnectorDescription description,
            ConnectorInstanceFactory factory, Connector current) {
        Connector connector = factory.applyAttributes(current, description.getAttributes());
        if (connector != current) {
            instances.put(id, connector);
            doRegisterServiceInstance(id, description, connector);
        }
    }

    private void updateProperties(String id, Map<String, Object> properties) {
        if (properties == null) {
            return;
        }
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

    protected ConnectorInstanceFactory getConnectorFactoryForDescription(ConnectorDescription description) {
        Filter connectorFilter = FilterUtils.makeFilter(ConnectorInstanceFactory.class,
                String.format("(&(%s=%s)(%s=%s))",
                        Constants.DOMAIN_KEY, description.getDomainType(),
                        Constants.CONNECTOR_KEY, description.getConnectorType()));
        ConnectorInstanceFactory service =
            serviceUtils.getOsgiServiceProxy(connectorFilter, ConnectorInstanceFactory.class);
        return service;
    }

    private DomainProvider getDomainProvider(String domain) {
        Filter domainFilter =
            FilterUtils.makeFilter(DomainProvider.class, String.format("(%s=%s)", Constants.DOMAIN_KEY, domain));
        DomainProvider domainProvider = serviceUtils.getOsgiServiceProxy(domainFilter, DomainProvider.class);
        return domainProvider;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        serviceUtils = new DefaultOsgiUtilsService(bundleContext);
    }

    public void setSecurityInterceptor(MethodInterceptor securityInterceptor) {
        this.securityInterceptor = securityInterceptor;
    }

    public void setAttributeStore(SecurityAttributeProviderImpl attributeStore) {
        this.attributeStore = attributeStore;
    }

}
