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
import org.openengsb.core.api.ServiceInstanceFactory;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.api.remote.CallRouter;
import org.openengsb.core.common.OpenEngSBCoreServices;

public class ProxyServiceFactory implements ServiceInstanceFactory {

    private CallRouter router = OpenEngSBCoreServices.getServiceUtilsService().getOsgiServiceProxy(CallRouter.class);
    private DomainProvider domainProvider;
    private Map<Domain, ProxyConnector> handlers = new HashMap<Domain, ProxyConnector>();

    private static Map<DomainProvider, ProxyServiceFactory> instances =
        new HashMap<DomainProvider, ProxyServiceFactory>();

    public static ServiceInstanceFactory getInstance(DomainProvider domainProvider) {
        if (!instances.containsKey(domainProvider)) {
            instances.put(domainProvider, new ProxyServiceFactory(domainProvider));
        }
        return instances.get(domainProvider);
    }

    public ProxyServiceFactory(DomainProvider domainProvider) {
        this.domainProvider = domainProvider;
    }

    @Override
    public ServiceDescriptor getDescriptor(Builder builder) {
        builder.id(domainProvider.getId()).serviceType(domainProvider.getDomainInterface())
            .implementationType(domainProvider.getDomainInterface())
            .name("proxy.name", domainProvider.getName().getString(Locale.getDefault()))
            .description("proxy.description");
        builder.attribute(builder.newAttribute().id("portId").name("proxy.port.id")
            .description("proxy.port.description").build());
        builder.attribute(builder.newAttribute().id("destination").name("proxy.destination.name")
            .description("proxy.destination.description").build());
        builder.attribute(builder.newAttribute().id("serviceId").name("proxy.serviceId.name")
            .description("proxy.serviceId.description").build());
        return builder.build();
    }

    @Override
    public Domain createServiceInstance(String id, Map<String, String> attributes) {
        ProxyConnector handler = new ProxyConnector(id);
        handler.setCallRouter(router);
        updateHandlerAttributes(handler, attributes);
        Domain newProxyInstance =
            (Domain) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class<?>[]{ domainProvider.getDomainInterface(), },
                handler);
        newProxyInstance.hashCode();
        handlers.put(newProxyInstance, handler);
        return newProxyInstance;
    }

    private void updateHandlerAttributes(ProxyConnector handler, Map<String, String> attributes) {
        handler.setPortId(attributes.get("portId"));
        String destination = attributes.get("destination");
        handler.setDestination(destination);
        String serviceId = attributes.get("serviceId");
        handler.addMetadata("serviceId", serviceId);
    }

    @Override
    public void updateServiceInstance(Domain instance, Map<String, String> attributes) {
        ProxyConnector handler = handlers.get(instance);
        updateHandlerAttributes(handler, attributes);
    }

    public void setRouter(CallRouter router) {
        this.router = router;
    }

    @Override
    public void updateServiceInstance(Domain instance, Map<String, String> attributes, boolean validate) {
        updateServiceInstance(instance, attributes);
    }

    @Override
    public Domain createServiceInstance(String id, Map<String, String> attributes, boolean validate) {
        return createServiceInstance(id, attributes);
    }

}
