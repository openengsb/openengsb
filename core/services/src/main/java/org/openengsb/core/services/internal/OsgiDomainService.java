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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.DomainService;
import org.openengsb.core.api.InternalServiceRegistrationManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class OsgiDomainService implements DomainService {

    private Log log = LogFactory.getLog(OsgiDomainService.class);

    private List<DomainProvider> domains;
    private BundleContext bundleContext;

    @Override
    public List<DomainProvider> domains() {
        return new ArrayList<DomainProvider>(domains);
    }

    @Override
    public InternalServiceRegistrationManager serviceManagerForConnector(String connectorName) {
        try {
            String filter = "(connector=" + connectorName + ")";
            ServiceReference[] serviceReferences =
                bundleContext.getAllServiceReferences(InternalServiceRegistrationManager.class.getName(), filter);
            if (serviceReferences == null || serviceReferences.length == 0) {
                throw new IllegalStateException("No ServiceManager could be retrieved for domain");
            }
            return (InternalServiceRegistrationManager) bundleContext.getService(serviceReferences[0]);
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<InternalServiceRegistrationManager> serviceManagersForDomain(Class<? extends Domain> domain) {
        List<InternalServiceRegistrationManager> serviceManagers = new ArrayList<InternalServiceRegistrationManager>();
        try {
            String filter = "(domain=" + domain.getName() + ")";
            ServiceReference[] allServiceReferences =
                bundleContext.getAllServiceReferences(InternalServiceRegistrationManager.class.getName(), filter);

            if (allServiceReferences != null) {
                for (ServiceReference serviceReference : allServiceReferences) {
                    Object service = bundleContext.getService(serviceReference);
                    serviceManagers.add((InternalServiceRegistrationManager) service);
                }
            }
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException(e);
        }
        return serviceManagers;
    }

    @Override
    public List<ServiceReference> serviceReferencesForDomain(Class<? extends Domain> domain) {
        List<ServiceReference> serviceReferences = new ArrayList<ServiceReference>();
        try {
            ServiceReference[] allServiceReferences = bundleContext.getAllServiceReferences(domain.getName(), null);
            if (allServiceReferences != null) {
                serviceReferences = Arrays.asList(allServiceReferences);
            }
        } catch (InvalidSyntaxException e) {
            throw new IllegalStateException(e);
        }
        return serviceReferences;
    }

    @Override
    public List<? extends ServiceReference> getAllServiceInstances() {
        try {
            ServiceReference[] refs = bundleContext.getAllServiceReferences(Domain.class.getName(), null);
            if (refs == null) {
                return Collections.emptyList();
            }
            return Arrays.asList(refs);
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
        ServiceReference[] refs;
        log.info(String.format("try to find service for class \"%s\", and id \"%s\"", serviceClass, serviceId));
        try {
            refs = bundleContext.getAllServiceReferences(serviceClass, String.format("(id=%s)", serviceId));
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException("could not find service " + serviceId, e);
        }
        if (refs == null) {
            throw new IllegalArgumentException("no services found for class, " + serviceClass);
        }
        if (refs.length > 1) {
            throw new IllegalStateException(String.format("more than one service with id %s found", serviceId));
        }
        return bundleContext.getService(refs[0]);
    }

    public void setDomains(List<DomainProvider> domains) {
        this.domains = domains;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;

    }
}
