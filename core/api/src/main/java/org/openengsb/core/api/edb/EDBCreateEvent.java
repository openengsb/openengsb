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

package org.openengsb.core.api.edb;

import org.openengsb.core.api.Event;
import org.openengsb.core.api.model.OpenEngSBModel;

/**
 * Represents a create Event. Try to insert an OpenEngSBModel object into the EDB. The saving name defines under which
 * name the model should be saved. Because this name should be unique, it is recommended to use a connector name + id
 * for the name.
 */
public class EDBCreateEvent extends Event {

    private OpenEngSBModel model;
    private String savingName;
    private String committer;
    private String role;

    public EDBCreateEvent(OpenEngSBModel model, String savingName, String committer, String role) {
        this.model = model;
        this.savingName = savingName;
        this.committer = committer;
        this.role = role;
    }

    public OpenEngSBModel getModel() {
        return model;
    }

    public String getSavingName() {
        return savingName;
    }
    
    public String getCommitter() {
        return committer;
    }
    
    public String getRole() {
        return role;
    }

}
