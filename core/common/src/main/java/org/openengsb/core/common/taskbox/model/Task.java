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
import java.util.UUID;

/**
 * A Task is handled by the TaskboxService and represents a human action to be
 * done and proper information / properties
 */
public class Task extends ProcessBag {
    public Task() {
        super();
        init();
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
        properties.put("taskId", UUID.randomUUID().toString());
        properties.put("finished", false);
        properties.put("taskCreationTimestamp", new Date());
    }

    /**
     * returns the unique ID the Task can be identified with
     */
    public String getTaskId() {
        return (String) properties.get("taskId");
    }

    /**
     * generates and returns the unique ID the Task can be identified with
     */
    public String generateTaskId() {
        properties.put("taskId", UUID.randomUUID().toString());
        return (String) properties.get("taskId");
    }

    /**
     * returns the Type of the Task. The Type is used to group similar Tasks
     * together
     */
    public String getTaskType() {
        return (String) properties.get("taskType");
    }

    public void setTaskType(String taskType) {
        properties.put("taskType", taskType);
    }

    public String getName() {
        return (String) properties.get("name");
    }

    public void setName(String name) {
        properties.put("name", name);
    }

    public String getDescription() {
        return (String) properties.get("description");
    }

    public void setDescription(String description) {
        properties.put("description", description);
    }

    public void setFinished(boolean finished) {
        properties.put("finished", finished);
    }

    public boolean isFinished() {
        if (!properties.containsKey("finished")) {
            return false;
        }
        return (Boolean) properties.get("finished");
    }

    public void finishTask() {
        properties.put("finished", true);
    }

    public Date getTaskCreationTimestamp() {
        return (Date) properties.get("taskCreationTimestamp");
    }
}
