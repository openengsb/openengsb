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

import java.io.Serializable;
import java.util.List;

/**
 * Represents a generic model which should be used by any model data in the domains. With this model it is possible to
 * use one model for all kinds of domain model data. Every domain model marked with the Model interface get this
 * interface injected.
 */
public interface OpenEngSBModel extends Serializable {

    /**
     * Returns a list of OpenEngSBModelEntries. The list contains all data fields which are used by the specific domain.
     */
    List<OpenEngSBModelEntry> toOpenEngSBModelEntries();

    /**
     * Returns the internal model id of the model. This id is defined through the OpenEngSBModelId annotation. Returns
     * null if no model id is defined.
     */
    Object retrieveInternalModelId();

    /**
     * Returns the internal model timestamp. This timestamp tells the user when this model has been last saved in the
     * EDB. If the model is not persisted, the returned value is null.
     */
    Long retrieveInternalModelTimestamp();

    /**
     * Returns the internal model version. This version tells the user which version this model in the EDB has. If the
     * model is not persisted, the returned value is null.
     */
    Integer retrieveInternalModelVersion();

    /**
     * Adds a OpenEngSBModelEntry to the model. Can be used to add information that is not in a domain model defined
     * ("tail").
     */
    void addOpenEngSBModelEntry(OpenEngSBModelEntry entry);

    /**
     * Removes a OpenEngSBModelEntry from the model. Should be used with caution. Can produce problems with complex
     * types or lists. Should only be used to maintain the "tail".
     */
    void removeOpenEngSBModelEntry(String key);

    /**
     * Returns the tail of the model, which are all elements added by addOpenEngSBModelEntry. Elements which are added
     * there are mainly elements added by the OpenEngSB internally or by the user directly.
     */
    List<OpenEngSBModelEntry> getOpenEngSBModelTail();

    /**
     * Sets the tail of the model. Caution: values added here have nothing to do with the normal properties of the
     * model.
     */
    void setOpenEngSBModelTail(List<OpenEngSBModelEntry> entries);
}
