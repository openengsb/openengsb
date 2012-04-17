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

import java.util.Collection;

import org.openengsb.labs.delegation.service.ClassProvider;
import org.openengsb.labs.delegation.service.Constants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

public class DelegatedClassLoadingHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatedClassLoadingHelper.class);

    private BundleContext bundleContext;

    public DelegatedClassLoadingHelper(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public DelegatedClassLoadingHelper() {
    }

    public Collection<Class<?>> getAllKnownSubTypes(String superType) {
        LOGGER.info("queryKnownSubTypes for {}", superType);
        Collection<Class<?>> result = Sets.newHashSet();
        ServiceReference[] serviceReferences;
        try {
            serviceReferences = bundleContext.getAllServiceReferences(ClassProvider.class.getName(), null);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        if (serviceReferences == null) {
            return result;
        }
        for (ServiceReference r : serviceReferences) {
            ClassProvider service = (ClassProvider) bundleContext.getService(r);
            Collection<Class<?>> allClasses = service.listClasses();
            result.addAll(Collections2.filter(allClasses, new Predicate<Class<?>>() {
                @Override
                public boolean apply(Class<?> input) {
                    return true;
                }
            }));
        }
        return result;
    }

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            // was worth a try
        }
        try {
            return getClass().getClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            // was worth a try
        }
        String filter = String.format("(%s=%s)", Constants.PROVIDED_CLASSES_KEY, name);
        ServiceReference[] serviceReferences;
        try {
            serviceReferences = bundleContext.getServiceReferences(ClassProvider.class.getName(), filter);
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }
        if (serviceReferences == null) {
            throw new ClassNotFoundException("found no ClassloadingDelegates for type " + name);
        }
        ClassProvider service = (ClassProvider) bundleContext.getService(serviceReferences[0]);

        return service.loadClass(name);
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
