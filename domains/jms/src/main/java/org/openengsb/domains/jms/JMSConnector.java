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

import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.service.DomainService;
import org.osgi.framework.BundleContext;

public class JMSConnector {

    public JMSConnector(BundleContext context, DomainService domainService, InvocationHandlerFactory factory) {
        for (DomainProvider domain : domainService.domains()) {
            Class<? extends Domain> domainInterface = domain.getDomainInterface();
            InvocationHandler handler = factory.createInstance(domain);
            Object newProxyInstance =
                Proxy.newProxyInstance(domainInterface.getClassLoader(), new Class[]{domainInterface}, handler);
            context.registerService(domainInterface.getName(), newProxyInstance, null);
        }
    }
}
