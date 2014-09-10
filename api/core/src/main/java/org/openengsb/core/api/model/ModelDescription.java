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

import java.io.Serializable;
import java.util.Objects;

/**
 * The model description class defines the unique description of a model by the model class name and the version of the
 * model class. The version is equals to the version of the bundle containing the model class. If no version is given,
 * 1.0.0 will be used. All versions have the form x.y.z where x,y and z are integers.
 */
public class ModelDescription implements Serializable {
    private static final long serialVersionUID = 1L;

    private String modelClassName;
    private String versionString;

    public ModelDescription() {
    }

    public ModelDescription(String modelClassName, String versionString) {
        this.modelClassName = modelClassName;
        this.versionString = versionString;
    }

    public ModelDescription(Class<?> modelClass, String versionString) {
        this(modelClass.getName(), versionString);
    }

    public ModelDescription(String modelClassName) {
        this(modelClassName, "1.0.0");
    }

    public ModelDescription(Class<?> modelClass) {
        this(modelClass.getName(), "1.0.0");
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

    public void setVersionString(String versionString) {
        this.versionString = versionString;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(modelClassName).append(";").append(versionString);
        return builder.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(modelClassName, versionString);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ModelDescription) {
            ModelDescription other = (ModelDescription) obj;
            return Objects.equals(modelClassName, other.modelClassName)
                    && Objects.equals(versionString, other.versionString);
        }
        return false;
    }
}
