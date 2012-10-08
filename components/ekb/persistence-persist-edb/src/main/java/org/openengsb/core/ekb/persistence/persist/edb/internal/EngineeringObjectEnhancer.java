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
        Map<Object, OpenEngSBModel> updated = new HashMap<Object, OpenEngSBModel>();
        commit.getUpdates().addAll(recursiveUpdateEnhancement(commit.getUpdates(), updated, commit));
    }

    /**
     * Recursive function for calculating all models which need to be updated due to the original updates of the
     * EKBCommit.
     */
    private List<OpenEngSBModel> recursiveUpdateEnhancement(List<OpenEngSBModel> updates,
            Map<Object, OpenEngSBModel> updated, EKBCommit commit) {
        List<OpenEngSBModel> additionalUpdates = enhanceUpdates(updates, updated, commit);
        if (!additionalUpdates.isEmpty()) {
            for (OpenEngSBModel model : additionalUpdates) {
                updated.put(getCompleteModelOID(model, commit), model);
            }
            additionalUpdates.addAll(recursiveUpdateEnhancement(additionalUpdates, updated, commit));
        }
        return additionalUpdates;
    }

    /**
     * Enhances the given list of updates and returns a list of models which need to be additionally updated.
     */
    private List<OpenEngSBModel> enhanceUpdates(List<OpenEngSBModel> updates,
            Map<Object, OpenEngSBModel> updated, EKBCommit commit) {
        List<OpenEngSBModel> additionalUpdates = new ArrayList<OpenEngSBModel>();
        for (OpenEngSBModel model : updates) {
            if (updated.containsKey(getCompleteModelOID(model, commit))) {
                continue; // this model was already updated in this commit
            }
            if (ModelUtils.isEngineeringObject(model)) {
                additionalUpdates.addAll(performEOModelUpdate(model, commit));
            }
            additionalUpdates.addAll(getReferenceBasedAdditionalUpdates(model, updated, commit));
        }
        return additionalUpdates;
    }

    /**
     * Runs the logic of updating an Engineering Object model. Returns a list of models which need to be updated
     * additionally.
     */
    private List<OpenEngSBModel> performEOModelUpdate(OpenEngSBModel model, EKBCommit commit) {
        EDBObject queryResult = edbService.getObject(getCompleteModelOID(model, commit));
        OpenEngSBModel old = edbConverter.convertEDBObjectToModel(model.getClass(), queryResult);
        ModelDiff diff = ModelDiff.createModelDiff(old, model);
        boolean referencesChanged = diff.isForeignKeyChanged();
        boolean valuesChanged = diff.isValueChanged();
        if (referencesChanged && valuesChanged) {
            throw new EKBException("Engineering Objects may be updated only at "
                    + "references or at values not both in the same commit");
        }
        if (referencesChanged) {
            reloadReferencesAndUpdateEO(diff, model);
        } else {
            return updateReferencedModelsByEO(model);
        }
        return new ArrayList<OpenEngSBModel>();
    }

    /**
     * Updates all models which are referenced by the given engineering object.
     */
    private List<OpenEngSBModel> updateReferencedModelsByEO(OpenEngSBModel model) {
        List<OpenEngSBModel> updates = new ArrayList<OpenEngSBModel>();
        for (Field field : getForeignKeyFields(model.getClass())) {
            OpenEngSBModel result = performMerge(field, model, false);
            if (result != null) {
                updates.add(result);
            }
        }
        return updates;
    }

    /**
     * Reload the references which have changed in the actual update and update the Engineering Object accordingly.
     */
    private void reloadReferencesAndUpdateEO(ModelDiff diff, OpenEngSBModel model) {
        for (ModelDiffEntry entry : diff.getDifferences().values()) {
            mergeEngineeringObjectWithReferencedModel(entry.getField(), model);
        }
    }

    /**
     * Returns engineering objects to the commit, which are changed by a model which was committed in the EKBCommit
     */
    private List<OpenEngSBModel> getReferenceBasedAdditionalUpdates(OpenEngSBModel model,
            Map<Object, OpenEngSBModel> updated, EKBCommit commit) throws EKBException {
        List<OpenEngSBModel> updates = new ArrayList<OpenEngSBModel>();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(EDBConverterUtils.REFERENCE_PREFIX + "%", getCompleteModelOID(model, commit));
        List<EDBObject> references = edbService.query(params, System.currentTimeMillis());
        for (EDBObject reference : references) {
            OpenEngSBModel ref = updateEOByUpdatedModel(reference, model, updated);
            if (!updated.containsKey(getCompleteModelOID(ref, commit))) {
                updates.add(ref);
            }
            updated.put(getCompleteModelOID(ref, commit), ref);
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
            mergeEngineeringObjectWithReferencedModel(field, model);
        }
    }

    /**
     * Performs a merge based on the third parameter. If the third parameter is true, then the given model is the merge
     * target. If the third parameter is false, then the through the given field referenced model is the merge target.
     */
    private OpenEngSBModel performMerge(Field field, OpenEngSBModel model, boolean modelIsTarget) {
        try {
            ModelDescription description = getModelDescriptionFromField(field);
            String modelKey = (String) FieldUtils.readField(field, model, true);
            if (modelKey == null) {
                return null;
            }
            Class<?> sourceClass = modelRegistry.loadModel(description);
            Object instance = edbConverter.convertEDBObjectToModel(sourceClass, edbService.getObject(modelKey));
            ModelDescription target = ModelUtils.getModelDescription(model);
            if (modelIsTarget) {
                return (OpenEngSBModel) transformationEngine.performTransformation(description, target,
                    instance, model);
            } else {
                return (OpenEngSBModel) transformationEngine.performTransformation(target, description,
                    model, instance);
            }
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
     * Generates the model description of a field which is annotated with the OpenEngSBForeignKey annotation.
     */
    private ModelDescription getModelDescriptionFromField(Field field) {
        OpenEngSBForeignKey key = field.getAnnotation(OpenEngSBForeignKey.class);
        ModelDescription description = new ModelDescription(key.modelType(), key.modelVersion());
        return description;
    }

    /**
     * Merges the given EngineeringObject with the referenced model which is defined in the given field.
     */
    private void mergeEngineeringObjectWithReferencedModel(Field field, OpenEngSBModel model) {
        OpenEngSBModel result = performMerge(field, model, true);
        if (result != null) {
            model = result;
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

    /**
     * Calculates the complete model oid from the model and the commit object.
     */
    private String getCompleteModelOID(OpenEngSBModel model, EKBCommit commit) {
        return String.format("%s/%s/%s", commit.getDomainId(), commit.getConnectorId(),
            model.retrieveInternalModelId());
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
