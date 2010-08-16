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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.config.Domain;
import org.openengsb.core.config.DomainProvider;
import org.openengsb.core.config.ServiceManager;
import org.openengsb.ui.web.service.DomainService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class OsgiDomainService implements DomainService {

    private List<DomainProvider> domains;
    private final BundleContext bundleContext;

    Log log = LogFactory.getLog(OsgiDomainService.class);

    public OsgiDomainService(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setDomains(List<DomainProvider> domains) {
        this.domains = new ArrayList<DomainProvider>(domains);
    }

    @Override
    public List<DomainProvider> domains() {
        return new ArrayList<DomainProvider>(this.domains);
    }

    @Override
    public List<ServiceManager> serviceManagersForDomain(Class<? extends Domain> domain) {
        List<ServiceManager> serviceManagers = new ArrayList<ServiceManager>();
        try {
            ServiceReference[] allServiceReferences = bundleContext.getAllServiceReferences(
                    ServiceManager.class.getName(), "(domain=" + domain.getName() + ")");

            if (allServiceReferences != null) {
                for (ServiceReference serviceReference : allServiceReferences) {
                    Object service = bundleContext.getService(serviceReference);
                    if (service instanceof ServiceManager) {
                        serviceManagers.add((ServiceManager) service);
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            log.debug(e.getMessage());
        }
        return serviceManagers;
    }

    @Override
    public List<ServiceReference> serviceReferencesForConnector(Class<? extends Domain> connectorClass) {
        List<ServiceReference> serviceReferences = new ArrayList<ServiceReference>();
        try {
            ServiceReference[] allServiceReferences = bundleContext.getAllServiceReferences(connectorClass.getName(),
                    null);
            if (allServiceReferences != null) {
                serviceReferences = Arrays.asList(allServiceReferences);
            }
        } catch (InvalidSyntaxException e) {
            log.debug(e.getMessage());
        }
        return serviceReferences;
    }

    @Override
    public List<? extends ServiceReference> getManagedServiceInstances() {
        try {
            return Arrays.asList(bundleContext.getAllServiceReferences(Domain.class.getName(), null));
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException("this should never happen, since no filter is used, or is it?");
        }
    }

    @Override
    public Object getService(ServiceReference reference) {
        return bundleContext.getService(reference);
    }

    @Override
    public Object getService(String serviceClass, String serviceId) {
        return null;
    }
}
