/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.report;

import java.util.Set;

import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.endpoints.SimpleEventEndpoint;
import org.openengsb.core.model.Event;
import org.openengsb.report.datastore.EventStorageType;
import org.openengsb.report.datastore.EventStore;
import org.openengsb.report.datastore.StorageKey;

/**
 * @org.apache.xbean.XBean element="eventEndpoint"
 *                         description="Report event endpoint"
 */
public class ReportEventEndpoint extends SimpleEventEndpoint {

    private EventStore eventStore;

    private EventStorageRegistry policy;

    @Override
    protected void handleEvent(Event e, ContextHelper contextHelper, MessageProperties msgProperties) {
        testAndStore(EventStorageType.contextId, msgProperties.getContextId(), e);
        testAndStore(EventStorageType.correlationId, msgProperties.getCorrelationId(), e);
        testAndStore(EventStorageType.workflowId, msgProperties.getWorkflowId(), e);
        testAndStore(EventStorageType.workflowInstanceId, msgProperties.getWorkflowInstanceId(), e);
    }

    private void testAndStore(EventStorageType idType, String id, Event event) {
        if (id == null) {
            return;
        }
        Set<StorageKey> storeEventsFor = policy.getStorageKeysFor(idType, id);
        for (StorageKey key : storeEventsFor) {
            eventStore.storeEvent(key, event);
        }
    }

    public void setEventStore(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public void setPolicy(EventStorageRegistry policy) {
        this.policy = policy;
    }

}
