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

package org.openengsb.core.common;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.openengsb.core.common.context.ContextService;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.osgi.context.BundleContextAware;

public class DefaultDomainProxyFactoryBean implements BundleContextAware, FactoryBean<Domain> {

    private Class<? extends Domain> domainInterface;
    private BundleContext bundleContext;
    private String domainName;
    private ContextService context;

    private ForwardHandler makeHandler() {
        ForwardHandler handler = new ForwardHandler();
        String domainInterfaceName = domainInterface.getName();
        handler.setDomainInterfaceName(domainInterfaceName);
        handler.setDomainName(domainName);
        handler.setContext(context);
        handler.setBundleContext(bundleContext);
        return handler;
    }

    public void setDomainInterface(Class<? extends Domain> domainInterface) {
        this.domainInterface = domainInterface;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public void setContext(ContextService context) {
        this.context = context;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public Domain getObject() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?>[] classes = new Class<?>[]{ Domain.class, domainInterface, };
        InvocationHandler handler = makeHandler();
        return (Domain) Proxy.newProxyInstance(classLoader, classes, handler);
    }

    @Override
    public Class<? extends Domain> getObjectType() {
        return domainInterface;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
