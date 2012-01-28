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

import java.util.List;

import org.openengsb.core.api.edb.EDBBatchEvent;
import org.openengsb.core.api.edb.EDBDeleteEvent;
import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.edb.EDBInsertEvent;
import org.openengsb.core.api.edb.EDBUpdateEvent;
import org.openengsb.core.api.edb.EngineeringDatabaseService;
import org.openengsb.core.api.ekb.PersistInterface;
import org.openengsb.core.api.ekb.SanityCheckException;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the PersistInterface service. It's main responsibilities are the saving of models and the
 * sanity checks of these.
 */
public class PersistInterfaceService implements PersistInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistInterfaceService.class);

    private EngineeringDatabaseService edbService;
    private EDBConverter edbConverter;
    
    @Override
    public void commit(List<OpenEngSBModel> inserts, List<OpenEngSBModel> updates, List<OpenEngSBModel> deletes)
        throws SanityCheckException {
        // TODO Auto-generated method stub
    }

    @Override
    public void forceCommit(List<OpenEngSBModel> inserts, List<OpenEngSBModel> updates, List<OpenEngSBModel> deletes) {
        // TODO Auto-generated method stub
    }

    @Override
    public void check(List<OpenEngSBModel> inserts, List<OpenEngSBModel> updates, List<OpenEngSBModel> deletes)
        throws SanityCheckException {
        // TODO Auto-generated method stub
    }

    @Override
    public void processEDBInsertEvent(EDBInsertEvent event) throws EDBException {
        // TODO Auto-generated method stub
    }

    @Override
    public void processEDBDeleteEvent(EDBDeleteEvent event) throws EDBException {
        // TODO Auto-generated method stub
    }

    @Override
    public void processEDBUpdateEvent(EDBUpdateEvent event) throws EDBException {
        // TODO Auto-generated method stub
    }

    @Override
    public void processEDBBatchEvent(EDBBatchEvent event) throws EDBException {
        // TODO Auto-generated method stub
    }
    
    public void setEdbService(EngineeringDatabaseService edbService) {
        this.edbService = edbService;
    }
    
    public void setEdbConverter(EDBConverter edbConverter) {
        this.edbConverter = edbConverter;
    }
}
