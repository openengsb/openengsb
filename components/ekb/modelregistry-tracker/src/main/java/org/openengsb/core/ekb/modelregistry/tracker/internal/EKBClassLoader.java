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

package org.openengsb.core.ekb.modelregistry.tracker.internal;

import org.openengsb.core.api.model.ModelDescription;

/**
 * The class loader needed for EKB internal use. 
 */
public interface EKBClassLoader {
    
    /**
     * Tries to load the class with the given class name. It uses a delegation class loader, which first try to load the
     * the class with the EKB bundle classloader and if that fails, it searches the OSGi environment if another bundle
     * provide this class.
     */
    Class<?> loadClass(String classname) throws ClassNotFoundException;

    /**
     * Try to load a model based on the given model description. Throws a ClassNotFoundException if the model can't be
     * loaded.
     */
    Class<?> loadModel(ModelDescription model) throws ClassNotFoundException;
}
