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

package org.openengsb.core.persistence.internal;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class DelegatingClassLoader extends ClassLoader {

    private List<ClassLoader> classloaders = new ArrayList<ClassLoader>();
    private BundleContext bundleContext;

    public DelegatingClassLoader(ClassLoader parent, BundleContext bundleContext) {
        super(parent);
        this.bundleContext = bundleContext;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        for (ClassLoader loader : classloaders) {
            try {
                return loader.loadClass(name);
            } catch (ClassNotFoundException cnfe) {
                // ignore and continue with the next class loader
            }
        }
        return findClassWithBundleContext(name);
    }

    private Class<?> findClassWithBundleContext(String name) throws ClassNotFoundException {
        Bundle[] bundles = bundleContext.getBundles();
        for (Bundle bundle : bundles) {
            try {
                if (bundle.getState() == Bundle.ACTIVE) {
                    bundle.loadClass(name);
                }
            } catch (ClassNotFoundException cnfe) {
                // ignore and continue with the next bundle
            }
        }
        throw new ClassNotFoundException("No class definition found for class with name: " + name);
    }

    public void addClassLoader(ClassLoader loader) {
        this.classloaders.add(loader);
    }
}
