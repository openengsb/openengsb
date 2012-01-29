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

package org.openengsb.core.ekb.internal;

import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.api.edb.EDBBatchEvent;
import org.openengsb.core.api.edb.EDBDeleteEvent;
import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.edb.EDBInsertEvent;
import org.openengsb.core.api.edb.EDBObject;
import org.openengsb.core.api.edb.EDBUpdateEvent;
import org.openengsb.core.api.edb.EngineeringDatabaseService;
import org.openengsb.core.api.ekb.PersistInterface;
import org.openengsb.core.api.ekb.SanityCheckException;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the PersistInterface service. It's main responsibilities are the saving of models and the sanity
 * checks of these.
 */
public class PersistInterfaceService implements PersistInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistInterfaceService.class);

    private EngineeringDatabaseService edbService;
    private EDBConverter edbConverter;

    @Override
    public void commit(List<OpenEngSBModel> inserts, List<OpenEngSBModel> updates, List<OpenEngSBModel> deletes,
            ConnectorId id)
        throws SanityCheckException, EDBException {
        LOGGER.debug("Commit of models was called");
        runPersistingLogic(inserts, updates, deletes, true, true, id);
        LOGGER.debug("Commit of models was successful");
    }

    @Override
    public void forceCommit(List<OpenEngSBModel> inserts, List<OpenEngSBModel> updates, List<OpenEngSBModel> deletes,
            ConnectorId id)
        throws EDBException {
        LOGGER.debug("Force commit of models was called");
        runPersistingLogic(inserts, updates, deletes, false, true, id);
        LOGGER.debug("Force commit of models was successful");
    }

    @Override
    public void check(List<OpenEngSBModel> inserts, List<OpenEngSBModel> updates, List<OpenEngSBModel> deletes,
            ConnectorId id)
        throws SanityCheckException, EDBException {
        LOGGER.debug("Sanity checks of models was called");
        runPersistingLogic(inserts, updates, deletes, true, false, id);
        LOGGER.debug("Sanity checks of models passed successful");
    }

    /**
     * Runs the logic of the PersistInterface. Does the sanity checks if check is set to true and does the persisting of
     * models if persist is set to true.
     */
    private void runPersistingLogic(List<OpenEngSBModel> inserts, List<OpenEngSBModel> updates,
            List<OpenEngSBModel> deletes, boolean check, boolean persist, ConnectorId id) throws SanityCheckException,
        EDBException {
        if (check) {
            performSanityChecks(inserts, updates, deletes);
        }
        if (persist) {
            List<EDBObject> ins = edbConverter.convertModelsToEDBObjects(inserts, id);
            List<EDBObject> upd = edbConverter.convertModelsToEDBObjects(updates, id);
            List<EDBObject> del = edbConverter.convertModelsToEDBObjects(deletes, id);
            performPersisting(ins, upd, del);
        }
    }

    /**
     * Performs the sanity checks of the given models.
     */
    private void performSanityChecks(List<OpenEngSBModel> inserts, List<OpenEngSBModel> updates,
            List<OpenEngSBModel> deletes) throws SanityCheckException {
        // TODO: implement sanity check logic
    }

    /**
     * Performs the persisting of the models into the EDB.
     */
    private void performPersisting(List<EDBObject> inserts, List<EDBObject> updates, List<EDBObject> deletes)
        throws EDBException {
        edbService.commitEDBObjects(inserts, updates, deletes);
    }

    @Override
    public void processEDBInsertEvent(EDBInsertEvent event) throws EDBException {
        List<OpenEngSBModel> models = new ArrayList<OpenEngSBModel>();
        models.add(event.getModel());
        commit(models, null, null, new ConnectorId(event.getDomainId(), event.getConnectorId(), event.getInstanceId()));
    }

    @Override
    public void processEDBDeleteEvent(EDBDeleteEvent event) throws EDBException {
        List<OpenEngSBModel> models = new ArrayList<OpenEngSBModel>();
        models.add(event.getModel());
        commit(null, null, models, new ConnectorId(event.getDomainId(), event.getConnectorId(), event.getInstanceId()));
    }

    @Override
    public void processEDBUpdateEvent(EDBUpdateEvent event) throws EDBException {
        List<OpenEngSBModel> models = new ArrayList<OpenEngSBModel>();
        models.add(event.getModel());
        commit(null, models, null, new ConnectorId(event.getDomainId(), event.getConnectorId(), event.getInstanceId()));
    }

    @Override
    public void processEDBBatchEvent(EDBBatchEvent event) throws EDBException {
        commit(event.getInserts(), event.getUpdates(), event.getDeletions(),
            new ConnectorId(event.getDomainId(), event.getConnectorId(), event.getInstanceId()));
    }

    public void setEdbService(EngineeringDatabaseService edbService) {
        this.edbService = edbService;
    }

    public void setEdbConverter(EDBConverter edbConverter) {
        this.edbConverter = edbConverter;
    }
}
