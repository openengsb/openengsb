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

package org.openengsb.core.services.internal.pseudo;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;

public abstract class PseudoConnectorFactory<PseudoType extends PseudoConnector> implements ConnectorInstanceFactory {

    private DomainProvider domainProvider;
    protected Map<Domain, PseudoType> handlers = new HashMap<Domain, PseudoType>();

    protected PseudoConnectorFactory(DomainProvider domainProvider) {
        this.domainProvider = domainProvider;
    }

    @Override
    public Domain createNewInstance(String id) {
        PseudoType handler = createNewHandler(id);
        Domain newProxyInstance =
            (Domain) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class<?>[]{ domainProvider.getDomainInterface(), }, handler);
        handlers.put(newProxyInstance, handler);
        return newProxyInstance;
    }

    protected abstract PseudoType createNewHandler(String id);

    @Override
    public void applyAttributes(Domain instance, Map<String, String> attributes) {
        PseudoType handler = handlers.get(instance);
        updateHandlerAttributes(handler, attributes);
    }

    protected abstract void updateHandlerAttributes(PseudoType handler, Map<String, String> attributes);

}
