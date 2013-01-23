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
package org.openengsb.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompositeClassLoader extends ClassLoader {

    private List<ClassLoader> loaders;

    public CompositeClassLoader(ClassLoader parent, ClassLoader... loaders) {
        super(parent);
        this.loaders = new ArrayList<ClassLoader>(Arrays.asList(loaders));
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        for (ClassLoader cl : loaders) {
            try {
                return cl.loadClass(name);
            } catch (ClassNotFoundException e) {
                // ignore and try the next one
            }
        }
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(name);
        } catch (ClassNotFoundException e) {
            // ignore and let the parent worry about loading that class
        }
        return super.loadClass(name);
    }

    public void add(ClassLoader loader) {
        loaders.add(loader);
    }
}
