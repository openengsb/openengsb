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

package org.openengsb.core.ekb.transformation.wonderland.internal;

import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.ekb.api.ModelGraph;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;
import org.openengsb.core.ekb.transformation.wonderland.internal.performer.TransformationPerformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the transformation engine. Only supports the transformations from OpenEngSBModels to
 * OpenEngSBModels.
 */
public class TransformationEngineService implements TransformationEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationEngineService.class);
    private ModelRegistry modelRegistry;
    private ModelGraph graphDb;

    @Override
    public void saveDescription(TransformationDescription description) {
        LOGGER.debug("Added transformation description {} to transformation engine service", description);
        deleteDescription(description);
        graphDb.addTransformation(description);
    }

    @Override
    public void saveDescriptions(List<TransformationDescription> descriptions) {
        for (TransformationDescription description : descriptions) {
            saveDescription(description);
        }
    }

    @Override
    public void deleteDescription(TransformationDescription description) {
        LOGGER.debug("Deleted transformation description {} from transformation engine service", description);
        graphDb.removeTransformation(description);
    }

    @Override
    public void deleteDescriptionsByFile(String fileName) {
        for (TransformationDescription description : getDescriptionsByFile(fileName)) {
            deleteDescription(description);
        }
    }

    @Override
    public List<TransformationDescription> getDescriptionsByFile(String fileName) {
        return graphDb.getTransformationsPerFileName(fileName);
    }

    @Override
    public Object performTransformation(ModelDescription sourceModel, ModelDescription targetModel, Object source) {
        return performTransformation(sourceModel, targetModel, source, new ArrayList<String>());
    }

    @Override
    public Object performTransformation(ModelDescription sourceModel, ModelDescription targetModel, Object source,
            List<String> ids) {
        try {
            List<TransformationDescription> result = graphDb.getTransformationPath(sourceModel, targetModel, ids);
            if (result != null && !result.isEmpty()) {
                for (TransformationDescription step : result) {
                    TransformationPerformer performer = new TransformationPerformer(modelRegistry);
                    source = performer.transformObject(step, source);
                }
            }
            return source;
        } catch (InstantiationException e) {
            LOGGER.error("Instantiation exception while trying to perform transformations", e);
        } catch (IllegalAccessException e) {
            LOGGER.error("Illegal accesss exception while trying to perform transformations", e);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Class not found exception while trying to perform transformations", e);
        }
        throw new IllegalArgumentException("No transformation description for the given parameters defined");
    }

    @Override
    public Boolean isTransformationPossible(ModelDescription sourceModel, ModelDescription targetModel) {
        return isTransformationPossible(sourceModel, targetModel, new ArrayList<String>());
    }

    @Override
    public Boolean isTransformationPossible(ModelDescription sourceModel, ModelDescription targetModel,
            List<String> ids) {
        return graphDb.isTransformationPossible(sourceModel, targetModel, ids);
    }

    public void setModelRegistry(ModelRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
    }

    public void setGraphDb(ModelGraph graphDb) {
        this.graphDb = graphDb;
    }

}
