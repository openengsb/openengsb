/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.ui.web.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.config.DomainProvider;
import org.openengsb.core.config.ServiceManager;
import org.openengsb.ui.web.service.DomainService;
import org.openengsb.ui.web.service.ManagedServices;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class OsgiManagedServices implements ManagedServices {

    Log log = LogFactory.getLog(OsgiManagedServices.class);

    private List<ServiceManager> managedServices;

    private BundleContext bundleContext;

    private DomainService domainService;

    public void setDomainService(DomainService domainService) {
        this.domainService = domainService;
    }

    public OsgiManagedServices() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public List<ServiceManager> getManagedServices() {
        return managedServices;
    }

    public void setManagedServices(List<ServiceManager> managedServices) {
        this.managedServices = managedServices;
    }

    public Map<Class<?>, List<ServiceReference>> getManagedServiceInstances() {
        Map<Class<?>, List<ServiceReference>> managedInstances = new HashMap<Class<?>, List<ServiceReference>>();
        for (DomainProvider provider : domainService.getDomains()) {
            log.debug("Provider: " + provider.getName());
            String name = provider.getDomainInterface().getName();
            ServiceReference[] allServiceReferences;
            try {
                allServiceReferences = bundleContext.getAllServiceReferences(name, null);
                log.debug("ServiceReferences: " + allServiceReferences.length);
                managedInstances.put(provider.getDomainInterface(), Arrays.asList(allServiceReferences));
            } catch (InvalidSyntaxException e) {
                log.error(e.getMessage());
            }
        }
        return managedInstances;
    }

    public void setBundleContext(BundleContext context) {
        this.bundleContext = context;
    }
}
