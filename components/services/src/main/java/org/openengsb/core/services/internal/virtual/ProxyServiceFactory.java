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

package org.openengsb.core.services.internal.virtual;

import java.util.Collections;
import java.util.Map;

import org.openengsb.core.api.Connector;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.remote.OutgoingPortUtilService;
import org.openengsb.core.common.VirtualConnectorFactory;

public class ProxyServiceFactory extends VirtualConnectorFactory<ProxyConnector> {

    private OutgoingPortUtilService outgoingPortUtilService;
    private ProxyConnectorRegistryImpl connectorRegistry;

    protected ProxyServiceFactory(DomainProvider domainProvider, OutgoingPortUtilService outgoingPortUtilService,
            ProxyConnectorRegistryImpl connectorRegistry) {
        super(domainProvider);
        this.outgoingPortUtilService = outgoingPortUtilService;
        this.connectorRegistry = connectorRegistry;
    }

    @Override
    public void updateHandlerAttributes(ProxyConnector handler, Map<String, String> attributes) {
        handler.setPortId(attributes.get("portId"));
        String destination = attributes.get("destination");
        handler.setDestination(destination);
        String serviceId = attributes.get("serviceId");
        handler.addMetadata("serviceId", serviceId);
    }

    @Override
    public void applyAttributes(Connector instance, Map<String, String> attributes) {
        ProxyConnector handler = handlers.get(instance);
        updateHandlerAttributes(handler, attributes);
    }

    @Override
    protected ProxyConnector createNewHandler(String id) {
        ProxyRegistration proxyRegistration = connectorRegistry.create(id);
        return new ProxyConnector(id, outgoingPortUtilService, proxyRegistration);
    }

    @Override
    public Map<String, String> getValidationErrors(Map<String, String> attributes) {
        // TODO OPENENGSB-1290: implement some validation
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getValidationErrors(Connector instance, Map<String, String> attributes) {
        // TODO OPENENGSB-1290: implement some validation
        return Collections.emptyMap();
    }

}
