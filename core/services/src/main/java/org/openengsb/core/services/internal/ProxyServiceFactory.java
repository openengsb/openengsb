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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.common.OpenEngSBCoreServices;

public class ProxyServiceFactory implements ConnectorInstanceFactory {

    private CallRouter router = OpenEngSBCoreServices.getServiceUtilsService().getOsgiServiceProxy(CallRouter.class);
    private DomainProvider domainProvider;
    private Map<Domain, ProxyConnector> handlers = new HashMap<Domain, ProxyConnector>();

    private static Map<DomainProvider, ProxyServiceFactory> instances =
        new HashMap<DomainProvider, ProxyServiceFactory>();

    public static ConnectorInstanceFactory getInstance(DomainProvider domainProvider) {
        if (!instances.containsKey(domainProvider)) {
            instances.put(domainProvider, new ProxyServiceFactory(domainProvider));
        }
        return instances.get(domainProvider);
    }

    protected ProxyServiceFactory(DomainProvider domainProvider) {
        this.domainProvider = domainProvider;
    }

    private void updateHandlerAttributes(ProxyConnector handler, Map<String, String> attributes) {
        handler.setPortId(attributes.get("portId"));
        String destination = attributes.get("destination");
        handler.setDestination(destination);
        String serviceId = attributes.get("serviceId");
        handler.addMetadata("serviceId", serviceId);
    }

    @Override
    public void applyAttributes(Domain instance, Map<String, String> attributes) {
        ProxyConnector handler = handlers.get(instance);
        updateHandlerAttributes(handler, attributes);
    }

    public void setRouter(CallRouter router) {
        this.router = router;
    }

    @Override
    public Domain createNewInstance(String id) {
        ProxyConnector handler = new ProxyConnector(id);
        handler.setCallRouter(router);
        Domain newProxyInstance =
            (Domain) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class<?>[]{ domainProvider.getDomainInterface(), },
                handler);
        handlers.put(newProxyInstance, handler);
        return newProxyInstance;
    }

    @Override
    public Map<String, String> getValidationErrors(Map<String, String> attributes) {
        // TODO implement some validation
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getValidationErrors(Domain instance, Map<String, String> attributes) {
        // TODO implement some validation
        return Collections.emptyMap();
    }

}
