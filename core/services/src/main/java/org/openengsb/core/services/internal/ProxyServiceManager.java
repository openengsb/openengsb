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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.InternalServiceRegistrationManager;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.api.remote.CallRouter;
import org.openengsb.core.api.validation.MultipleAttributeValidationResult;
import org.openengsb.core.api.validation.MultipleAttributeValidationResultImpl;
import org.openengsb.core.common.AbstractServiceManagerParent;
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
public class ProxyServiceManager extends AbstractServiceManagerParent implements InternalServiceRegistrationManager {

    private final DomainProvider provider;

    private final Map<String, ServiceRegistration> services = new HashMap<String, ServiceRegistration>();

    private final CallRouter router;

    public ProxyServiceManager(DomainProvider provider, CallRouter router) {
        this.provider = provider;
        this.router = router;
    }

    @Override
    public ServiceDescriptor getDescriptor() {
        Builder builder =
            ServiceDescriptor.builder(getStrings()).id(provider.getId()).serviceType(getDomainInterface())
                .implementationType(getDomainInterface())
                .name("proxy.name", provider.getName().getString(Locale.getDefault())).description("proxy.description");
        builder.attribute(builder.newAttribute().id("portId").name("proxy.port.id")
            .description("proxy.port.description").build());
        builder.attribute(builder.newAttribute().id("destination").name("proxy.destination.name")
            .description("proxy.destination.description").build());
        builder.attribute(builder.newAttribute().id("serviceId").name("proxy.serviceId.name")
            .description("proxy.serviceId.description").build());
        return builder.build();
    }

    @Override
    protected Class<? extends Domain> getDomainInterface() {
        return provider.getDomainInterface();
    }

    @Override
    public MultipleAttributeValidationResult update(String id, Map<String, String> attributes) {
        synchronized (services) {
            if (!services.containsKey(id)) {
                ProxyConnector handler = new ProxyConnector();
                handler.setCallRouter(router);
                handler.setPortId(attributes.get("portId"));
                String destination = attributes.get("destination");
                String serviceId = attributes.get("serviceId");
                handler.addMetadata("serviceId", serviceId);
                handler.setDestination(destination);
                Domain newProxyInstance =
                    (Domain) Proxy.newProxyInstance(getDomainInterface().getClassLoader(),
                        new Class[]{getDomainInterface()}, handler);
                ServiceRegistration registration =
                    getBundleContext().registerService(
                        new String[]{getDomainInterface().getName(), Domain.class.getName()}, newProxyInstance,
                        createNotificationServiceProperties(id, attributes));
                services.put(id, registration);
            }
        }
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }

    @Override
    public void updateWithoutValidation(String id, Map<String, String> attributes) {
        update(id, attributes);
    }

    @Override
    public Map<String, String> getAttributeValues(String id) {
        return new HashMap<String, String>();
    }

    @Override
    public void delete(String id) {
        synchronized (services) {
            services.get(id).unregister();
            services.remove(id);
        }
    }

    @Override
    protected Class<? extends Domain> getImplementationClass() {
        return provider.getDomainInterface();
    }

    @Override
    public String getInstanceId() {
        return getClass().getName();
    }

}
