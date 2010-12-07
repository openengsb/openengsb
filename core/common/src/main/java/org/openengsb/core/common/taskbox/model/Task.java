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
import java.util.HashMap;
import java.util.UUID;

import org.openengsb.core.common.workflow.model.ProcessBag;

/**
 * A Task is based on a ProcessBag and used for human interaction. It contains all data needed by a human task i.e.
 * properties that need to be changed by a user. Each time a workflow needs user interaction, such task is created from
 * the workflows ProcessBag.
 */
public class Task extends ProcessBag {
    private static Task emptyTask;

    public Task() {
        super();
        init();
    }

    public static Task createNullTask() {
        if (emptyTask == null) {
            emptyTask = new Task();
            emptyTask.setProcessId(null);
            emptyTask.setContext(null);
            emptyTask.setUser(null);
            emptyTask.setProperties(null);
        }

        return emptyTask;
    }

    public Task(Task task) {
        super(task);
    }

    public Task(HashMap<String, Object> properties) {
        super(properties);
    }

    public Task(String processId, String context, String user) {
        super(processId, context, user);
        init();
    }

    public Task(String taskType) {
        this();
        setTaskType(taskType);
    }

    public Task(String taskType, String processId, String context, String user) {
        this(processId, context, user);
        setTaskType(taskType);
    }

    private void init() {
        addOrReplaceProperty("taskId", UUID.randomUUID().toString());
        addOrReplaceProperty("finished", false);
        addOrReplaceProperty("taskCreationTimestamp", new Date());
    }

    /**
     * returns the unique ID the Task can be identified with
     */
    public String getTaskId() {
        return (String) getProperty("taskId");
    }

    /**
     * generates and returns the unique ID the Task can be identified with
     */
    public String generateTaskId() {
        addOrReplaceProperty("taskId", UUID.randomUUID().toString());
        return (String) getProperty("taskId");
    }

    /**
     * returns the Type of the Task. The Type is used to group similar Tasks together
     */
    public String getTaskType() {
        return (String) getProperty("taskType");
    }

    public void setTaskType(String taskType) {
        addOrReplaceProperty("taskType", taskType);
    }

    public String getName() {
        return (String) getProperty("name");
    }

    public void setName(String name) {
        addOrReplaceProperty("name", name);
    }

    public String getDescription() {
        return (String) getProperty("description");
    }

    public void setDescription(String description) {
        addOrReplaceProperty("description", description);
    }

    public void setFinished(boolean finished) {
        addOrReplaceProperty("finished", finished);
    }

    public boolean isFinished() {
        if (!containsProperty("finished")) {
            return false;
        }
        return (Boolean) getProperty("finished");
    }

    public void finishTask() {
        addOrReplaceProperty("finished", true);
    }

    public Date getTaskCreationTimestamp() {
        return (Date) getProperty("taskCreationTimestamp");
    }
}
