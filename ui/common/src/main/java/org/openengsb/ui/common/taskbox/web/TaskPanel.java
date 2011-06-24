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

package org.openengsb.ui.common.taskbox.web;

import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.validation.validator.StringValidator;
import org.openengsb.core.api.workflow.TaskboxService;
import org.openengsb.core.api.workflow.WorkflowException;
import org.openengsb.core.api.workflow.model.Task;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class TaskPanel extends Panel {
    private Task task;

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskPanel.class);

    @PaxWicketBean(name = "taskboxService")
    private TaskboxService service;

    public TaskPanel(String id, Task t) {
        super(id);
        task = t;

        add(new Label("taskid", task.getTaskId()));
        add(new Label("taskname", task.getName()));
        add(new Label("tasktype", task.getTaskType() != null ? task.getTaskType() : "N/A"));

        add(new Label("taskdescription", task.getDescription() != null ? task.getDescription() : "N/A"));

        // CompoundPropertyModel<Task> taskModel = new CompoundPropertyModel<Task>(task);
        Form<Task> form = new Form<Task>("inputForm");
        form.setOutputMarkupId(true);

        form.add(new Label("taskid", new PropertyModel<String>(task, "taskId")));
        form.add(new TextField<String>("taskname", new PropertyModel<String>(task, "name")).setRequired(true).add(
                StringValidator.minimumLength(2)));
        form.add(new TextField<String>("tasktype", new PropertyModel<String>(task, "taskType")).setRequired(true).add(
            StringValidator.minimumLength(2)));
        form.add(new Label("taskcreationTimestamp",
                task.getTaskCreationTimestamp() != null ? task.getTaskCreationTimestamp().toString() : "N/A"));
        form.add(new TextArea<String>("taskdescription", new PropertyModel<String>(task, "description"))
            .setRequired(true));

        form.add(new AjaxButton("submitButton", form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    service.finishTask(task);
                    setResponsePage(getPage().getClass());
                } catch (WorkflowException e) {
                    LOGGER.error("Cant finish task", e);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
            }
        });
        add(form);
        add(new ListView<String>("propertiesList", new ArrayList<String>(task.propertyKeySet())) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("propertiesLabel", item.getModel()));
            }
        });
    }
}
