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

package org.openengsb.core.api.workflow.model;

import java.util.Date;
import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * A Task is based on a {@link ProcessBag} and used for human interaction. It
 * contains all data needed for human interaction i.e. the properties relevant
 * to the user, a unique identifier and the tasks type used to categorize all
 * tasks of a certain kind.
 *
 * Each time a workflow needs user interaction, such task is created from the
 * workflows ProcessBag. It is also used to pass data back to the workflow after
 * user interaction. The old ProcessBag is then replaced with this new one.
 */
@SuppressWarnings("serial")
@XmlRootElement
public class Task extends ProcessBag {
    public Task() {
        super();
        init();
    }

    /**
     * Generates an empty task to be used for persistence queries (to get all
     * tasks).
     */
    public static Task createTaskWithAllValuesSetToNull() {
        Task emptyTask = new Task();
        emptyTask.removeAllProperties();
        emptyTask.setEmpty();
        return emptyTask;
    }

    public Task(ProcessBag bag) {
        super(bag);
        init();
    }

    private void init() {
        if (!containsProperty("taskId")) {
            addOrReplaceProperty("taskId", UUID.randomUUID().toString());
        }
        if (!containsProperty("taskCreationTimestamp")) {
            addOrReplaceProperty("taskCreationTimestamp", new Date());
        }
    }

    public String getTaskId() {
        return (String) getProperty("taskId");
    }

    public void setTaskId(String id) {
        addOrReplaceProperty("taskId", id);
    }

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

    public Date getTaskCreationTimestamp() {
        return (Date) getProperty("taskCreationTimestamp");
    }

    public void setTaskCreationTimestamp(Date taskCreationTimestamp) {
        addOrReplaceProperty("taskCreationTimestamp", taskCreationTimestamp);
    }
}
