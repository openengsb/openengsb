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

import org.apache.wicket.markup.html.panel.Panel;
import org.openengsb.core.workflow.api.TaskboxException;
import org.openengsb.core.workflow.api.TaskboxService;
import org.openengsb.core.workflow.api.model.Task;

/**
 * The WebTaskboxService extends the normal {@link org.openengsb.core.common.taskbox.TaskboxService TaskboxService} by
 * adding some Wicket UI functionality. This includes the provisioning of tasks panels, an overview panel to display
 * open tasks and a registration mechanism to define custom panels for certain task types.
 */
public interface WebTaskboxService extends TaskboxService {
    /**
     * Returns a Wicket panel which displays all open tasks. It provides filtering and sorting capabilities.
     *
     * This panel has {@code componentId="OverviewPanel"} and style attribute {@code class="OverviewPanel"}.
     */
    Panel getOverviewPanel();

    /**
     * Gets the Wicket panel for the task type of the passed task. If a custom panel was registered for this type before
     * it gets returned, otherwise it falls back to the default task panel providing a generic user interface.
     *
     * @throws TaskboxException when the creation of the tasks panel fails
     */
    Panel getTaskPanel(Task task, String wicketPanelId) throws TaskboxException;

    /**
     * Registers a custom Wicket panel for a certain task type. Any older registration for this type gets overwritten.
     *
     * The panel is provided via its class and needs a constructor with the ID as string for the first parameter and the
     * {@link org.openengsb.core.common.taskbox.model.Task Task} as second.
     *
     * @throws TaskboxException when the panel could not be registered
     */
    void registerTaskPanel(String taskType, Class<? extends Panel> panelClass) throws TaskboxException;
}
