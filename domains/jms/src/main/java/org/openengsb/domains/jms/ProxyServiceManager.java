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

package org.openengsb.domains.jms;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.l10n.BundleStrings;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.core.common.validation.MultipleAttributeValidationResultImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Proxy Service Manager to instantiate Proxies to communicate with external systems.
 *
 * The proxy for the specified Domain are created upon request for a ServiceDescriptor.
 *
 * The ProxyServiceManager is completely generic. Business logic to interpret a certain call is handled via the
 *
 * @see InvocationHandler handed to the constructor.
 */
public class ProxyServiceManager implements ServiceManager {

    private static final class DomainRepresentation {
        private final ServiceRegistration registration;

        private DomainRepresentation(ServiceRegistration registration) {
            this.registration = registration;
        }
    }

    private final Log log = LogFactory.getLog(ProxyServiceManager.class);
    private final BundleContext bundleContext;
    private final BundleStrings strings;
    private final DomainProvider provider;
    private final Map<String, DomainRepresentation> services = new HashMap<String, DomainRepresentation>();
    private final InvocationHandler handler;

    public ProxyServiceManager(DomainProvider provider, InvocationHandler handler, BundleContext context) {
        this.provider = provider;
        this.handler = handler;
        this.bundleContext = context;
        strings = new BundleStrings(bundleContext.getBundle());
    }

    @Override
    public ServiceDescriptor getDescriptor() {
        log.info("BundleString-getDescriptor: " + strings);
        return ServiceDescriptor.builder(strings).id(provider.getId()).serviceType(getDomainInterface())
            .implementationType(getDomainInterface()).name("jms.name", getDomainInterface().getCanonicalName())
            .description("jms.description").build();
    }

    private Class<? extends Domain> getDomainInterface() {
        return provider.getDomainInterface();
    }

    @Override
    public MultipleAttributeValidationResult update(String id, Map<String, String> attributes) {
        synchronized (services) {
            if (!services.containsKey(id)) {
                Domain newProxyInstance =
                    (Domain) Proxy.newProxyInstance(getDomainInterface().getClassLoader(),
                        new Class[]{ getDomainInterface() }, handler);
                ServiceRegistration registration =
                    bundleContext.registerService(
                        new String[]{ getDomainInterface().getName(), Domain.class.getName() },
                        newProxyInstance, createNotificationServiceProperties(id));
                services.put(id, new DomainRepresentation(registration));
            }
        }
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }

    @Override
    public void delete(String id) {
        synchronized (services) {
            services.get(id).registration.unregister();
            services.remove(id);
        }
    }

    private Hashtable<String, String> createNotificationServiceProperties(String id) {
        Hashtable<String, String> serviceProperties = new Hashtable<String, String>();
        serviceProperties.put("id", id);
        serviceProperties.put("domain", getDomainInterface().getName());
        serviceProperties.put("class", "Proxy");
        serviceProperties.put("managerId", getDescriptor().getId());
        return serviceProperties;
    }

    @Override
    public Map<String, String> getAttributeValues(String id) {
        return new HashMap<String, String>();
    }
}
