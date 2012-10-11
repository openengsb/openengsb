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

package org.openengsb.core.ekb.common;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.model.annotation.OpenEngSBForeignKey;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.util.ModelUtils;

/**
 * The SimpleModelWrapper class is a helper class which encapsulates functions for models which are not part of the
 * standard function set.
 */
@SuppressWarnings("serial")
public class SimpleModelWrapper implements OpenEngSBModel {
    protected OpenEngSBModel model;

    public SimpleModelWrapper(OpenEngSBModel model) {
        this.model = model;
    }

    public SimpleModelWrapper(Object model) {
        ModelUtils.checkIfObjectIsModel(model);
        this.model = (OpenEngSBModel) model;
    }

    /**
     * Returns a list of EDBObjects which are referring to this model.
     */
    public List<EDBObject> getModelsReferringToThisModel(EKBCommit commit, EngineeringDatabaseService edbService) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(EDBConverterUtils.REFERENCE_PREFIX + "%", getCompleteModelOID(model, commit));
        return edbService.query(params, System.currentTimeMillis());
    }

    /**
     * Calculates the complete model oid from the model and the commit object.
     */
    private String getCompleteModelOID(OpenEngSBModel model, EKBCommit commit) {
        return String.format("%s/%s/%s", commit.getDomainId(), commit.getConnectorId(),
            model.retrieveInternalModelId());
    }

    /**
     * Returns true if the model is an engineering object, returns false if not.
     */
    public Boolean isEngineeringObject() {
        return isEngineeringObjectClass(model.getClass());
    }
    
    /**
     * Returns true if the class is the class of an engineering object, returns false if not.
     */
    public static Boolean isEngineeringObjectClass(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(OpenEngSBForeignKey.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the corresponding engineering object model wrapper to the simple model this wrapper is wrapping.
     */
    public EngineeringObjectModelWrapper toEngineeringObject() throws IllegalArgumentException {
        if (!isEngineeringObject()) {
            throw new IllegalArgumentException("The model of the SimpleModelWrapper is no EngineeringObject");
        }
        return new EngineeringObjectModelWrapper(model);
    }

    /**
     * Returns the underlying OpenEngSBModel instance object
     */
    public OpenEngSBModel getModel() {
        return model;
    }

    @Override
    public void addOpenEngSBModelEntry(OpenEngSBModelEntry arg0) {
        model.addOpenEngSBModelEntry(arg0);
    }

    @Override
    public List<OpenEngSBModelEntry> getOpenEngSBModelTail() {
        return model.getOpenEngSBModelTail();
    }

    @Override
    public void removeOpenEngSBModelEntry(String arg0) {
        model.removeOpenEngSBModelEntry(arg0);
    }

    @Override
    public Object retrieveInternalModelId() {
        return model.retrieveInternalModelId();
    }

    @Override
    public Long retrieveInternalModelTimestamp() {
        return model.retrieveInternalModelTimestamp();
    }

    @Override
    public Integer retrieveInternalModelVersion() {
        return model.retrieveInternalModelVersion();
    }

    @Override
    public String retrieveModelName() {
        return model.retrieveModelName();
    }

    @Override
    public String retrieveModelVersion() {
        return model.retrieveModelVersion();
    }

    @Override
    public void setOpenEngSBModelTail(List<OpenEngSBModelEntry> arg0) {
        model.setOpenEngSBModelTail(arg0);
    }

    @Override
    public List<OpenEngSBModelEntry> toOpenEngSBModelEntries() {
        return model.toOpenEngSBModelEntries();
    }
}
