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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.l10n.BundleStrings;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.context.BundleContextAware;

public abstract class AbstractServiceManagerParent<DomainType extends Domain, InstanceType extends DomainType>
        implements BundleContextAware {

    protected final class DomainRepresentation {
        private final InstanceType service;
        final ServiceRegistration registration;

        private DomainRepresentation(InstanceType service, ServiceRegistration registration) {
            this.service = service;
            this.registration = registration;
        }
    }

    private BundleContext bundleContext;
    private BundleStrings strings;
    private final Map<String, DomainRepresentation> services = new HashMap<String, DomainRepresentation>();

    public AbstractServiceManagerParent() {
        super();
    }

    protected final BundleStrings getStrings() {
        return strings;
    }

    public final BundleContext getBundleContext() {
        return bundleContext;
    }

    protected boolean servicesContainsKey(String id) {
        return this.services.containsKey(id);
    }

    public final Map<String, DomainRepresentation> getServices() {
        return services;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        strings = new BundleStrings(bundleContext.getBundle());
    }

    public void delete(String id) {
        synchronized (services) {
            services.get(id).registration.unregister();
            services.remove(id);
            deleteOnChild(id);
        }
    }

    protected void addDomainRepresentation(String id, InstanceType instance, ServiceRegistration registration) {
        services.put(id, new DomainRepresentation(instance, registration));
    }

    protected InstanceType getService(String id) {
        return services.get(id).service;
    }

    protected Hashtable<String, String> createNotificationServiceProperties(String id) {
        Hashtable<String, String> serviceProperties = new Hashtable<String, String>();
        serviceProperties.put("id", id);
        serviceProperties.put("domain", getDomainInterface().getName());
        serviceProperties.put("class", getImplementationClass().getName());
        serviceProperties.put("managerId", getDescriptor().getId());
        return serviceProperties;
    }

    protected abstract ServiceDescriptor getDescriptor();

    @SuppressWarnings("unchecked")
    protected Class<DomainType> getDomainInterface() {
        return (Class<DomainType>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @SuppressWarnings("unchecked")
    protected Class<InstanceType> getImplementationClass() {
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        Type instanceType = genericSuperclass.getActualTypeArguments()[1];
        return (Class<InstanceType>) instanceType;
    }

    protected abstract void deleteOnChild(String id);

}
