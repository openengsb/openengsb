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

package org.openengsb.core.api.model;

import java.util.List;

/**
 * The OpenEngSBModelWrapper class is needed for the proper sending of OpenEngSBModels (with Jason). 
 */
public class OpenEngSBModelWrapper {
    private Class<?> modelClass;
    private List<OpenEngSBModelEntry> entries;
    
    public Class<?> getModelClass() {
        return modelClass;
    }
    public void setModelClass(Class<?> modelClass) {
        this.modelClass = modelClass;
    }
    public List<OpenEngSBModelEntry> getEntries() {
        return entries;
    }
    public void setEntries(List<OpenEngSBModelEntry> entries) {
        this.entries = entries;
    }    
}
