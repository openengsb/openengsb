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

package org.openengsb.core.common.taskbox;

import java.util.List;

import org.openengsb.core.common.Event;
import org.openengsb.core.common.taskbox.model.Task;
import org.openengsb.core.common.workflow.WorkflowException;

/**
 * The Taskbox is a service which can be used when human interaction is required, e.g. by help desk applications. This
 * core part is responsible for storing tasks, throwing events and starting workflows. Therefore it provides methods
 * which can be called by workflows e.g. assigning a task to different user-roles (such as case worker or developer) or
 * setting a task status. Another job is to choose the right wicket panel from the UI project to display the right
 * information in a certain situation.
 * 
 * The component uses the persistence compontent to store tasks and the workflow component to take control of specific
 * workflows.
 */
public interface TaskboxService {
    /**
     * Gets the message set by a workflow Used for testing purposes up to now
     * 
     * @throws TaskboxException when the message is not set
     */
    String getWorkflowMessage() throws TaskboxException;

    /**
     * Starts a test workflow workflowName - the name of the workflow to be started taskVariableName - the name of the
     * variable containing the taskObject in the workflow task - the taskobject to be reasoned about
     * 
     * @throws TaskboxException when the test workflow could not be started
     */

    void startWorkflow(String workflowName, String taskVariableName, Task task) throws TaskboxException;

    /**
     * Used by a workflow to set a message Used for testing purposes up to now
     */
    void setWorkflowMessage(String message);

    /**
     * Redirect events to workflowService
     * 
     * @throws WorkflowException
     */
    void processEvent(Event event) throws WorkflowException;

    /**
     * Loads all open tasks out of the persistence service.
     * 
     * @return List of open tasks
     */
    List<Task> getOpenTasks();
    /**
     * Loads all tasks which match the example task. 
     * @param example
     * @return List of tasks fitting the example
     */
    List<Task> getTasksForExample(Task example);
}
