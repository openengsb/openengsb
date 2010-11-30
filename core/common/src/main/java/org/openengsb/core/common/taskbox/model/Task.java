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

package org.openengsb.core.common.taskbox.model;

/**
 * A Task is handled by the TaskboxService and consists of 1 or more TaskSteps.
 * It represents a more or less long running process (e.g.: handling of a Ticket
 * in a SupportSystem)
 */
public class Task {

    protected String taskId;
    protected String taskType;

    /**
     * Every Task has/wraps a ProcessBag
     */
    protected ProcessBag processBag;

    public Task(String taskId) {
        this.taskId = taskId;
    }

    public Task(String taskId, ProcessBag processBag) {
        this.taskId = taskId;
        this.processBag = processBag;
    }

    public Task(String taskId, String taskType) {
        this.taskId = taskId;
        this.taskType = taskType;
    }

    public Task(String taskId, String taskType, ProcessBag processBag) {
        this.taskId = taskId;
        this.taskType = taskType;
        this.processBag = processBag;
    }

    /**
     * returns the Unique ID the Task can be identified with
     */
    public String getTaskId() {
        return this.taskId;
    }

    /**
     * sets the Unique ID the Task can be identified with
     */
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    /**
     * returns the Type of the Task. The Type is used to group similar Tasks
     * together
     */
    public String getTaskType() {
        return this.taskType;
    }

    /**
     * sets the Type of the Task
     */
    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    /**
     * sets the ProcessBag of the Task
     * 
     * @param processBag
     */
    public void setProcessBag(ProcessBag processBag) {
        this.processBag = processBag;
    }

    /**
     * returns the ProcessBag
     * 
     * @return returns the ProcessBag of this Task
     */
    public ProcessBag getProcessBag() {
        return processBag;
    }
}
