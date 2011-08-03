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
import org.openengsb.core.api.PseudoConnectorProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

public class PseudoConnectorManager {

    private static class Registration {

        Registration(PseudoConnectorProvider pseudoConnector, DomainProvider domainProvider,
                ServiceRegistration factoryService) {
            this.pseudoConnector = pseudoConnector;
            this.domainProvider = domainProvider;
            this.factoryService = factoryService;
        }

        private PseudoConnectorProvider pseudoConnector;
        private DomainProvider domainProvider;
        private ServiceRegistration factoryService;
    }

    private Collection<Registration> registeredFactories = Sets.newHashSet();

    private ServiceTracker pseudoConnectorProviderTracker;

    private ServiceTracker domainProviderTracker;

    private BundleContext bundleContext;

    private OsgiUtilsService utilsService;

    public void init() {
        Filter pseudoConnectorFilter = utilsService.makeFilterForClass(PseudoConnectorProvider.class);
        pseudoConnectorProviderTracker =
            new ServiceTracker(bundleContext, pseudoConnectorFilter, new ServiceTrackerCustomizer() {
                @Override
                public void removedService(ServiceReference reference, Object service) {
                    PseudoConnectorProvider provider = (PseudoConnectorProvider) service;
                    Iterator<Registration> factoryServices = getFactoriesForPseudoConnectorForRemoval(provider);
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
                    PseudoConnectorProvider pseudoConnectorProvider =
                        (PseudoConnectorProvider) bundleContext.getService(reference);
                    for (DomainProvider p : getServicesFromTracker(domainProviderTracker, DomainProvider.class)) {
                        registerConnectorFactoryService(pseudoConnectorProvider, p);
                        // TODO create ConnectorProvider for every registered domainProvider
                    }
                    return bundleContext.getService(reference);
                }

            });
        pseudoConnectorProviderTracker.open();

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
                    Collection<PseudoConnectorProvider> pseudoProviders =
                        getServicesFromTracker(pseudoConnectorProviderTracker, PseudoConnectorProvider.class);
                    DomainProvider newProvider = (DomainProvider) bundleContext.getService(reference);
                    for (PseudoConnectorProvider p : pseudoProviders) {
                        registerConnectorFactoryService(p, newProvider);
                    }
                    return newProvider;
                }
            });
        domainProviderTracker.open();
    }

    protected void registerConnectorFactoryService(PseudoConnectorProvider pseudoConnectorProvider,
            DomainProvider p) {
        ConnectorInstanceFactory factory = pseudoConnectorProvider.createFactory(p);
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put(org.openengsb.core.api.Constants.DOMAIN_KEY, p.getId());
        properties.put(org.openengsb.core.api.Constants.CONNECTOR_KEY, pseudoConnectorProvider.getId());
        ServiceRegistration serviceRegistration =
            bundleContext.registerService(ConnectorInstanceFactory.class.getName(), factory, properties);
        registeredFactories.add(new Registration(pseudoConnectorProvider, p, serviceRegistration));
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

    protected Iterator<Registration> getFactoriesForPseudoConnectorForRemoval(final PseudoConnectorProvider provider) {
        Iterator<Registration> consumingIterator = Iterators.consumingIterator(registeredFactories.iterator());
        return Iterators.filter(consumingIterator, new Predicate<Registration>() {
            @Override
            public boolean apply(Registration input) {
                return ObjectUtils.equals(input.pseudoConnector, provider);
            }
        });
    }

    protected Iterator<Registration> getFactoriesForDomainProviderForRemoval(final DomainProvider provider) {
        Iterator<Registration> consumingIterator = Iterators.consumingIterator(registeredFactories.iterator());
        return Iterators.filter(consumingIterator, new Predicate<Registration>() {
            @Override
            public boolean apply(Registration input) {
                return ObjectUtils.equals(input.domainProvider, provider);
            }
        });
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setUtilsService(OsgiUtilsService utilsService) {
        this.utilsService = utilsService;
    }

}
