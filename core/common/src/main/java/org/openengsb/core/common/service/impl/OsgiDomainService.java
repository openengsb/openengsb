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

package org.openengsb.core.common.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.service.DomainService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.context.BundleContextAware;

public class OsgiDomainService implements DomainService, BundleContextAware {

    private Log log = LogFactory.getLog(OsgiDomainService.class);

    private List<DomainProvider> domains;
    private BundleContext bundleContext;

    @Override
    public List<DomainProvider> domains() {
        return new ArrayList<DomainProvider>(this.domains);
    }

    @Override
    public List<ServiceManager> serviceManagersForDomain(Class<? extends Domain> domain) {
        List<ServiceManager> serviceManagers = new ArrayList<ServiceManager>();
        try {
            String filter = "(domain=" + domain.getName() + ")";
            ServiceReference[] allServiceReferences = bundleContext.getAllServiceReferences(
                    ServiceManager.class.getName(), filter);

            if (allServiceReferences != null) {
                for (ServiceReference serviceReference : allServiceReferences) {
                    Object service = bundleContext.getService(serviceReference);
                    serviceManagers.add((ServiceManager) service);
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
            ServiceReference[] allServiceReferences = bundleContext.getAllServiceReferences(domain.getName(),
                    null);
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

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;

    }
}
