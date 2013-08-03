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

import java.util.List;

import org.openengsb.core.api.context.ContextHolder;
import org.osgi.framework.Version;

/**
 * This class is a class which wraps a model object for easier model handling in the static code.
 */
public class ModelWrapper {
    protected OpenEngSBModel model;

    protected ModelWrapper(OpenEngSBModel model) {
        this.model = model;
    }

    /**
     * Creates a model wrapper object out of the given model object. Throws IllegalArgumentException in case the given
     * model object is no model.
     */
    public static ModelWrapper wrap(Object model) {
        if (!(isModel(model.getClass()))) {
            throw new IllegalArgumentException("The given object is no model");
        }
        return new ModelWrapper((OpenEngSBModel) model);
    }

    /**
     * Returns true if the given class is a model class, returns false if not.
     */
    public static boolean isModel(Class<?> clazz) {
        return OpenEngSBModel.class.isAssignableFrom(clazz);
    }

    /**
     * Returns the underlying model object.
     */
    public OpenEngSBModel getUnderlyingModel() {
        return model;
    }

    /**
     * Creates the model description object for the underlying model object.
     */
    public ModelDescription getModelDescription() {
        String modelName = retrieveModelName();
        String modelVersion = retrieveModelVersion();
        if (modelName == null || modelVersion == null) {
            throw new IllegalArgumentException("Unsufficient information to create model description.");
        }
        return new ModelDescription(modelName, modelVersion);
    }

    /**
     * Calls the corresponding method in the wrapped model.
     */
    public List<OpenEngSBModelEntry> toOpenEngSBModelValues() {
        return model.toOpenEngSBModelValues();
    }

    /**
     * Calls the corresponding method in the wrapped model.
     */
    public List<OpenEngSBModelEntry> toOpenEngSBModelEntries() {
        return model.toOpenEngSBModelEntries();
    }

    /**
     * Calls the corresponding method in the wrapped model.
     */
    public Object retrieveInternalModelId() {
        return model.retrieveInternalModelId();
    }

    /**
     * Calls the corresponding method in the wrapped model.
     */
    public Long retrieveInternalModelTimestamp() {
        return model.retrieveInternalModelTimestamp();
    }

    /**
     * Calls the corresponding method in the wrapped model.
     */
    public Integer retrieveInternalModelVersion() {
        return model.retrieveInternalModelVersion();
    }

    /**
     * Calls the corresponding method in the wrapped model.
     */
    public void addOpenEngSBModelEntry(OpenEngSBModelEntry entry) {
        model.addOpenEngSBModelEntry(entry);
    }

    /**
     * Calls the corresponding method in the wrapped model.
     */
    public void removeOpenEngSBModelEntry(String key) {
        model.removeOpenEngSBModelEntry(key);
    }

    /**
     * Calls the corresponding method in the wrapped model.
     */
    public List<OpenEngSBModelEntry> getOpenEngSBModelTail() {
        return model.getOpenEngSBModelTail();
    }

    /**
     * Calls the corresponding method in the wrapped model.
     */
    public void setOpenEngSBModelTail(List<OpenEngSBModelEntry> entries) {
        model.setOpenEngSBModelTail(entries);
    }

    /**
     * Calls the corresponding method in the wrapped model.
     */
    public String retrieveModelName() {
        return model.retrieveModelName();
    }

    /**
     * Calls the corresponding method in the wrapped model.
     */
    public String retrieveModelVersion() {
        return model.retrieveModelVersion();
    }

    /**
     * Creates a version object from the model version string in the wrapper model.
     */
    public Version retrieveModelVersionObject() {
        return new Version(retrieveModelVersion());
    }
    
    /**
     * Calculates the complete model oid from the model.
     */
    public String getCompleteModelOID() {
        return appendContextId(model.retrieveInternalModelId());
    }
    
    /**
     * Appends the currentContext to the given model id.
     */
    protected String appendContextId(Object modelId) {
        return String.format("%s/%s", ContextHolder.get().getCurrentContextId(), modelId);
    }
}
