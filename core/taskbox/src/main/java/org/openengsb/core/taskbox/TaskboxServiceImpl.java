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

package org.openengsb.core.taskbox;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceManager;
import org.openengsb.core.common.persistence.PersistenceService;
import org.openengsb.core.common.taskbox.TaskboxService;
import org.openengsb.core.common.taskbox.model.Task;
import org.openengsb.core.common.taskbox.model.TaskFinishedEvent;
import org.openengsb.core.common.workflow.WorkflowException;
import org.openengsb.core.common.workflow.WorkflowService;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

public class TaskboxServiceImpl implements TaskboxService, BundleContextAware {
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

    @Override
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
    public void finishTask(Task task) throws WorkflowException {
        TaskFinishedEvent finishedEvent = new TaskFinishedEvent(task);
        try {
            persistence.delete(task);
        } catch (PersistenceException e) {
            throw new WorkflowException(e.getMessage());
        }

        workflowService.processEvent(finishedEvent);
        log.info("finished task " + task.getTaskId());
    }

    @Override
    public Task getTaskForId(String id) {
        Task example=Task.createTaskWithAllValuesSetToNull();
        example.setTaskId(id);
        return getTasksForExample(example).get(0);
    }
}
