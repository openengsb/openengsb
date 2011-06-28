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

import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;

/**
 * The interface for the ekb service. Contains the function for creating a proxy for simulating simple
 * OpenEngSBModel interfaces.
 */
public interface EngineeringKnowledgeBaseService {
    
    /**
     * Creates a proxy for the model interface which simulates an implementation of the interface.
     */
    <T extends OpenEngSBModel> T createEmptyModelObject(Class<T> model, OpenEngSBModelEntry... entries);
 
    /**
     * calling this function should only be done in the MethodUtil class. There it is necessary because we only
     * have a class field and we have no idea if this class extends OpenEngSBModel or not. Throws an IllegalArgument
     * Exception if the model parameter isn't an interface which extends OpenEngSBModel. 
     */
    @Deprecated
    Object createModelObject(Class<?> model, OpenEngSBModelEntry... entries) throws IllegalArgumentException;
}
