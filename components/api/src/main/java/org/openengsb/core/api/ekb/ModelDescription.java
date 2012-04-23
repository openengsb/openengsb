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

package org.openengsb.core.api.ekb;

import org.osgi.framework.Version;

/**
 * The model description class defines the unique description of a model by the model class name and the version of the
 * model class. The version is equals to the version of the bundle containing the model class. If no version is given,
 * 1.0.0 will be used. All versions have the form x.y.z where x,y and z are integers.
 */
public class ModelDescription {
    private String modelClassName;
    private Version version;

    public ModelDescription(String modelClassName, Version version) {
        this.modelClassName = modelClassName;
        this.version = version;
    }

    public ModelDescription(String modelClassName, String versionString) {
        this(modelClassName, Version.parseVersion(versionString));
    }
    
    public ModelDescription(String modelClassName) {
        this(modelClassName, new Version(1, 0, 0));
    }

    public ModelDescription(Class<?> modelClass, Version version) {
        this(modelClass.getName(), version);
    }

    public ModelDescription(Class<?> modelClass, String versionString) {
        this(modelClass.getName(), Version.parseVersion(versionString));
    }
    
    public ModelDescription(Class<?> modelClass) {
        this(modelClass.getName(), new Version(1, 0, 0));
    }

    public String getModelClassName() {
        return modelClassName;
    }

    public String getModelVersionString() {
        return version.toString();
    }

    public Version getModelVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((modelClassName == null) ? 0 : modelClassName.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ModelDescription)) {
            return false;
        }
        ModelDescription other = (ModelDescription) obj;
        if (!(other.getModelClassName().equals(this.getModelClassName()))) {
            return false;
        }
        if (!(other.getModelVersion().equals(this.getModelVersion()))) {
            return false;
        }
        return true;
    }
}
