/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openengsb.core.workflow.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.event.process.DefaultProcessEventListener;
import org.drools.event.process.ProcessCompletedEvent;
import org.drools.event.process.ProcessNodeTriggeredEvent;
import org.drools.event.process.ProcessStartedEvent;
import org.drools.runtime.StatefulKnowledgeSession;

public class ProcessFinishedListener extends DefaultProcessEventListener {
    private Log log = LogFactory.getLog(ProcessFinishedListener.class);

    private StatefulKnowledgeSession session;

    public ProcessFinishedListener(StatefulKnowledgeSession session) {
        super();
        this.session = session;
    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        log.debug("process finished");
        session.halt();
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
        log.debug("process started: " + event.getProcessInstance().getId());
    }

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        log.debug("triggered Node: " + event.getNodeInstance().getNodeName());
    }

}
