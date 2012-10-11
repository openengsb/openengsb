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

import static org.openengsb.core.api.model.ModelDescription.getModelDescriptionFromModel;
import static org.openengsb.core.ekb.persistence.persist.edb.internal.ModelDiff.createModelDiff;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.edb.api.EDBObject;
import org.openengsb.core.edb.api.EngineeringDatabaseService;
import org.openengsb.core.ekb.api.EKBCommit;
import org.openengsb.core.ekb.api.EKBException;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.core.ekb.common.EDBConverter;
import org.openengsb.core.ekb.common.EngineeringObjectModelWrapper;
import org.openengsb.core.ekb.common.SimpleModelWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The EngineeringObjectEnhancer enhance an EKBCommit object with additional models which need to be updated or enhance
 * inserted models based on the Engineering Object concept of the OpenEngSB.
 */
// TODO: OPENENGSB-3356, until now the automatic update propagation is done without asking. There should be a
// possibility to alter this behavior, so that it is possible e.g. to have no automatic update propagation at
// all or that only Engineering Objects receive automatic updates.
public class EngineeringObjectEnhancer {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineeringObjectEnhancer.class);
    // TODO: OPENENGSB-3359, replace edbService and edbConverter with queryInterface
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
        // TODO: OPENENGSB-3357, consider also deletions in the enhancement
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
            if (new SimpleModelWrapper(model).isEngineeringObject()) {
                additionalUpdates.addAll(performEOModelUpdate(model, commit));
            }
            additionalUpdates.addAll(getReferenceBasedUpdates(model, updated, commit));
        }
        return additionalUpdates;
    }

    /**
     * Runs the logic of updating an Engineering Object model. Returns a list of models which need to be updated
     * additionally.
     */
    private List<OpenEngSBModel> performEOModelUpdate(OpenEngSBModel model, EKBCommit commit) {
        ModelDiff diff = createModelDiff(model, getCompleteModelOID(model, commit),
            edbService, edbConverter);
        boolean referencesChanged = diff.isForeignKeyChanged();
        boolean valuesChanged = diff.isValueChanged();
        // TODO: OPENENGSB-3358, Make it possible to change references and values at the same time. Should be
        // no too big deal since we already know at this point which values of the model have been changed and
        // what the old and the new value for the changed properties are
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
        EngineeringObjectModelWrapper eo = new EngineeringObjectModelWrapper(model);
        for (Field field : eo.getForeignKeyFields()) {
            OpenEngSBModel result = performMerge(model, loadReferencedModel(eo, field));
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
    private List<OpenEngSBModel> getReferenceBasedUpdates(OpenEngSBModel model,
            Map<Object, OpenEngSBModel> updated, EKBCommit commit) throws EKBException {
        List<OpenEngSBModel> updates = new ArrayList<OpenEngSBModel>();
        SimpleModelWrapper simple = new SimpleModelWrapper(model);
        List<EDBObject> references = simple.getModelsReferringToThisModel(commit, edbService);
        for (EDBObject reference : references) {
            EDBModelObject modelReference = new EDBModelObject(reference, modelRegistry, edbConverter);
            OpenEngSBModel ref = updateEOByUpdatedModel(modelReference, model, updated);
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
    private OpenEngSBModel updateEOByUpdatedModel(EDBModelObject reference, OpenEngSBModel model,
            Map<Object, OpenEngSBModel> updated) {
        ModelDescription source = getModelDescriptionFromModel(model);
        ModelDescription description = reference.getModelDescription();
        Object ref = updated.get(reference.getOID());
        if (ref == null) {
            ref = reference.getCorrespondingModel();
        }
        ref = transformationEngine.performTransformation(source, description, model, ref);
        return (OpenEngSBModel) ref;
    }

    /**
     * Enhances the EKBCommit for the insertion of EngineeringObjects.
     */
    private void enhanceCommitInserts(EKBCommit commit) throws EKBException {
        for (OpenEngSBModel model : commit.getInserts()) {
            SimpleModelWrapper simple = new SimpleModelWrapper(model);
            if (simple.isEngineeringObject()) {
                performInsertEOLogic(simple.toEngineeringObject());
            }
        }
    }

    /**
     * Performs the logic for the enhancement needed to be performed to insert an Engineering Object into the EDB.
     */
    private void performInsertEOLogic(EngineeringObjectModelWrapper model) {
        for (Field field : model.getForeignKeyFields()) {
            mergeEngineeringObjectWithReferencedModel(field, model.getModel());
        }
    }

    /**
     * Performs the merge from the source model to the target model and returns the result. Returns null if either the
     * source or the target is null.
     */
    private OpenEngSBModel performMerge(OpenEngSBModel source, OpenEngSBModel target) {
        if (source == null || target == null) {
            return null;
        }
        ModelDescription sourceDesc = getModelDescriptionFromModel(source);
        ModelDescription targetDesc = getModelDescriptionFromModel(target);
        return (OpenEngSBModel) transformationEngine.performTransformation(sourceDesc, targetDesc, source, target);
    }

    /**
     * Merges the given EngineeringObject with the referenced model which is defined in the given field.
     */
    private void mergeEngineeringObjectWithReferencedModel(Field field, OpenEngSBModel model) {
        EngineeringObjectModelWrapper eo = new EngineeringObjectModelWrapper(model);
        OpenEngSBModel result = performMerge(loadReferencedModel(eo, field), model);
        if (result != null) {
            model = result;
        }
    }
    
    private OpenEngSBModel loadReferencedModel(EngineeringObjectModelWrapper eo, Field field) {
        return eo.loadReferencedModel(field, modelRegistry, edbService, edbConverter);
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
