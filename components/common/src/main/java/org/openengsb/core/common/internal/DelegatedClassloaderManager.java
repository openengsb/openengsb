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
package org.openengsb.core.common.internal;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.apache.aries.blueprint.utils.BundleDelegatingClassLoader;
import org.apache.commons.lang.StringUtils;
import org.openengsb.core.api.ClassloadingDelegate;
import org.openengsb.core.api.Constants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class DelegatedClassloaderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DelegatedClassloaderManager.class);

    private ConcurrentMap<Bundle, Collection<ServiceRegistration>> classLoaderServices = Maps.newConcurrentMap();
    private BundleContext bundleContext;

    public DelegatedClassloaderManager(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void start() {
        BundleListener bundleListener = new BundleListener() {
            @Override
            public void bundleChanged(BundleEvent event) {
                if (event.getType() == BundleEvent.STOPPED) {
                    handleBundleUninstall(event.getBundle());
                } else if (event.getType() == BundleEvent.STARTED) {
                    handleBundleInstall(event.getBundle());
                }
            }
        };
        bundleContext.addBundleListener(bundleListener);
        for (Bundle b : bundleContext.getBundles()) {
            if (b.getState() == Bundle.ACTIVE) {
                handleBundleInstall(b);
            }
        }
    }

    protected synchronized void handleBundleUninstall(Bundle bundle) {
        if (!classLoaderServices.containsKey(bundle)) {
            LOGGER.warn("tried to unregister ClassloadingDelegate for unknown bundle");
            return;
        }
        for (ServiceRegistration registration : classLoaderServices.get(bundle)) {
            registration.unregister();
        }
    }

    private synchronized void handleBundleInstall(Bundle b) {
        if (classLoaderServices.containsKey(b)) {
            LOGGER.warn("tried to register Classloader-delegate for already registered bundle");
            return;
        }
        String providesClasses = (String) b.getHeaders().get(Constants.PROVIDED_CLASSES);
        if (providesClasses == null) {
            return;
        }
        Set<Class<?>> classes = getProvidedClasses(b, providesClasses);

        String providesParents = (String) b.getHeaders().get(Constants.PROVIDED_CLASSES_PARENTS);
        if (providesParents == null) {
            return;
        }
        Set<Class<?>> superClasses = getProvidedParentClasses(b, providesParents);

        createAllDelegates(b, classes, superClasses);
    }

    private Collection<ServiceRegistration> createAllDelegates(Bundle b, Set<Class<?>> classes,
            Set<Class<?>> superClasses) {
        Collection<ServiceRegistration> bundleRegistrations = Lists.newLinkedList();
        for (final Class<?> superClass : superClasses) {
            Set<Class<?>> filtered = Sets.filter(classes, new Predicate<Class<?>>() {
                @Override
                public boolean apply(Class<?> input) {
                    return superClass.isAssignableFrom(input);
                }
            });
            ServiceRegistration registration = doCreateDelegate(superClass, filtered);
            bundleRegistrations.add(registration);
        }
        classLoaderServices.put(b, bundleRegistrations);
        return bundleRegistrations;
    }

    private ServiceRegistration doCreateDelegate(final Class<?> superClass, Set<Class<?>> providedClasses) {
        ClassloadingDelegate service = new ClassloadingDelegateImpl(providedClasses);
        Collection<String> filteredNames = Collections2.transform(providedClasses, new Function<Class<?>, String>() {
            @Override
            public String apply(Class<?> input) {
                return input.getName();
            }
        });
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put(Constants.PROVIDED_CLASSES_PARENTS_KEY, superClass.getName());
        properties.put(Constants.PROVIDED_CLASSES_KEY, filteredNames);
        ServiceRegistration registration =
            bundleContext.registerService(ClassloadingDelegate.class.getName(), service,
                properties);
        return registration;
    }

    private Set<Class<?>> getProvidedParentClasses(Bundle b, String providesParents) {
        Set<Class<?>> superClasses = Sets.newHashSet();
        for (String superClassName : StringUtils.split(providesParents, ",")) {
            Class<?> superClass = null;
            ClassNotFoundException last = null;
            ClassLoader[] classloaders =
                new ClassLoader[]{ new BundleDelegatingClassLoader(b), getClass().getClassLoader() };
            for (ClassLoader l : classloaders) {
                try {
                    superClass = l.loadClass(superClassName);
                } catch (ClassNotFoundException e) {
                    last = e;
                }
                if (superClass == null) {
                    LOGGER.error(String.format(
                        "Could not load class %s during initializing ClassloadingDelegate for %s", superClassName,
                        b.getSymbolicName()), last);
                }
            }
            superClasses.add(superClass);
        }
        return superClasses;
    }

    private Set<Class<?>> getProvidedClasses(Bundle b, String providesClasses) {
        Set<Class<?>> classes = Sets.newHashSet();
        for (String className : StringUtils.split(providesClasses, ",")) {
            try {
                classes.add(b.loadClass(className));
            } catch (ClassNotFoundException e) {
                LOGGER.error(String.format(
                    "Could not load class %s during initializing ClassloadingDelegate for %s", className,
                    b.getSymbolicName()), e);
            }
        }
        return classes;
    }
}
