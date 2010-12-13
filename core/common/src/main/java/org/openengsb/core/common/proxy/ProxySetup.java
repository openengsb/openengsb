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

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.communication.CallRouter;
import org.openengsb.core.common.service.DomainService;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

public class ProxySetup implements BundleContextAware {

    Log log = LogFactory.getLog(ProxySetup.class);

    private BundleContext bundleContext;
    private final DomainService domainService;

    private CallRouter router;

    public ProxySetup(DomainService domainService) {
        this.domainService = domainService;
    }

    public void addProxiesToContext() {
        for (DomainProvider domain : domainService.domains()) {
            addProxyServiceManager(domain);
        }
    }

    private void addProxyServiceManager(DomainProvider domain) {
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put("domain", domain.getDomainInterface().getName());
        ProxyServiceManager service = new ProxyServiceManager(domain, router);
        service.setBundleContext(bundleContext);
        bundleContext.registerService(ServiceManager.class.getName(), service, properties);
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
