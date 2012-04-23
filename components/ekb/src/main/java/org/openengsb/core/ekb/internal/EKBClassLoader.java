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

package org.openengsb.core.ekb.internal;

import java.util.List;

import org.openengsb.labs.delegation.service.ClassProvider;

/**
 * Simple class loading service for EKB internal usage.
 */
public class EKBClassLoader {
    private List<ClassProvider> providers;

    public EKBClassLoader() {
    }

    /**
     * Tries to load the class with the given class name. First it uses the classloader of the EKB bundle. If the class
     * can't be loaded by this classloader, this function iterates through the ClassProviders which are in the OSGi
     * environment (see lab project about delegated classloading). If the class can't again been loaded, an
     * IllegalArgumentException is thrown.
     */
    public Class<?> loadClass(String className) {
        try {
            return this.getClass().getClassLoader().loadClass(className);
        } catch (Exception e) {
            if (providers != null) {
                try {
                    for (ClassProvider provider : providers) {
                        return provider.loadClass(className);
                    }
                } catch (ClassNotFoundException ex) {
                    // ignore
                }
            }
            throw new IllegalArgumentException("Unable to load class \"" + className + "\"", e);
        }
    }

    public void setProviders(List<ClassProvider> providers) {
        this.providers = providers;
    }
}
