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

package org.openengsb.core.workflow;

import org.openengsb.core.common.Event;

public interface WorkflowService {
    /**
     * processes the event in the knowledgebase by inserting it as a fact, and signaling it to every running process in
     * the current context
     *
     * @throws WorkflowException when there is a problem with obtaining the KnowledgeSession
     */
    void processEvent(Event event) throws WorkflowException;

    /**
     * Starts a flow with the given id, in the current context's session.
     *
     * @return the process' instance ID as returned by drools's KnowledgeSession. It's unique in the scope of the same
     *         context.
     * @throws WorkflowException when there is a problem with obtaining the KnowledgeSession or the flow could not be
     *         started
     */
    long startFlow(String processId) throws WorkflowException;
}
