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
import org.openengsb.core.common.taskbox.model.Task;

/**
 * WebTask extends a normal Task by adding graphical Functionality so it can be used and displayed in Web-Applications -
 * that means that a any Panel can be associated with a WebTask
 */
public class WebTask extends Task {
    Class<? extends Panel> panelClass;

    public WebTask() {
        super();
        // panelClass = DefaultWebTaskPanel.class;
    }

    public WebTask(String processId, String context, String user) {
        super(processId, context, user);
        // panelClass = DefaultWebTaskPanel.class;
    }

    public WebTask(String taskType) {
        super(taskType);
        // panelClass = DefaultWebTaskPanel.class;
    }

    public WebTask(String taskType, String processId, String context, String user) {
        super(taskType, processId, context, user);
        // panelClass = DefaultWebTaskPanel.class;
    }

    public void setNull() {
        super.setNull();
        // panelClass = DefaultWebTaskPanel.class;
    }

    public static Task returnNullTask() {
        WebTask wt = new WebTask();
        wt.setNull();
        return wt;
    }

    /**
     * returns the according Wicket Panel Class for the task
     */
    public Class<? extends Panel> getPanelClass() {
        return panelClass;
    }

    /**
     * sets the according Wicket Panel Class for the task
     */
    public void setPanelClass(Class<? extends Panel> panelClass) {
        this.panelClass = panelClass;
    }
}
