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

package org.openengsb.core.common.proxy;

import org.openengsb.core.common.BundleContextAware;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.communication.CallRouter;
import org.osgi.framework.BundleContext;

public class ProxyFactoryImpl implements BundleContextAware, ProxyFactory {

    private CallRouter callRouter;
    private BundleContext bundleContext;

    @Override
    public ProxyServiceManager createProxyForDomain(DomainProvider provider) {
        ProxyServiceManager proxyServiceManager = new ProxyServiceManager(provider, callRouter);
        proxyServiceManager.setBundleContext(bundleContext);
        return proxyServiceManager;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setCallRouter(CallRouter callRouter) {
        this.callRouter = callRouter;
    }
}
