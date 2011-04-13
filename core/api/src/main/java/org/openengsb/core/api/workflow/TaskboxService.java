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

import java.util.List;

import org.openengsb.core.api.workflow.model.Task;

/**
 * The Taskbox is a service which can be used when human interaction is required, e.g. by help desk applications. This
 * service provides functionality to get all or a filtered set of currently open human tasks. It also contains
 * functionality to finish an open task.
 * 
 * A human task can be inserted into any workflow by using the sub-workflow "humantask". This workflow then
 * automatically creates a task which then can be obtained by this service.
 * 
 * There is a {@link org.openengsb.ui.common.wicket.taskbox.WebTaskboxService WebTaskboxService} which adds UI
 * capabilities.
 * 
 * IMPORTANT: Every workflow which should support human interaction needs to wait for the FlowStartedEvent before the
 * first humantask is inserted!
 */
public interface TaskboxService {
    /**
     * Gets all tasks waiting for human interaction.
     */
    List<Task> getOpenTasks();

    /**
     * Gets all open tasks which match the example task.
     */
    List<Task> getTasksForExample(Task example);

    /**
     * Gets the open task for the passed ID.
     * 
     * @throws TaskboxException if none or more than one task was found
     */
    Task getTaskForId(String id) throws TaskboxException;

    /**
     * Gets all open tasks belonging to a workflow instance via its ID.
     */
    List<Task> getTasksForProcessId(String id);

    /**
     * Finishes the passed human interaction task. The task is removed and the workflow to which this task belongs to is
     * signaled that it can go on.
     * 
     * @throws WorkflowException if the workflow could not get the event (maybe due to an invalid processId in the tasks
     *         processBag) or the task could not be removed.
     */
    void finishTask(Task task) throws WorkflowException;

    /**
     * Updates a task by overriding the current task in the persistence with the parameter task. The old task gets
     * identified through the task id.
     * 
     * @throws WorkflowException if the task cant get replaced, e.g. if the old task doesn't exist.
     */
    void updateTask(Task task) throws WorkflowException;

}
