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

package org.openengsb.core.api.workflow;

import java.util.Map;
import java.util.concurrent.Future;

import org.openengsb.core.api.Event;
import org.openengsb.core.api.OpenEngSBService;
import org.openengsb.core.api.workflow.model.ProcessBag;

public interface WorkflowService extends OpenEngSBService {
    /**
     * processes the event in the Knowledgebase by inserting it as a fact, and signaling it the processes it may
     * concern. The processes the Event is signaled to are determined by looking at the processId-field of the event. If
     * an "InternalWorkflowEvent" is processed, the proccessBag is checked in addition. The Event is then signaled to
     * these processes and their direct subProcesses. If the Event does not contain any information about which process
     * it belongs to, it is signaled to all running processes.
     * 
     * @throws WorkflowException when there is a problem with obtaining the KnowledgeSession
     */
    void processEvent(Event event) throws WorkflowException;

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
     * wait for the process with the given processInstance-id to finish.
     * 
     * @throws InterruptedException if the waiting is interrupted
     * @throws WorkflowException if the session could not be obtained
     */
    void waitForFlowToFinish(long id) throws InterruptedException, WorkflowException;

    /**
     * wait for the process with the given processInstance-id to finish, but only for a limited time. The timeout is
     * specified in milliseconds.
     * 
     * @throws InterruptedException if the waiting is interrupted
     * @throws WorkflowException if the session could not be obtained
     */
    boolean waitForFlowToFinish(long id, long timeout) throws InterruptedException, WorkflowException;

    /**
     * this method adds a rule to the rulebase that always starts workflow(s) when a certain event is raised. All fields
     * or the event that is handed in are checked excluding those, which are set {@code null}.
     * 
     * @throws WorkflowException when there is a problem while adding the new rule
     */
    void registerFlowTriggerEvent(Event event, String... flowIds) throws WorkflowException;

    /**
     * executes a workflow with the given name, and adds the processbag to its parameters. The processBag may be altered
     * by the workflow during execution. The modified processBag is returned as soon as the workflow-execution is
     * finished.
     * 
     * @throws WorkflowException when an error occurs during workflow-execution
     */
    ProcessBag executeWorkflow(String processId, ProcessBag parameters) throws WorkflowException;

    /**
     * cancels a currently ongoing workflow. Use this with caution. There is now error-handling or rollback of any kind.
     * 
     * @throws WorkflowException
     */
    void cancelFlow(Long processInstanceId) throws WorkflowException;

    /**
     * returns the processbag used by the given instance.
     * 
     * @throws IllegalArgumentException if no instance with the given ID is running
     */
    ProcessBag getProcessBagForInstance(long instanceId);

    /**
     * Attaches a {@link WorkflowListener} to the engine that listens to every future workflow run.
     */
    void registerWorkflowListener(WorkflowListener listener);

}
