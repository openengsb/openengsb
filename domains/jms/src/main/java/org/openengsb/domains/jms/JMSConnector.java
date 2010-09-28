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
import org.springframework.osgi.context.BundleContextAware;

public class JMSConnector implements BundleContextAware {

    private BundleContext bundleContext;
    private final InvocationHandlerFactory handlerFactory;
    private final DomainService domainService;

    public JMSConnector(DomainService domainService, InvocationHandlerFactory factory) {
        this.domainService = domainService;
        this.handlerFactory = factory;
    }

    public void addProxiesToContext() {
        for (DomainProvider domain : domainService.domains()) {
            Class<? extends Domain> domainInterface = domain.getDomainInterface();
            InvocationHandler handler = handlerFactory.createInstance(domain);
            Object newProxyInstance =
                Proxy.newProxyInstance(domainInterface.getClassLoader(), new Class[]{domainInterface}, handler);
            bundleContext.registerService(domainInterface.getName(), newProxyInstance, null);
        }
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
