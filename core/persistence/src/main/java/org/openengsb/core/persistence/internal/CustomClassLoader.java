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

import org.osgi.framework.Bundle;

public class CustomClassLoader extends ClassLoader {

    private Bundle bundle;

    private ClassLoader backUpClassLoader;

    public CustomClassLoader(ClassLoader parent, Bundle bundle) {
        super(parent);
        this.bundle = bundle;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return bundle.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            return tryBackupClassLoader(name);
        }
    }

    private Class<?> tryBackupClassLoader(String name) throws ClassNotFoundException {
        if (backUpClassLoader == null) {
            throw new ClassNotFoundException(getExceptionText(name, false));
        }
        try {
            return backUpClassLoader.loadClass(name);
        } catch (ClassNotFoundException cnfe) {
            throw new ClassNotFoundException(getExceptionText(name, true));
        }
    }

    private String getExceptionText(String name, boolean backupUsed) {
        String message = "CustomClassLoader for OpenEngSB persistence cannot load class with name '" + name
                + "' with default class loader and bundle class loader.";
        if (backupUsed) {
            message += "Backup class loader '" + backUpClassLoader + "' used.";
        } else {
            message += "No backup class loader configured.";
        }
        return message;
    }

    public void setBackUpClassLoader(ClassLoader backUpClassLoader) {
        this.backUpClassLoader = backUpClassLoader;
    }

}
