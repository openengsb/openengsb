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

import java.util.Dictionary;
import java.util.Hashtable;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainEvents;
import org.openengsb.core.api.DomainProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainProviderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainProviderManager.class);

    private BundleContext bundleContext;

    private BundleTracker bundleTracker;

    public void start() {
        bundleTracker = new BundleTracker(bundleContext, Bundle.ACTIVE, new BundleTrackerCustomizer() {

            @Override
            public void removedBundle(Bundle bundle, BundleEvent event, Object object) {
            }

            @Override
            public void modifiedBundle(Bundle bundle, BundleEvent event, Object object) {
            }

            @Override
            public Object addingBundle(Bundle bundle, BundleEvent event) {
                LOGGER.trace("checking whether Bundle {} is a domain", bundle);
                Dictionary<String, String> headers = bundle.getHeaders();
                String name = headers.get(Constants.DOMAIN_NAME_HEADER);
                if (name == null) {
                    LOGGER.trace("Bundle {} is not a domain, ignoring", bundle);
                    return null;
                }
                return registerDomainProvider(bundle, name);
            }

        });
        bundleTracker.open();
    }

    public void stop() {
        bundleTracker.close();
    }

    @SuppressWarnings("unchecked")
    private Class<? extends DomainEvents> loadDomainEventsInterfaceFromBundle(Bundle bundle) {
        String domainEventsInterfaceName = bundle.getHeaders().get(Constants.DOMAIN_EVENTS_INTERFACE_HEADER);
        if (domainEventsInterfaceName == null) {
            LOGGER.info("Domain-bundle {} has no DomainEvents-interface. There are no Events in this domain.", bundle);
            return null;
        }
        Class<?> domainEventsInterface;
        try {
            domainEventsInterface = bundle.loadClass(domainEventsInterfaceName);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Could not load DomainEvents interface for bundle {}", bundle, e);
            return null;
        }
        if (!DomainEvents.class.isAssignableFrom(domainEventsInterface)) {
            LOGGER.error("Bundle {} has an invalid Domain interface. It must be derived from {}",
                bundle, DomainEvents.class.getName());
            return null;
        }
        return (Class<? extends DomainEvents>) domainEventsInterface;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Domain> loadDomainInterfaceFromBundle(Bundle bundle) {
        String domainInterfaceName = bundle.getHeaders().get(Constants.DOMAIN_INTERFACE_HEADER);
        if (domainInterfaceName == null) {
            LOGGER.info("Domain-bundle {} has no domain-interface. There are no operations in this domain.", bundle);
            return null;
        }
        Class<?> domainInterface;
        try {
            domainInterface = bundle.loadClass(domainInterfaceName);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Could not load domain interface for bundle {}", bundle, e);
            return null;
        }
        if (!Domain.class.isAssignableFrom(domainInterface)) {
            LOGGER.error("Bundle {} has an invalid Domain interface. It must be derived from {}",
                bundle, Domain.class.getName());
            return null;
        }
        return (Class<? extends Domain>) domainInterface;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    private Object registerDomainProvider(Bundle bundle, String name) {
        Class<? extends Domain> domainInterface = loadDomainInterfaceFromBundle(bundle);
        Class<? extends DomainEvents> domainEventsInterface = loadDomainEventsInterfaceFromBundle(bundle);
        DomainProvider provider =
            new DefaultDomainProvider(name, bundle, domainInterface, domainEventsInterface);
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("domain", name);
        LOGGER.debug("registering DomainProvider for Bundle {} with properties {}", bundle, props);
        return bundle.getBundleContext().registerService(DomainProvider.class, provider, props);
    }

}
