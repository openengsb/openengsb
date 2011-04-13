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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.PersistenceManager;
import org.openengsb.core.api.persistence.PersistenceService;
import org.openengsb.core.api.workflow.TaskboxException;
import org.openengsb.core.api.workflow.TaskboxService;
import org.openengsb.core.api.workflow.WorkflowException;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.core.api.workflow.model.InternalWorkflowEvent;
import org.openengsb.core.api.workflow.model.Task;
import org.osgi.framework.BundleContext;

public class TaskboxServiceImpl implements TaskboxService {
    private Log log = LogFactory.getLog(getClass());

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
        InternalWorkflowEvent finishedEvent = new InternalWorkflowEvent("TaskFinished", task);
        Task t = Task.createTaskWithAllValuesSetToNull();
        t.setTaskId(task.getTaskId());

        if (getTasksForExample(t).size() > 0) {
            try {
                persistence.delete(t);
            } catch (PersistenceException e) {
                throw new WorkflowException(e);
            }

            workflowService.processEvent(finishedEvent);
            log.info("finished task " + task.getTaskId());
        } else {
            log.error("tried to finish task " + task.getTaskId() + " BUT there is no such task.");
        }
    }

    @Override
    public void updateTask(Task task) throws WorkflowException {
        Task oldTask = getTaskForId(task.getTaskId());
        try {
            persistence.update(oldTask, task);
            log.info("updated task " + task.getTaskId());
        } catch (PersistenceException e) {
            log.error("tried to update task " + task.getTaskId() + ", but it didnt work!");
            throw new WorkflowException(e);
        }
    }
}
