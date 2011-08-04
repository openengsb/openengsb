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

package org.openengsb.core.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.VirtualConnectorProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

public class VirtualConnectorManager {

    private static class Registration {

        protected Registration(VirtualConnectorProvider virtualConnector, DomainProvider domainProvider,
                ServiceRegistration factoryService) {
            this.virtualConnector = virtualConnector;
            this.domainProvider = domainProvider;
            this.factoryService = factoryService;
        }

        private VirtualConnectorProvider virtualConnector;
        private DomainProvider domainProvider;
        private ServiceRegistration factoryService;
    }

    private Collection<Registration> registeredFactories = Sets.newHashSet();

    private ServiceTracker virtualConnectorProviderTracker;

    private ServiceTracker domainProviderTracker;

    private BundleContext bundleContext;

    private OsgiUtilsService utilsService;

    public void init() {
        Filter virtualConnectorFilter = utilsService.makeFilterForClass(VirtualConnectorProvider.class);
        virtualConnectorProviderTracker =
            new ServiceTracker(bundleContext, virtualConnectorFilter, new ServiceTrackerCustomizer() {
                @Override
                public void removedService(ServiceReference reference, Object service) {
                    VirtualConnectorProvider provider = (VirtualConnectorProvider) service;
                    Iterator<Registration> factoryServices = getFactoriesForVirtualConnectorForRemoval(provider);
                    while (factoryServices.hasNext()) {
                        Registration r = factoryServices.next();
                        r.factoryService.unregister();
                    }
                }

                @Override
                public void modifiedService(ServiceReference reference, Object service) {
                    // do nothing
                }

                @Override
                public Object addingService(ServiceReference reference) {
                    createNewFactoryForVirtualConnectorProvider(reference);
                    return bundleContext.getService(reference);
                }

            });
        virtualConnectorProviderTracker.open();

        Filter domainProviderFilter = utilsService.makeFilterForClass(DomainProvider.class);
        domainProviderTracker =
            new ServiceTracker(bundleContext, domainProviderFilter, new ServiceTrackerCustomizer() {
                @Override
                public void removedService(ServiceReference reference, Object service) {
                    Iterator<Registration> factoryServices =
                        getFactoriesForDomainProviderForRemoval((DomainProvider) service);
                    while (factoryServices.hasNext()) {
                        Registration r = factoryServices.next();
                        r.factoryService.unregister();
                    }
                }

                @Override
                public void modifiedService(ServiceReference reference, Object service) {
                    // do nothing
                }

                @Override
                public Object addingService(ServiceReference reference) {
                    DomainProvider newProvider = (DomainProvider) bundleContext.getService(reference);
                    createNewFactoryForDomainProvider(newProvider);
                    return newProvider;
                }

            });
        domainProviderTracker.open();
        Object[] services = domainProviderTracker.getServices();
        if (services != null) {
            for (Object service : services) {
                createNewFactoryForDomainProvider((DomainProvider) service);
            }
        }
    }

    protected void registerConnectorFactoryService(VirtualConnectorProvider virtualConnectorProvider,
            DomainProvider p) {
        ConnectorInstanceFactory factory = virtualConnectorProvider.createFactory(p);
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(org.openengsb.core.api.Constants.DOMAIN_KEY, p.getId());

        properties.put(org.openengsb.core.api.Constants.CONNECTOR_KEY, virtualConnectorProvider.getId());
        ServiceRegistration serviceRegistration =
            bundleContext.registerService(ConnectorInstanceFactory.class.getName(), factory, properties);
        registeredFactories.add(new Registration(virtualConnectorProvider, p, serviceRegistration));
    }

    protected static <T> Collection<T> getServicesFromTracker(ServiceTracker tracker, Class<T> serviceClass) {
        Collection<T> result = new ArrayList<T>();
        if (tracker == null) {
            return result;
        }
        Object[] services = tracker.getServices();
        if (services != null) {
            CollectionUtils.addAll(result, services);
        }
        return result;

    }

    private Iterator<Registration> getFactoriesForVirtualConnectorForRemoval(final VirtualConnectorProvider provider) {
        Iterator<Registration> consumingIterator = Iterators.consumingIterator(registeredFactories.iterator());
        return Iterators.filter(consumingIterator, new Predicate<Registration>() {
            @Override
            public boolean apply(Registration input) {
                return ObjectUtils.equals(input.virtualConnector, provider);
            }
        });
    }

    private Iterator<Registration> getFactoriesForDomainProviderForRemoval(final DomainProvider provider) {
        Iterator<Registration> consumingIterator = Iterators.consumingIterator(registeredFactories.iterator());
        return Iterators.filter(consumingIterator, new Predicate<Registration>() {
            @Override
            public boolean apply(Registration input) {
                return ObjectUtils.equals(input.domainProvider, provider);
            }
        });
    }

    private void createNewFactoryForVirtualConnectorProvider(ServiceReference reference) {
        VirtualConnectorProvider virtualConnectorProvider =
            (VirtualConnectorProvider) bundleContext.getService(reference);
        for (DomainProvider p : getServicesFromTracker(domainProviderTracker, DomainProvider.class)) {
            registerConnectorFactoryService(virtualConnectorProvider, p);
            // TODO create ConnectorProvider for every registered domainProvider
        }
    }

    private void createNewFactoryForDomainProvider(DomainProvider newProvider) {
        Collection<VirtualConnectorProvider> virtualProviders =
            getServicesFromTracker(virtualConnectorProviderTracker, VirtualConnectorProvider.class);
        for (VirtualConnectorProvider p : virtualProviders) {
            registerConnectorFactoryService(p, newProvider);
        }
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setUtilsService(OsgiUtilsService utilsService) {
        this.utilsService = utilsService;
    }

}
