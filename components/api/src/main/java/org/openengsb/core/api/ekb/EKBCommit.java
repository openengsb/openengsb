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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openengsb.core.api.model.OpenEngSBModel;

/**
 * The EKBCommit class contains all necessary information to do a commit to the EDB through the EKB PersistInterface.
 */
public class EKBCommit {
    private List<OpenEngSBModel> inserts;
    private List<OpenEngSBModel> updates;
    private List<OpenEngSBModel> deletes;
    private String domainId;
    private String connectorId;
    private String instanceId;

    public EKBCommit() {
        inserts = new ArrayList<OpenEngSBModel>();
        updates = new ArrayList<OpenEngSBModel>();
        deletes = new ArrayList<OpenEngSBModel>();
    }

    /**
     * Adds a model to the list of models which shall be inserted into the EDB. If the given object is not a model, an
     * IllegalArgumentException is thrown.
     */
    public EKBCommit addInsert(Object insert) {
        if (insert != null) {
            checkIfModel(insert);
            inserts.add((OpenEngSBModel) insert);
        }
        return this;
    }

    /**
     * Adds a collection of models which shall be inserted into the EDB. If one of the given objects is not a model, an
     * IllegalArgumentException is thrown.
     */
    public EKBCommit addInserts(Collection<?> inserts) {
        if (inserts != null) {
            for (Object insert : inserts) {
                checkIfModel(insert);
                this.inserts.add((OpenEngSBModel) insert);
            }
        }
        return this;
    }

    /**
     * Adds a model to the list of models which shall be updated in the EDB. If the given object is not a model, an
     * IllegalArgumentException is thrown.
     */
    public EKBCommit addUpdate(Object update) {
        if (update != null) {
            checkIfModel(update);
            updates.add((OpenEngSBModel) update);
        }
        return this;
    }

    /**
     * Adds a collection of models which shall be updated in the EDB. If one of the given objects is not a model, an
     * IllegalArgumentException is thrown.
     */
    public EKBCommit addUpdates(Collection<?> updates) {
        if (updates != null) {
            for (Object update : updates) {
                checkIfModel(update);
                this.updates.add((OpenEngSBModel) update);
            }
        }
        return this;
    }

    /**
     * Adds an model to the list of models which shall be deleted from the EDB. If the given object is not a model, an
     * IllegalArgumentException is thrown.
     */
    public EKBCommit addDelete(Object delete) {
        if (delete != null) {
            checkIfModel(delete);
            deletes.add((OpenEngSBModel) delete);
        }
        return this;
    }

    /**
     * Adds a collection of models which shall be deleted from the EDB. If one of the given objects is not a model, an
     * IllegalArgumentException is thrown.
     */
    public EKBCommit addDeletes(Collection<?> deletes) {
        if (deletes != null) {
            for (Object delete : deletes) {
                checkIfModel(delete);
                this.deletes.add((OpenEngSBModel) delete);
            }
        }
        return this;
    }

    /**
     * Defines the id of the domain from where the commit comes from.
     */
    public EKBCommit setDomainId(String domainId) {
        this.domainId = domainId;
        return this;
    }

    /**
     * Defines the id of the connector from where the commit comes from.
     */
    public EKBCommit setConnectorId(String connectorId) {
        this.connectorId = connectorId;
        return this;
    }

    /**
     * Defines the id of the instance from where the commit comes from.
     */
    public EKBCommit setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    /**
     * Returns the list of OpenEngSBModels which shall be inserted into the EDB.
     */
    public List<OpenEngSBModel> getInserts() {
        return inserts;
    }

    /**
     * Returns the list of OpenEngSBModels which shall be updated in the EDB.
     */
    public List<OpenEngSBModel> getUpdates() {
        return updates;
    }

    /**
     * Returns the list of OpenEngSBModels which shall be deleted from the EDB.
     */
    public List<OpenEngSBModel> getDeletes() {
        return deletes;
    }

    /**
     * Returns the id of the domain from where the commit comes from.
     */
    public String getDomainId() {
        return domainId;
    }

    /**
     * Returns the id of the connector from where the commit comes from.
     */
    public String getConnectorId() {
        return connectorId;
    }

    /**
     * Returns the id of the instance from where the commit comes from.
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * Checks if an object is an OpenEngSBModel and throws an IllegalArgumentException if the object is no model.
     */
    private void checkIfModel(Object model) {
        if (!OpenEngSBModel.class.isAssignableFrom(model.getClass())) {
            throw new IllegalArgumentException("Only models can be committed");
        }
    }
}
