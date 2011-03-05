/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.ui.common.taskbox;

import java.lang.reflect.Constructor;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.markup.html.panel.Panel;
import org.openengsb.core.common.BundleContextAware;
import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceManager;
import org.openengsb.core.common.persistence.PersistenceService;
import org.openengsb.core.common.taskbox.TaskboxException;
import org.openengsb.core.common.taskbox.model.Task;
import org.openengsb.core.workflow.taskbox.TaskboxServiceImpl;
import org.openengsb.ui.common.taskbox.web.TaskOverviewPanel;
import org.openengsb.ui.common.taskbox.web.TaskPanel;
import org.osgi.framework.BundleContext;

public class WebTaskboxServiceImpl extends TaskboxServiceImpl implements WebTaskboxService, BundleContextAware {
    private Log log = LogFactory.getLog(getClass());

    private PersistenceService persistence;
    private PersistenceManager persistenceManager;
    private BundleContext bundleContext;

    @Override
    public void init() {
        persistence = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());
    }

    @Override
    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
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
            log.info("Successfully registered " + panelClass.getName() + " for task type " + taskType);
        } catch (PersistenceException e) {
            throw new TaskboxException(e);
        }
    }
}
