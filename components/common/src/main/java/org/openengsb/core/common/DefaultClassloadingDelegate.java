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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.ClassloadingDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

/**
 * Helps to provide a Simple Delegated class-loading provider, configurable as a bean.
 */
@Deprecated
public class DefaultClassloadingDelegate<T> implements ClassloadingDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultClassloadingDelegate.class);

    protected Map<String, Class<?>> supported;

    public DefaultClassloadingDelegate(Class<? extends T>... classes) {
        Map<String, Class<?>> supported = Maps.newHashMap();
        for (Class<?> clazz : classes) {
            supported.put(clazz.getName(), clazz);
        }
        this.supported = Collections.unmodifiableMap(supported);
    }

    @Override
    public Class<?> load(String classname) throws ClassNotFoundException {
        if (supported.containsKey(classname)) {
            return supported.get(classname);
        }
        LOGGER.warn("class not found: {}", classname);
        throw new ClassNotFoundException("Type " + classname + " not found in this provider");
    }

    @Override
    public Collection<Class<?>> getSupportedTypes() {
        return supported.values();
    }

    @SuppressWarnings("unchecked")
    public void setClasses(List<String> classnames) {
        /* Classes should all be resolvable by the bundles module class loader */
        Map<String, Class<?>> supported = Maps.newHashMap();
        ClassLoader classLoader = this.getClass().getClassLoader();
        for (String name : classnames) {
            Class<? extends T> clazz;
            try {
                clazz = (Class<? extends T>) classLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
                throw Throwables.propagate(e);
            }
            supported.put(name, clazz);
        }
        this.supported = Collections.unmodifiableMap(supported);
    }

}
