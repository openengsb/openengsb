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

package org.openengsb.core.common;

import org.openengsb.core.api.Connector;
import org.openengsb.core.api.DomainEvents;
import org.openengsb.core.api.DomainMethodExecutionException;
import org.openengsb.core.api.edb.EDBBatchEvent;
import org.openengsb.core.api.edb.EDBDeleteEvent;
import org.openengsb.core.api.edb.EDBEvent;
import org.openengsb.core.api.edb.EDBEventType;
import org.openengsb.core.api.edb.EDBException;
import org.openengsb.core.api.edb.EDBInsertEvent;
import org.openengsb.core.api.edb.EDBUpdateEvent;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.common.util.ModelUtils;

/**
 * Base class for implementations of connector services. It also contains the method for sending EDB events to the EDB.
 */
public abstract class AbstractOpenEngSBConnectorService extends AbstractOpenEngSBService implements Connector {

    protected String domainId;
    protected String connectorId;
    protected EDBBatchEvent batchEvent;

    public AbstractOpenEngSBConnectorService() {
        super();
    }

    public AbstractOpenEngSBConnectorService(String instanceId) {
        super(instanceId);
    }

    @Deprecated
    /**
     * DEPRECATED: use EKB PersistInterface instead!
     */
    public void sendEDBEvent(EDBEventType type, OpenEngSBModel model, DomainEvents events)
        throws EDBException {
        switch (type) {
            case INSERT:
                EDBInsertEvent create = ModelUtils.createEmptyModelObject(EDBInsertEvent.class);
                create.setModel(model);
                enrichEDBEvent(create);
                events.raiseEvent(create);
                break;
            case DELETE:
                EDBDeleteEvent delete = ModelUtils.createEmptyModelObject(EDBDeleteEvent.class);
                delete.setModel(model);
                enrichEDBEvent(delete);
                events.raiseEvent(delete);
                break;
            case UPDATE:
                EDBUpdateEvent update = ModelUtils.createEmptyModelObject(EDBUpdateEvent.class);
                update.setModel(model);
                enrichEDBEvent(update);
                events.raiseEvent(update);
                break;
            default:
                throw new DomainMethodExecutionException("unsupported type of event --> " + type);
        }
    }

    @Deprecated
    /**
     * DEPRECATED: use EKB PersistInterface instead!
     */
    public void initiateEDBBatch() {
        batchEvent = ModelUtils.createEmptyModelObject(EDBBatchEvent.class);
        enrichEDBEvent(batchEvent);
    }

    @Deprecated
    /**
     * DEPRECATED: use EKB PersistInterface instead!
     */
    public void addInsertModelToBatch(OpenEngSBModel model) {
        checkEDBBatchEvent();
        batchEvent.getInserts().add(model);
    }

    @Deprecated
    /**
     * DEPRECATED: use EKB PersistInterface instead!
     */
    public void addDeleteModelToBatch(OpenEngSBModel model) {
        checkEDBBatchEvent();
        batchEvent.getDeletes().add(model);
    }

    @Deprecated
    /**
     * DEPRECATED: use EKB PersistInterface instead!
     */
    public void addUpdateModelToBatch(OpenEngSBModel model) {
        checkEDBBatchEvent();
        batchEvent.getUpdates().add(model);
    }

    @Deprecated
    /**
     * DEPRECATED: use EKB PersistInterface instead!
     */
    public void sendEDBBatchEvent(DomainEvents events) throws EDBException {
        checkEDBBatchEvent();
        events.raiseEvent(batchEvent);
        batchEvent = null;
    }

    @Deprecated
    /**
     * DEPRECATED: use EKB PersistInterface instead!
     */
    private void checkEDBBatchEvent() {
        if (batchEvent == null) {
            throw new EDBException("EDBBatchEvent wasn't initiated");
        }
    }

    private void enrichEDBEvent(EDBEvent event) {
        event.setDomainId(domainId);
        event.setConnectorId(connectorId);
        event.setInstanceId(instanceId);
    }

    @Override
    public void setDomainId(String domainId) {
        this.domainId = domainId;
    }

    @Override
    public String getDomainId() {
        return domainId;
    }

    @Override
    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    @Override
    public String getConnectorId() {
        return connectorId;
    }
}
