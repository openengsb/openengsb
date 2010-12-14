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

package org.openengsb.core.common.workflow;

import java.util.Map;
import java.util.concurrent.Future;

import org.openengsb.core.common.Event;
import org.openengsb.core.common.workflow.model.InternalWorkflowEvent;

public interface WorkflowService {
    /**
     * processes the event in the knowledgebase by inserting it as a fact, and signaling it to every running process in
     * the current context
     *
     * @throws WorkflowException when there is a problem with obtaining the KnowledgeSession
     */
    void processEvent(Event event) throws WorkflowException;

    /**
     * processes the event in the knowledgebase by inserting it as a fact. the event only gets signaled to the process
     * specified in the InternalWorkflowEvent (see ProcessBag - ProcessId)
     *
     * @throws WorkflowException when there is a problem with obtaining the KnowledgeSession
     */
    void processEvent(InternalWorkflowEvent event) throws WorkflowException;

    /**
     * Starts a flow with the given id, in the current context's session and returns the process' instance ID as
     * returned by drools's KnowledgeSession. It's unique in the scope of the same context.
     *
     * This method may block execution until the workflow is forced to background by any kind of "waiting-node" (e.g.
     * Join-node waiting for an Event). You can listen to the "FlowStartedEvent" in your flow to keep this blocking as
     * short as possible.
     *
     * @throws WorkflowException when there is a problem with obtaining the KnowledgeSession or the flow could not be
     *         started
     */
    long startFlow(String processId) throws WorkflowException;

    /**
     * Starts a flow with the given id, in the current context's session. The Objects supplied in the ParameterMap are
     * added to the flow as variables.
     *
     * This method may block execution until the workflow is forced to background by any kind of "waiting-node" (e.g.
     * Join-node waiting for an Event). You can listen to the "FlowStartedEvent" in your flow to keep this blocking as
     * short as possible.
     *
     * @return the process' instance ID as returned by drools's KnowledgeSession. It's unique in the scope of the same
     *         context.
     * @throws WorkflowException when there is a problem with obtaining the KnowledgeSession or the flow could not be
     *         started
     * */
    long startFlow(String processId, Map<String, Object> parameterMap) throws WorkflowException;

    /**
     * Starts a flow with the given id, in the current context's session. The Objects supplied in the ParameterMap are
     * added to the flow as variables.
     *
     * This method will never block. It creates a new Thread that handles starting the flow. The returned future is done
     * as soon as the workflow is fully initialized (process-instance and id are available).
     *
     * @throws WorkflowException WorkflowException when there is a problem with obtaining the KnowledgeSession or the
     *         flow could not be started
     */
    Future<Long> startFlowInBackground(String processId) throws WorkflowException;

    /**
     * Starts a flow with the given id, in the current context's session and returns the process' instance ID as
     * returned by drools's KnowledgeSession. It's unique in the scope of the same context.
     *
     * This method will never block. It creates a new Thread that handles starting the flow. The returned future is done
     * as soon as the workflow is fully initialized (process-instance and id are available).
     *
     * @throws WorkflowException WorkflowException when there is a problem with obtaining the KnowledgeSession or the
     *         flow could not be started
     */
    Future<Long> startFlowInBackground(String processId, Map<String, Object> paramterMap) throws WorkflowException;

    /**
     * this method adds a rule to the rulebase that always starts workflow(s) when a certain event is raised
     *
     * @throws WorkflowException when there is a problem while adding the new rule
     */
    void registerFlowTriggerEvent(Event event, String... flowIds) throws WorkflowException;

}
