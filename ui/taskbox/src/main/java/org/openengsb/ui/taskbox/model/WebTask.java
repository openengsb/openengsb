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

package org.openengsb.ui.taskbox.model;

import org.apache.wicket.markup.html.panel.Panel;
import org.openengsb.core.common.taskbox.model.ProcessBag;
import org.openengsb.core.common.taskbox.model.Task;

/**
 * WebTask extends a normal Task by adding graphical Functionality so it can be
 * used and displayed in Web-Applications
 */
public class WebTask extends Task {

    public WebTask(String taskId) {
        super(taskId);
    }

    public WebTask(String taskId, ProcessBag processBag) {
        super(taskId, processBag);
    }

    public WebTask(String taskId, String taskType) {
        super(taskId, taskType);
    }

    public WebTask(String taskId, String taskType, ProcessBag processBag) {
        super(taskId, taskType, processBag);
    }

    /**
     * returns the according Wicket Panel for the task can be used to give an
     * Overview over a Task and its contained TaskSteps
     */
    public Panel getPanel(String id) {
        // Standard-Panel WebTaskPanel
        // oder Task-spezifisches bzw. processBag-spezifisches Panel
        // zurückgeben...
        return new Panel(id);
    }
}
