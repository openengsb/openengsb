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

import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.model.OpenEngSBModel;

/**
 * The persist interface provides the functions to insert models into the EDB. This includes the conversion of models
 * and sanity checks of models.
 */
public interface PersistInterface {

    /**
     * Does a sanity check over the models and the status of the EDB when this models are inserted. After passed sanity
     * check, the models are persisted.
     */
    void commit(List<OpenEngSBModel> inserts, List<OpenEngSBModel> updates, List<OpenEngSBModel> deletes,
            ConnectorId id) throws SanityCheckException, EDBException;

    /**
     * Persist the models without performing sanity checks of them.
     */
    void forceCommit(List<OpenEngSBModel> inserts, List<OpenEngSBModel> updates, List<OpenEngSBModel> deletes,
            ConnectorId id) throws EDBException;

    /**
     * Only perform the sanity checks of the models.
     */
    SanityCheckReport check(List<OpenEngSBModel> inserts, List<OpenEngSBModel> updates, List<OpenEngSBModel> deletes,
            ConnectorId id) throws EDBException;
}
