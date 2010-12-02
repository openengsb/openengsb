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

import java.util.Date;
import java.util.Calendar;
import java.util.UUID;

/**
 * A Task is handled by the TaskboxService and represents a human action to be
 * done and proper information / properties
 */
public class Task extends ProcessBag {

    protected String taskId;
    protected String taskType;
    protected String name;
    protected String description;
    protected Boolean doneFlag;
    protected Date taskCreationTimestamp;

    public Task() {
        super();
        this.taskId = UUID.randomUUID().toString();
        doneFlag = new Boolean(false);
        taskCreationTimestamp = Calendar.getInstance().getTime();
    }

    public Task(String processId, String context, String user) {
        super(processId, context, user);
        this.taskId = UUID.randomUUID().toString();
        doneFlag = new Boolean(false);
        taskCreationTimestamp = Calendar.getInstance().getTime();
    }

    public Task(String taskType) {
        super();
        this.taskId = UUID.randomUUID().toString();
        this.taskType = taskType;
        doneFlag = new Boolean(false);
        taskCreationTimestamp = Calendar.getInstance().getTime();
    }

    public Task(String taskType, String processId, String context, String user) {
        super(processId, context, user);
        this.taskId = UUID.randomUUID().toString();
        this.taskType = taskType;
        doneFlag = new Boolean(false);
        taskCreationTimestamp = Calendar.getInstance().getTime();
    }

    public void setNull() {
        super.setNull();
        this.taskId = null;
        this.taskType = null;
        this.name = null;
        this.description = null;
        this.doneFlag = null;
        this.taskCreationTimestamp = null;
    }

    public static Task returnNullTask() {
        Task t = new Task();
        t.setNull();
        return t;
    }

    /**
     * returns the Unique ID the Task can be identified with
     */
    public String getTaskId() {
        return this.taskId;
    }

    /**
     * generates and returns the Unique ID the Task can be identified with
     */
    public String generateTaskId() {
        this.taskId = UUID.randomUUID().toString();
        return taskId;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getDoneFlag() {
        return doneFlag;
    }

    public void setDoneFlag(Boolean doneFlag) {
        this.doneFlag = doneFlag;
    }

    public boolean isTaskFinished() {
        if (doneFlag == null)
            return false;
        return doneFlag.booleanValue();
    }

    public Date getTaskCreationTimestamp() {
        return taskCreationTimestamp;
    }
}
