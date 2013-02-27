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

package org.openengsb.core.ekb.api;

import java.util.List;

import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.ekb.api.transformation.TransformationDescription;

/**
 * The model graph is used as base for the finding of transformation descriptions and is able to perform graph based
 * algorithm on the transformation graph.
 */
public interface ModelGraph {

    /**
     * Adds a model to the graph database and defines it as an active model.
     */
    void addModel(ModelDescription model);

    /**
     * Removes a model from the graph database by defining it as an inactive model.
     */
    void removeModel(ModelDescription model);

    /**
     * Adds a transformation description to the graph database. It also adds the models which are the source and the
     * target of the description, if they are not yet added, as inactive models.
     */
    void addTransformation(TransformationDescription description);

    /**
     * Removes a transformation description from the graph database. If the given description has no defined id, then
     * all transformation descriptions in the given edge are deleted, which have ids which were automatically added.
     */
    void removeTransformation(TransformationDescription description);

    /**
     * Returns all transformation descriptions which were added by the file with the given filename.
     */
    List<TransformationDescription> getTransformationsPerFileName(String filename);

    /**
     * Returns a possible transformation path, beginning at the source model type and ending with the target model type
     * where all given transformation description ids appear in the path.
     */
    List<TransformationDescription> getTransformationPath(ModelDescription source, ModelDescription target,
            List<String> ids);

    /**
     * Returns true if there is a transformation path, beginning at the source model type and ending with the target
     * model type where all given transformation description ids appear in the path. Returns false if not.
     */
    Boolean isTransformationPossible(ModelDescription source, ModelDescription target, List<String> ids);

}
