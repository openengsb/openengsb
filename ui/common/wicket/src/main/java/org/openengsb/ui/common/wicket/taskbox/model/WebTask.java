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

package org.openengsb.ui.common.wicket.taskbox.model;

import org.apache.wicket.markup.html.panel.Panel;
import org.openengsb.core.common.taskbox.model.Task;
import org.openengsb.core.common.workflow.model.ProcessBag;

/**
 * WebTask extends a normal Task by adding graphical functionality so it can be used and displayed in Wicket web
 * application. It holds a generic wicket panel to display all containing properties. This panel can easily be
 * overwritten by a custom panel.
 */
@SuppressWarnings("serial")
public class WebTask extends Task {
    public WebTask() {
        super();
    }

    public WebTask(ProcessBag bag) {
        super(bag);
    }

    /**
     * returns the according Wicket panel class for the task
     */
    @SuppressWarnings("unchecked")
    public Class<? extends Panel> getPanelClass() {
        return (Class<? extends Panel>) getProperty("panelClass");
    }

    /**
     * sets the according Wicket panel class for the task
     */
    public void setPanelClass(Class<? extends Panel> panelClass) {
        addOrReplaceProperty("panelClass", panelClass);
    }
}

