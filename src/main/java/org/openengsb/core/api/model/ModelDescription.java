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

package org.openengsb.core.api.model;

import org.osgi.framework.Version;

import com.google.common.base.Objects;

/**
 * The model description class defines the unique description of a model by the model class name and the version of the
 * model class. The version is equals to the version of the bundle containing the model class. If no version is given,
 * 1.0.0 will be used. All versions have the form x.y.z where x,y and z are integers.
 */
public class ModelDescription {
    private String modelClassName;
    private String versionString;

    public ModelDescription() {
    }
    
    public ModelDescription(String modelClassName, String versionString) {
        this.modelClassName = modelClassName;
        this.versionString = checkVersionString(versionString);
    }

    public ModelDescription(String modelClassName, Version version) {
        this.modelClassName = modelClassName;
        this.versionString = version.toString();
    }

    public ModelDescription(String modelClassName) {
        this(modelClassName, new Version(1, 0, 0));
    }

    public ModelDescription(Class<?> modelClass, Version version) {
        this(modelClass.getName(), version);
    }

    public ModelDescription(Class<?> modelClass, String versionString) {
        this(modelClass.getName(), versionString);
    }

    public ModelDescription(Class<?> modelClass) {
        this(modelClass.getName(), new Version(1, 0, 0));
    }

    public String getModelClassName() {
        return modelClassName;
    }

    public void setModelClassName(String modelClassName) {
        this.modelClassName = modelClassName;
    }

    public String getVersionString() {
        return versionString;
    }

    public void setVersion(Version version) {
        this.versionString = version.toString();
    }

    public void setVersionString(String versionString) {
        this.versionString = checkVersionString(versionString);
    }

    private String checkVersionString(String versionString) {
        try {
            return Version.parseVersion(versionString).toString();
        } catch (IllegalArgumentException e) {
            String errorMessage =
                String.format("%s is not a valid version string for a model description", versionString);
            throw new IllegalArgumentException(errorMessage, e);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(modelClassName).append(";").append(versionString);
        return builder.toString();
    }

    @Override
    public int hashCode() {
        Object[] obj = new Object[]{ modelClassName, versionString };
        return Objects.hashCode(obj);
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
        if (!(other.getVersionString().equals(this.getVersionString()))) {
            return false;
        }
        return true;
    }
}
