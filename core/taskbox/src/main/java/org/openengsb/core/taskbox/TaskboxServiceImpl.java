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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.persistence.PersistenceManager;
import org.openengsb.core.common.persistence.PersistenceService;
import org.openengsb.core.common.taskbox.TaskboxException;
import org.openengsb.core.common.taskbox.TaskboxService;
import org.openengsb.core.common.taskbox.model.Task;
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

    private String message;

    public void init() {
        persistence = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());
    }

    @Override
    public String getWorkflowMessage() throws TaskboxException {
        if (message == null) {
            throw new TaskboxException();
        }

        return message;
    }

    @Override
    public void setWorkflowMessage(String message) {
        this.message = message;
    }

    @Override
    public void startWorkflow(String workflowName, String taskVariableName, Task task) throws TaskboxException {
        try {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            parameterMap.put(taskVariableName, task);

            workflowService.startFlow(workflowName, parameterMap);

            log.trace("Started workflow " + workflowName);
        } catch (Exception e) {
            log.error(e.getMessage() + " STACKTRACE: " + e.getStackTrace());
            throw new TaskboxException(e);
        }

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
    public void processEvent(Event event) throws WorkflowException {
        workflowService.processEvent(event);
    }

    @Override
    public List<Task> getOpenTasks() {
        Task example= Task.returnNullTask();
        example.setDoneFlag(false);
        return getTasksForExample(example);
    }

    @Override
    public List<Task> getTasksForExample(Task example) {
        return persistence.query(example);
    }
}
