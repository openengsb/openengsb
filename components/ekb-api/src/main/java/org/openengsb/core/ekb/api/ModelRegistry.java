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

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * The model registry service provides the possibility to register models, which than can be used by the EKB for
 * transformations.
 */
public interface ModelRegistry {

    /**
     * Registers a model in the model registry. As long as a model is registered, it can be used by the EKB for
     * transformations and model querying out of the EDB.
     */
    void registerModel(ModelDescription model);

    /**
     * Unregisters a model from the model registry.
     */
    void unregisterModel(ModelDescription model);

    /**
     * The model registry tries to load the model based on the given description. If the model is inactive or not
     * registered, a class not found exception is thrown.
     */
    Class<?> loadModel(ModelDescription model) throws ClassNotFoundException;

    /**
     * The model registry tries to identify all fields of the described model which have the given annotation. If the
     * model is inactive or not registered, a class not found exception is thrown.
     */
    List<String> getAnnotatedFields(ModelDescription model, Class<? extends Annotation> annotationClass)
        throws ClassNotFoundException;
}
