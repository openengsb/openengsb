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

package org.openengsb.ui.common.taskbox;

import java.lang.reflect.Constructor;
import java.util.List;

import org.apache.wicket.markup.html.panel.Panel;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.PersistenceManager;
import org.openengsb.core.api.persistence.PersistenceService;
import org.openengsb.core.api.workflow.TaskboxException;
import org.openengsb.core.api.workflow.TaskboxService;
import org.openengsb.core.api.workflow.WorkflowException;
import org.openengsb.core.api.workflow.model.Task;
import org.openengsb.ui.common.taskbox.web.TaskOverviewPanel;
import org.openengsb.ui.common.taskbox.web.TaskPanel;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebTaskboxServiceImpl implements TaskboxService, WebTaskboxService {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebTaskboxServiceImpl.class);

    private PersistenceService persistence;
    private TaskboxService taskboxService;
    private BundleContext bundleContext;
    private PersistenceManager persistenceManager;

    public void init() {
        persistence = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());
    }

    @Override
    public Panel getOverviewPanel() {
        return new TaskOverviewPanel("OverviewPanel");
    }

    @Override
    public Panel getTaskPanel(Task task, String wicketPanelId) throws TaskboxException {
        List<PanelRegistryEntry> panels = persistence.query(new PanelRegistryEntry(task.getTaskType()));

        Class<? extends Panel> panelClass;
        if (panels.size() > 0) {
            panelClass = panels.get(0).getPanelClass();
        } else {
            panelClass = TaskPanel.class;
        }

        try {
            Constructor<? extends Panel> panelConstructor = panelClass.getConstructor(String.class, Task.class);
            return panelConstructor.newInstance(wicketPanelId, task);
        } catch (Exception e) {
            throw new TaskboxException(e);
        }
    }

    @Override
    public void registerTaskPanel(String taskType, Class<? extends Panel> panelClass) throws TaskboxException {
        try {
            if (persistence.query(new PanelRegistryEntry(taskType)).size() > 0) {
                persistence.delete(new PanelRegistryEntry(taskType));
            }
            persistence.create(new PanelRegistryEntry(taskType, panelClass));
            LOGGER.info("Successfully registered {} for task type {}", panelClass.getName(), taskType);
        } catch (PersistenceException e) {
            throw new TaskboxException(e);
        }
    }

    @Override
    public List<Task> getOpenTasks() {
        return taskboxService.getOpenTasks();
    }

    @Override
    public List<Task> getTasksForExample(Task example) {
        return taskboxService.getTasksForExample(example);
    }

    @Override
    public Task getTaskForId(String id) throws TaskboxException {
        return taskboxService.getTaskForId(id);
    }

    @Override
    public List<Task> getTasksForProcessId(String id) {
        return taskboxService.getTasksForProcessId(id);
    }

    @Override
    public void finishTask(Task task) throws WorkflowException {
        taskboxService.finishTask(task);
    }

    public void setTaskboxService(TaskboxService taskboxService) {
        this.taskboxService = taskboxService;
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}
