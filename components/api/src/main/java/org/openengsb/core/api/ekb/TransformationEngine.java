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

package org.openengsb.core.api.ekb;

import java.util.List;

import org.openengsb.core.api.ekb.transformation.TransformationDescription;

/**
 * The transformation engine does the actual conversation work. It uses the transformation descriptions it got to
 * transform one model into another if possible.
 */
public interface TransformationEngine {

    /**
     * Saves a transformation description into the transformation engine memory. If a transformation description for the
     * same class pair already exists, it gets updated.
     */
    void saveDescription(TransformationDescription description);

    /**
     * Saves a collection of transformation descriptions into the transformation engine memory. If a transformation
     * description for the same class pair already exists, it gets updated.
     */
    void saveDescriptions(List<TransformationDescription> descriptions);

    /**
     * Deletes a transformation description from the transformation engine memory.
     */
    void deleteDescription(TransformationDescription description);

    /**
     * Transforms the source object of the source class type to the target class type. Throws an
     * IllegalArgumentException if no transformation descriptions for this transformation are available.
     */
    <T> T performTransformation(Class<?> sourceClass, Class<T> targetClass, Object source);

    /**
     * Returns true if there is a transformation possible from source to target class. Returns fals if not.
     */
    Boolean isTransformationPossible(Class<?> sourceClass, Class<?> targetClass);
}
