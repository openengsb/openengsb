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

package org.openengsb.core.ekb.persistence.persist.edb.internal;

import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.ekb.api.EKBException;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.ekb.common.EDBConverter;

/**
 * The EDBModelObject is a helper class which encapsulates functions for the EDBObject class for more sophisticated
 * features.
 */
public class EDBModelObject {
    private EDBObject object;
    private ModelRegistry modelRegistry;
    private EDBConverter edbConverter;

    public EDBModelObject(EDBObject object, ModelRegistry modelRegistry, EDBConverter edbConverter) {
        this.object = object;
        this.modelRegistry = modelRegistry;
        this.edbConverter = edbConverter;
    }

    /**
     * Returns the model description for the model class of the EDBModelObject
     */
    public ModelDescription getModelDescription() {
        String modelType = object.getString(EDBConstants.MODEL_TYPE);
        String version = object.getString(EDBConstants.MODEL_TYPE_VERSION);
        return new ModelDescription(modelType, version);
    }

    /**
     * Returns the corresponding model object for the EDBModelObject
     */
    public OpenEngSBModel getCorrespondingModel() throws EKBException {
        ModelDescription description = getModelDescription();
        try {
            Class<?> modelClass = modelRegistry.loadModel(description);
            return (OpenEngSBModel) edbConverter.convertEDBObjectToModel(modelClass, object);
        } catch (ClassNotFoundException e) {
            throw new EKBException(String.format("Unable to load model of type %s", description), e);
        }
    }

    /**
     * Returns the object id of the EDBModelObject
     */
    public String getOID() {
        return object.getOID();
    }
}
