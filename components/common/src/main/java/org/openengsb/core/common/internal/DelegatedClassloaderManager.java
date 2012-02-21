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
import java.util.Map;
import java.util.Set;

import org.apache.aries.blueprint.utils.BundleDelegatingClassLoader;
import org.apache.commons.lang.StringUtils;
import org.openengsb.core.api.ClassloadingDelegate;
import org.openengsb.core.api.Constants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
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

    private Map<Bundle, Collection<ServiceRegistration>> classLoaderServices = Maps.newHashMap();
    private BundleContext bundleContext;

    public DelegatedClassloaderManager(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void start() {
        for (Bundle b : bundleContext.getBundles()) {
            String providesClasses = (String) b.getHeaders().get(Constants.PROVIDED_CLASSES);
            if (providesClasses == null) {
                continue;
            }
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

            String providesParents = (String) b.getHeaders().get(Constants.PROVIDED_CLASSES_PARENTS);
            if (providesParents == null) {
                return;
            }
            String[] superClassNames = StringUtils.split(providesParents, ",");
            Set<Class<?>> superClasses = Sets.newHashSet();
            ClassLoader[] loaders = new ClassLoader[]
            { new BundleDelegatingClassLoader(b), getClass().getClassLoader() };
            for (String superClassName : superClassNames) {
                Class<?> superClass = null;
                ClassNotFoundException last = null;
                for (ClassLoader l : loaders) {
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

            Collection<ServiceRegistration> bundleRegistrations = Lists.newLinkedList();
            for (final Class<?> superClass : superClasses) {
                Set<Class<?>> filtered = Sets.filter(classes, new Predicate<Class<?>>() {
                    @Override
                    public boolean apply(Class<?> input) {
                        return superClass.isAssignableFrom(input);
                    }
                });
                ClassloadingDelegate service = new ClassloadingDelegateImpl(filtered);
                Collection<String> filteredNames = Collections2.transform(filtered, new Function<Class<?>, String>() {
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
                bundleRegistrations.add(registration);
            }
            classLoaderServices.put(b, bundleRegistrations);
        }
    }
}
