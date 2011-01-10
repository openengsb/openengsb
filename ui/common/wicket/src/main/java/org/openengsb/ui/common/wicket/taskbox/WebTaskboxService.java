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

import org.apache.wicket.markup.html.panel.Panel;
import org.openengsb.core.common.taskbox.TaskboxException;
import org.openengsb.core.common.taskbox.TaskboxService;
import org.openengsb.core.common.taskbox.model.Task;

/**
 * The WebTaskboxService extends the normal TaskboxService with a function to generate a standard overview panel.
 */
public interface WebTaskboxService extends TaskboxService {

    /**
     * Generates a standard Wicket-Panel, which displays tasks out of the persistence.
     * The panel has the componentId="OverviewPanel" and the style attribute class="OverviewPanel"
     */
    Panel getOverviewPanel();

    /**
     * Gets the Wicket Panel for a Specific Task if it is registered.
     * If Panel is not registered, returns the Default-TaskPanel
     */
    Panel getTaskPanel(Task task, String wicketPanelId) throws TaskboxException;

    /**
     * Register a Specific Panel for a predefined Tasktype
     */
    void registerTaskPanel(String taskType, Class<?> panelClass);
}
