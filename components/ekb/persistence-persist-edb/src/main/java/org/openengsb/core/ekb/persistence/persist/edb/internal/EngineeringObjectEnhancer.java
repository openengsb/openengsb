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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.reflect.FieldUtils;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.annotation.OpenEngSBForeignKey;
import org.openengsb.core.edb.api.EDBConstants;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.EKBException;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.core.ekb.common.EDBConverter;
import org.openengsb.core.ekb.common.EDBConverterUtils;
import org.openengsb.core.util.ModelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The EngineeringObjectEnhancer enhance an EKBCommit object with additional models which need to be updated or enhance
 * inserted models based on the Engineering Object concept of the OpenEngSB.
 */
public class EngineeringObjectEnhancer {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineeringObjectEnhancer.class);
    private EngineeringDatabaseService edbService;
    private EDBConverter edbConverter;
    private TransformationEngine transformationEngine;
    private ModelRegistry modelRegistry;

    /**
     * Does the Engineering Object enhancement of the EKBCommit object. In this step there may be some inserts updated
     * or new objects will be added to the updates of the EKBCommit object. Throws an EKBException if an error in the
     * Engineering Object logic occurs.
     */
    public void enhanceEKBCommit(EKBCommit commit) throws EKBException {
        LOGGER.debug("Started to enhance the EKBCommit with Engineering Object information");
        enhanceCommitInserts(commit);
        enhanceCommitUpdates(commit);
        LOGGER.debug("Finished EKBCommit enhancing");
    }

    /**
     * Enhances the EKBCommit for the updates of EngineeringObjects.
     */
    private void enhanceCommitUpdates(EKBCommit commit) throws EKBException {
        List<OpenEngSBModel> additionalUpdates = new ArrayList<OpenEngSBModel>();
        Map<Object, OpenEngSBModel> updated = new HashMap<Object, OpenEngSBModel>();
        for (OpenEngSBModel model : commit.getUpdates()) {
            updated.put(model.retrieveInternalModelId(), model);
            if (ModelUtils.isEngineeringObject(model)) {
                // TODO: run EO object update logic
            }
            additionalUpdates.addAll(getReferenceBasedAdditionalUpdates(model, updated));
        }
        commit.getUpdates().addAll(additionalUpdates);
    }

    /**
     * Returns engineering objects to the commit, which are changed by a model which was committed in the EKBCommit
     */
    private List<OpenEngSBModel> getReferenceBasedAdditionalUpdates(OpenEngSBModel model,
            Map<Object, OpenEngSBModel> updated) throws EKBException {
        List<OpenEngSBModel> updates = new ArrayList<OpenEngSBModel>();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(EDBConverterUtils.REFERENCE_PREFIX + "%", model.retrieveInternalModelId());
        List<EDBObject> references = edbService.query(params, System.currentTimeMillis());
        for (EDBObject reference : references) {
            OpenEngSBModel ref = updateEOByUpdatedModel(reference, model, updated);
            updates.add(ref);
            updated.put(ref.retrieveInternalModelId(), ref);
        }
        return updates;
    }

    /**
     * Updates an Engineering Object given as EDBObject based on the update on the given model which is referenced by
     * the given Engineering Object.
     */
    private OpenEngSBModel updateEOByUpdatedModel(EDBObject reference, OpenEngSBModel model,
            Map<Object, OpenEngSBModel> updated) {
        ModelDescription source = ModelUtils.getModelDescription(model);
        ModelDescription description = getModelDescriptionFromEDBObject(reference);
        Object ref = updated.get(reference.getOID());
        if (ref == null) {
            ref = convertEDBObjectToModel(reference);
        }
        ref = transformationEngine.performTransformation(source, description, model, ref);
        return (OpenEngSBModel) ref;
    }

    /**
     * Converts an EDBObject into a model instance.
     */
    private OpenEngSBModel convertEDBObjectToModel(EDBObject object) throws EKBException {
        ModelDescription description = getModelDescriptionFromEDBObject(object);
        try {
            Class<?> modelClass = modelRegistry.loadModel(description);
            return (OpenEngSBModel) edbConverter.convertEDBObjectToModel(modelClass, object);
        } catch (ClassNotFoundException e) {
            throw new EKBException(String.format("Unable to load model of type %s", description), e);
        }
    }

    /**
     * Generates the model description of an EDBObject based on its values for the model type and model type version
     */
    private ModelDescription getModelDescriptionFromEDBObject(EDBObject object) {
        String modelType = object.getString(EDBConstants.MODEL_TYPE);
        String version = object.getString(EDBConstants.MODEL_TYPE_VERSION);
        return new ModelDescription(modelType, version);
    }

    /**
     * Enhances the EKBCommit for the insertion of EngineeringObjects.
     */
    private void enhanceCommitInserts(EKBCommit commit) throws EKBException {
        for (OpenEngSBModel model : commit.getInserts()) {
            if (ModelUtils.isEngineeringObject(model)) {
                performInsertEOLogic(model);
            }
        }
    }

    /**
     * Performs the logic for the enhancement needed to be performed to insert an Engineering Object into the EDB.
     */
    private void performInsertEOLogic(OpenEngSBModel model) {
        for (Field field : getForeignKeyFields(model.getClass())) {
            if (field.isAnnotationPresent(OpenEngSBForeignKey.class)) {
                performInsertEOFieldLogic(field, model);
            }
        }
    }

    /**
     * Performs the logic for updating the Engineering Object based on the OpenEngSBForeignKey annotation.
     */
    private void performInsertEOFieldLogic(Field field, OpenEngSBModel model) {
        try {
            OpenEngSBForeignKey key = field.getAnnotation(OpenEngSBForeignKey.class);
            ModelDescription description = new ModelDescription(key.modelType(), key.modelVersion());
            String modelKey = (String) FieldUtils.readField(field, model, true);
            Class<?> sourceClass = modelRegistry.loadModel(description);
            Object instance = edbConverter.convertEDBObjectToModel(sourceClass, edbService.getObject(modelKey));
            ModelDescription target = ModelUtils.getModelDescription(model);
            model = (OpenEngSBModel) transformationEngine.performTransformation(description, target, instance, model);
        } catch (SecurityException e) {
            throw new EKBException(generateErrorMessage(model), e);
        } catch (IllegalArgumentException e) {
            throw new EKBException(generateErrorMessage(model), e);
        } catch (IllegalAccessException e) {
            throw new EKBException(generateErrorMessage(model), e);
        } catch (ClassNotFoundException e) {
            throw new EKBException(generateErrorMessage(model), e);
        }
    }

    /**
     * Returns the list of fields of the given class which are annotated with the annotation OpenEngSBForeignKey
     */
    private List<Field> getForeignKeyFields(Class<?> modelClass) {
        List<Field> fields = new ArrayList<Field>();
        for (Field field : modelClass.getDeclaredFields()) {
            try {
                if (field.isAnnotationPresent(OpenEngSBForeignKey.class)) {
                    fields.add(field);
                }
            } catch (SecurityException e) {
                throw new EKBException(generateErrorMessage(modelClass), e);
            }
        }
        return fields;
    }

    /**
     * Generates an error message for the construction of EKBExceptions occurring during the enhancement
     */
    private String generateErrorMessage(Object model) {
        return generateErrorMessage(model.getClass());
    }

    /**
     * Generates an error message for the construction of EKBExceptions occurring during the enhancement
     */
    private String generateErrorMessage(Class<?> model) {
        return String.format("Unable to enhance the commit of the model %s with EngineeringObject information",
            model.getName());
    }

    public void setEdbService(EngineeringDatabaseService edbService) {
        this.edbService = edbService;
    }

    public void setEdbConverter(EDBConverter edbConverter) {
        this.edbConverter = edbConverter;
    }

    public void setTransformationEngine(TransformationEngine transformationEngine) {
        this.transformationEngine = transformationEngine;
    }

    public void setModelRegistry(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
    }
}
