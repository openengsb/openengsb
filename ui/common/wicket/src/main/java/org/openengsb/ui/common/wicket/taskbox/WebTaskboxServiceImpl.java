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

package org.openengsb.ui.common.wicket.taskbox;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.markup.html.panel.Panel;
import org.openengsb.core.common.taskbox.TaskboxException;
import org.openengsb.core.common.taskbox.model.Task;
import org.openengsb.core.workflow.taskbox.TaskboxServiceImpl;
import org.openengsb.ui.common.wicket.taskbox.web.TaskOverviewPanel;
import org.openengsb.ui.common.wicket.taskbox.web.TaskPanel;

public class WebTaskboxServiceImpl extends TaskboxServiceImpl implements WebTaskboxService {

    private Map<String, Class<?>> panelMap = new HashMap<String, Class<?>>();

    @Override
    public Panel getOverviewPanel() {
        return new TaskOverviewPanel("OverviewPanel");
    }

    @Override
    public Panel getTaskPanel(Task task, String wicketPanelId) throws TaskboxException {
        if (panelMap.containsKey(task.getTaskType())) {
            Panel p = null;
            try {
                Class<?> panelClass = panelMap.get(task.getTaskType());
                Constructor<?> panelConstructor = panelClass.getConstructor(String.class, Task.class);
                p = (Panel) panelConstructor.newInstance(wicketPanelId, task);
            } catch (Exception e) {
                throw new TaskboxException(e);
            }
            return p;
        } else {
            registerTaskPanel(task.getTaskType(), TaskPanel.class);
            return getTaskPanel(task, wicketPanelId);
        }
    }

    @Override
    public void registerTaskPanel(String taskType, Class<?> panelClass) {
        panelMap.put(taskType, panelClass);
    }
}
