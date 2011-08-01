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

public class CompositeConnectorFactory implements ConnectorInstanceFactory {

    private DomainProvider domainProvider;
    private Map<Domain, CompositeConnector> handlers = new HashMap<Domain, CompositeConnector>();

    private static Map<String, CompositeConnectorFactory> instances = new HashMap<String, CompositeConnectorFactory>();

    public static ConnectorInstanceFactory getInstance(DomainProvider domainProvider) {
        if (!instances.containsKey(domainProvider.getId())) {
            instances.put(domainProvider.getId(), new CompositeConnectorFactory(domainProvider));
        }
        return instances.get(domainProvider.getId());
    }

    protected CompositeConnectorFactory(DomainProvider domainProvider) {
        this.domainProvider = domainProvider;
    }

    private void updateHandlerAttributes(CompositeConnector handler, Map<String, String> attributes) {
        // TODO set service-list from attributes
        // TODO set composite-handler form attributes
    }

    @Override
    public void applyAttributes(Domain instance, Map<String, String> attributes) {
        CompositeConnector handler = handlers.get(instance);
        updateHandlerAttributes(handler, attributes);
    }

    @Override
    public Domain createNewInstance(String id) {
        CompositeConnector handler = new CompositeConnector(id);
        Domain newProxyInstance =
            (Domain) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class<?>[]{ domainProvider.getDomainInterface(), }, handler);
        handlers.put(newProxyInstance, handler);
        return newProxyInstance;
    }

    @Override
    public Map<String, String> getValidationErrors(Map<String, String> attributes) {
        // TODO OPENENGSB-1290: implement some validation
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getValidationErrors(Domain instance, Map<String, String> attributes) {
        // TODO OPENENGSB-1290: implement some validation
        return Collections.emptyMap();
    }

}
