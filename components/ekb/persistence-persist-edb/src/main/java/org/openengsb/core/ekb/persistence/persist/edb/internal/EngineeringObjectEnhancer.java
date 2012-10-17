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
import org.openengsb.core.ekb.api.hooks.EKBPreCommitHook;
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
public class EngineeringObjectEnhancer implements EKBPreCommitHook {
    private static final Logger LOGGER = LoggerFactory.getLogger(EngineeringObjectEnhancer.class);
    // TODO: OPENENGSB-3359, replace edbService and edbConverter with queryInterface
    private EngineeringDatabaseService edbService;
    private EDBConverter edbConverter;
    private TransformationEngine transformationEngine;
    private ModelRegistry modelRegistry;

    public EngineeringObjectEnhancer(EngineeringDatabaseService edbService, EDBConverter edbConverter,
            TransformationEngine transformationEngine, ModelRegistry modelRegistry) {
        this.edbService = edbService;
        this.edbConverter = edbConverter;
        this.transformationEngine = transformationEngine;
        this.modelRegistry = modelRegistry;
    }

    @Override
    public void onPreCommit(EKBCommit commit) throws EKBException {
        enhanceEKBCommit(commit);
    }

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
        Map<Object, SimpleModelWrapper> updated = new HashMap<Object, SimpleModelWrapper>();
        List<SimpleModelWrapper> result = recursiveUpdateEnhancement(
            convertOpenEngSBModelList(commit.getUpdates()), updated, commit);
        commit.getUpdates().addAll(convertSimpleModelWrapperList(result));

    }

    /**
     * Converts a list of OpenEngSBModel objects to a list of SimpleModelWrapper objects
     */
    private List<SimpleModelWrapper> convertOpenEngSBModelList(List<OpenEngSBModel> models) {
        List<SimpleModelWrapper> wrappers = new ArrayList<SimpleModelWrapper>();
        for (OpenEngSBModel model : models) {
            wrappers.add(new SimpleModelWrapper(model));
        }
        return wrappers;
    }

    /**
     * Converts a list of SimpleModelWrapper objects to a list of OpenEngSBModel objects
     */
    private List<OpenEngSBModel> convertSimpleModelWrapperList(List<SimpleModelWrapper> wrappers) {
        List<OpenEngSBModel> models = new ArrayList<OpenEngSBModel>();
        for (SimpleModelWrapper wrapper : wrappers) {
            models.add(wrapper.getModel());
        }
        return models;
    }

    /**
     * Recursive function for calculating all models which need to be updated due to the original updates of the
     * EKBCommit.
     */
    private List<SimpleModelWrapper> recursiveUpdateEnhancement(List<SimpleModelWrapper> updates,
            Map<Object, SimpleModelWrapper> updated, EKBCommit commit) {
        List<SimpleModelWrapper> additionalUpdates = enhanceUpdates(updates, updated, commit);
        if (!additionalUpdates.isEmpty()) {
            for (SimpleModelWrapper model : additionalUpdates) {
                updated.put(model.getCompleteModelOID(commit), model);
            }
            additionalUpdates.addAll(recursiveUpdateEnhancement(additionalUpdates, updated, commit));
        }
        return additionalUpdates;
    }

    /**
     * Enhances the given list of updates and returns a list of models which need to be additionally updated.
     */
    private List<SimpleModelWrapper> enhanceUpdates(List<SimpleModelWrapper> updates,
            Map<Object, SimpleModelWrapper> updated, EKBCommit commit) {
        List<SimpleModelWrapper> additionalUpdates = new ArrayList<SimpleModelWrapper>();
        for (SimpleModelWrapper model : updates) {
            if (updated.containsKey(model.getCompleteModelOID(commit))) {
                continue; // this model was already updated in this commit
            }
            if (model.isEngineeringObject()) {
                additionalUpdates.addAll(performEOModelUpdate(model.toEngineeringObject(), commit));
            }
            additionalUpdates.addAll(getReferenceBasedUpdates(model, updated, commit));
        }
        return additionalUpdates;
    }

    /**
     * Runs the logic of updating an Engineering Object model. Returns a list of models which need to be updated
     * additionally.
     */
    private List<SimpleModelWrapper> performEOModelUpdate(EngineeringObjectModelWrapper model, EKBCommit commit) {
        ModelDiff diff = createModelDiff(model.getModel(), model.getCompleteModelOID(commit),
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
        return new ArrayList<SimpleModelWrapper>();
    }

    /**
     * Updates all models which are referenced by the given engineering object.
     */
    private List<SimpleModelWrapper> updateReferencedModelsByEO(EngineeringObjectModelWrapper model) {
        List<SimpleModelWrapper> updates = new ArrayList<SimpleModelWrapper>();
        for (Field field : model.getForeignKeyFields()) {
            SimpleModelWrapper result = performMerge(model, loadReferencedModel(model, field));
            if (result != null) {
                updates.add(result);
            }
        }
        return updates;
    }

    /**
     * Reload the references which have changed in the actual update and update the Engineering Object accordingly.
     */
    private void reloadReferencesAndUpdateEO(ModelDiff diff, EngineeringObjectModelWrapper model) {
        for (ModelDiffEntry entry : diff.getDifferences().values()) {
            mergeEngineeringObjectWithReferencedModel(entry.getField(), model);
        }
    }

    /**
     * Returns engineering objects to the commit, which are changed by a model which was committed in the EKBCommit
     */
    private List<SimpleModelWrapper> getReferenceBasedUpdates(SimpleModelWrapper model,
            Map<Object, SimpleModelWrapper> updated, EKBCommit commit) throws EKBException {
        List<SimpleModelWrapper> updates = new ArrayList<SimpleModelWrapper>();
        List<EDBObject> references = model.getModelsReferringToThisModel(commit, edbService);
        for (EDBObject reference : references) {
            EDBModelObject modelReference = new EDBModelObject(reference, modelRegistry, edbConverter);
            SimpleModelWrapper ref = updateEOByUpdatedModel(modelReference, model, updated);
            if (!updated.containsKey(ref.getCompleteModelOID(commit))) {
                updates.add(ref);
            }
            updated.put(ref.getCompleteModelOID(commit), ref);
        }
        return updates;
    }

    /**
     * Updates an Engineering Object given as EDBObject based on the update on the given model which is referenced by
     * the given Engineering Object.
     */
    private SimpleModelWrapper updateEOByUpdatedModel(EDBModelObject reference, SimpleModelWrapper model,
            Map<Object, SimpleModelWrapper> updated) {
        ModelDescription source = model.getModelDescription();
        ModelDescription description = reference.getModelDescription();
        SimpleModelWrapper wrapper = updated.get(reference.getOID());
        Object ref = null;
        if (wrapper == null) {
            ref = reference.getCorrespondingModel();
        } else {
            ref = wrapper.getModel();
        }
        return new SimpleModelWrapper(transformationEngine.performTransformation(source, description,
            model.getModel(), ref));
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
            mergeEngineeringObjectWithReferencedModel(field, model);
        }
    }

    /**
     * Performs the merge from the source model to the target model and returns the result. Returns null if either the
     * source or the target is null.
     */
    private SimpleModelWrapper performMerge(SimpleModelWrapper source, SimpleModelWrapper target) {
        if (source == null || target == null) {
            return null;
        }
        ModelDescription sourceDesc = source.getModelDescription();
        ModelDescription targetDesc = target.getModelDescription();
        return new SimpleModelWrapper(transformationEngine.performTransformation(sourceDesc, targetDesc,
            source.getModel(), target.getModel()));
    }

    /**
     * Merges the given EngineeringObject with the referenced model which is defined in the given field.
     */
    private void mergeEngineeringObjectWithReferencedModel(Field field, EngineeringObjectModelWrapper model) {
        SimpleModelWrapper result = performMerge(loadReferencedModel(model, field), model);
        if (result != null) {
            model = result.toEngineeringObject();
        }
    }

    private SimpleModelWrapper loadReferencedModel(EngineeringObjectModelWrapper eo, Field field) {
        return eo.loadReferencedModel(field, modelRegistry, edbService, edbConverter);
    }
}
