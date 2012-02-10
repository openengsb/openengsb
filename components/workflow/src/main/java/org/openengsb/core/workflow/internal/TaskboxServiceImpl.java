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

package org.openengsb.core.workflow.internal;

import java.util.List;

import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.PersistenceManager;
import org.openengsb.core.api.persistence.PersistenceService;
import org.openengsb.core.api.workflow.TaskboxException;
import org.openengsb.core.api.workflow.TaskboxService;
import org.openengsb.core.api.workflow.WorkflowException;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.core.api.workflow.model.InternalWorkflowEvent;
import org.openengsb.core.api.workflow.model.Task;
import org.openengsb.core.common.util.ModelUtils;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskboxServiceImpl implements TaskboxService {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskboxServiceImpl.class);

    private WorkflowService workflowService;
    private PersistenceService persistence;
    private PersistenceManager persistenceManager;
    private BundleContext bundleContext;

    public void init() {
        persistence = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());
    }

    public void setWorkflowService(WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    @Override
    public List<Task> getOpenTasks() {
        return getTasksForExample(Task.createTaskWithAllValuesSetToNull());
    }

    @Override
    public List<Task> getTasksForExample(Task example) {
        return persistence.query(example);
    }

    @Override
    public Task getTaskForId(String id) throws TaskboxException {
        Task example = Task.createTaskWithAllValuesSetToNull();
        example.setTaskId(id);
        List<Task> list = getTasksForExample(example);
        if (list.size() != 1) {
            throw new TaskboxException((list.size() == 0 ? "No" : "More than one") + " task with ID " + id + " found!");
        }
        return list.get(0);
    }

    @Override
    public List<Task> getTasksForProcessId(String id) {
        Task example = Task.createTaskWithAllValuesSetToNull();
        example.setProcessId(id);
        return getTasksForExample(example);
    }

    @Override
    public synchronized void finishTask(Task task) throws WorkflowException {
        InternalWorkflowEvent finishedEvent = ModelUtils.createEmptyModelObject(InternalWorkflowEvent.class);
        finishedEvent.setName("TaskFinished");
        finishedEvent.setProcessBag(task);
        Task t = Task.createTaskWithAllValuesSetToNull();
        t.setTaskId(task.getTaskId());

        if (getTasksForExample(t).size() > 0) {
            try {
                persistence.delete(t);
            } catch (PersistenceException e) {
                throw new WorkflowException(e);
            }

            workflowService.processEvent(finishedEvent);
            LOGGER.info("finished task {}", task.getTaskId());
        } else {
            LOGGER.error("tried to finish task {}, BUT there is no such task.", task.getTaskId());
        }
    }

    @Override
    public void updateTask(Task task) throws WorkflowException {
        Task oldTask = getTaskForId(task.getTaskId());
        try {
            persistence.update(oldTask, task);
            LOGGER.info("updated task {}", task.getTaskId());
        } catch (PersistenceException e) {
            LOGGER.error("tried to update task {}, but it didnt work!", task.getTaskId());
            throw new WorkflowException(e);
        }
    }
}
