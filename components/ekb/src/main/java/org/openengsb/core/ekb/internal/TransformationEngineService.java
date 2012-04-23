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

package org.openengsb.core.ekb.internal;

import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.api.ekb.ModelDescription;
import org.openengsb.core.api.ekb.TransformationEngine;
import org.openengsb.core.api.ekb.transformation.TransformationDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the transformation engine. Only supports the transformations from OpenEngSBModels to
 * OpenEngSBModels.
 */
public class TransformationEngineService implements TransformationEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformationEngineService.class);
    private List<TransformationDescription> descriptions;
    private EKBClassLoader classLoader;

    public TransformationEngineService() {
        descriptions = new ArrayList<TransformationDescription>();
    }

    @Override
    public void saveDescription(TransformationDescription description) {
        deleteDescription(description);
        descriptions.add(description);
        LOGGER.debug("Added transformation description to the TransformationEngine");
    }

    @Override
    public void saveDescriptions(List<TransformationDescription> descriptions) {
        for (TransformationDescription description : descriptions) {
            saveDescription(description);
        }
    }

    @Override
    public void deleteDescription(TransformationDescription description) {
        for (TransformationDescription desc : descriptions) {
            if (desc.equals(description)) {
                descriptions.remove(desc);
                return;
            }
        }
    }

    /**
     * Returns the first possible way to transform an object of the source model to an object of the target model.
     */
    private TransformationDescription getTransformationDescription(ModelDescription source, ModelDescription target) {
        TransformationDescription compare = new TransformationDescription(source, target);
        for (TransformationDescription td : descriptions) {
            if (td.equals(compare)) {
                return td;
            }
        }
        return null;
    }

    @Override
    public Object performTransformation(ModelDescription sourceModel, ModelDescription targetModel, Object source) {
        try {
            TransformationDescription desc = getTransformationDescription(sourceModel, targetModel);
            if (desc != null) {
                TransformationPerformer performer = new TransformationPerformer(classLoader);
                return performer.transformObject(desc, source);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("No transformation description for this model pair defined");
    }

    @Override
    public Boolean isTransformationPossible(ModelDescription sourceModel, ModelDescription targetModel) {
        return getTransformationDescription(sourceModel, targetModel) != null;
    }
    
    @Override
    public void deleteDescriptionsByFile(String fileName) {
        for (TransformationDescription description : getDescriptionsByFile(fileName)) {
            deleteDescription(description);
        }
    }

    @Override
    public List<TransformationDescription> getDescriptionsByFile(String fileName) {
        List<TransformationDescription> result = new ArrayList<TransformationDescription>();
        for (TransformationDescription description : descriptions) {
            if (fileName.equals(description.getFileName())) {
                result.add(description);
            }
        }
        return result;
    }

    public void setClassLoader(EKBClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
