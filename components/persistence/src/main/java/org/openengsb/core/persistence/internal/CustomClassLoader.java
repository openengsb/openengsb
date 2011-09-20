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

package org.openengsb.core.persistence.internal;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Bundle;

public class CustomClassLoader extends ClassLoader {

    private Bundle bundle;

    private Map<String, Class<?>> classPool = new HashMap<String, Class<?>>();

    public CustomClassLoader(ClassLoader parent, Bundle bundle) {
        super(parent);
        this.bundle = bundle;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return bundle.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            return tryServiceBundles(name);
        }
    }

    private Class<?> tryServiceBundles(String name) throws ClassNotFoundException {
        for (Bundle other : bundle.getBundleContext().getBundles()) {
            Class<?> res = tryServiceBundle(other, name);
            if (res != null) {
                return res;
            }
        }
        return tryBackupSolution(name);
    }

    private Class<?> tryServiceBundle(Bundle otherBundle, String name) {
        if (otherBundle.getState() != Bundle.ACTIVE) {
            return null;
        }
        try {
            return otherBundle.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            return null;
        }
    }

    private Class<?> tryBackupSolution(String name) throws ClassNotFoundException {
        if (classPool.containsKey(name)) {
            return classPool.get(name);
        }
        String message =
            String.format(
                "Class '%s' could not be loaded by bundle class loader of bundle %s and was not found in classpool %s",
                name, bundle.getSymbolicName(), classPool);
        throw new ClassNotFoundException(message);
    }

    public void addClassToPool(Class<?> clazz) {
        classPool.put(clazz.getName(), clazz);
    }

    public void clearClassPool() {
        classPool.clear();
    }

}
