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

package org.openengsb.ui.taskbox.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.taskbox.TaskboxService;
import org.openengsb.core.common.taskbox.model.Task;
import org.openengsb.ui.taskbox.model.WebTask;

public class TaskPanel extends Panel {

    private WebTask task;

    @SpringBean
    private TaskboxService service;

    @SuppressWarnings("serial")
    public TaskPanel(String id, Task t) {
        super(id);
        this.task = (WebTask) t;
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);
        add(new Label("taskid", task.getTaskId()));
        add(new Label("taskname", task.getName()));
        add(new Label("tasktype", task.getTaskType() != null ? task.getTaskType() : "N/A"));
        add(new Label("taskcreationTimestamp", task.getTaskCreationTimestamp() != null ? task.getTaskCreationTimestamp()
            .toString() : "N/A"));
        add(new Label("taskdescription", task.getDescription() != null ? task.getDescription() : "N/A"));
        
        add(new ListView("propertiesList", new ArrayList<String>(task.propertyKeySet())) {
            @Override
            protected void populateItem(ListItem item) {
                item.add(new Label("propertiesLabel", item.getModel()));
                item.add(new Label("propertiesValueLabel", task.getPropertyClass(item.getModel().toString()).getCanonicalName()));
            }
        });
    }
}
