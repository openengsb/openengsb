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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.StringValidator;
import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.taskbox.TaskboxException;
import org.openengsb.core.common.taskbox.TaskboxService;
import org.openengsb.core.common.taskbox.model.Task;
import org.openengsb.core.common.workflow.WorkflowException;
import org.openengsb.ui.taskbox.TaskOverviewPanel;
import org.openengsb.ui.taskbox.model.WebTask;

public class CustomTaskPanel extends Panel {

    private Task task;

    @SpringBean(name="taskboxService")
    private TaskboxService service;

    @SuppressWarnings("serial")
    public CustomTaskPanel(String id, Task t) {
        super(id);
        this.task = t;
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);
        
        add(new Label("taskid", task.getTaskId()));
        add(new Label("taskname", task.getName()));
        add(new Label("tasktype", task.getTaskType() != null ? task.getTaskType() : "N/A"));
        
        add(new Label("taskdescription", task.getDescription() != null ? task.getDescription() : "N/A"));
        
        CompoundPropertyModel<Task> taskModel = new CompoundPropertyModel<Task>(task);
        Form<Task> form = new Form<Task>("inputForm", taskModel);
        form.setOutputMarkupId(true);

        form.add(new Label("taskid", taskModel.bind("taskId")));
        form.add(new TextField("taskname", taskModel.bind("name")).setRequired(true).add(
                StringValidator.minimumLength(2)));
        form.add(new TextField("tasktype", taskModel.bind("taskType")).setRequired(true).add(
            StringValidator.minimumLength(2)));
        form.add(new Label("taskcreationTimestamp", task.getTaskCreationTimestamp() != null ? task.getTaskCreationTimestamp()
                .toString() : "N/A"));
        form.add(new TextArea("taskdescription", taskModel.bind("description")).setRequired(true));

        form.add(new AjaxButton("submitButton", form)
        {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    service.finishTask(task);
                    //setResponsePage(TaskOverviewPage.class);
                } catch (WorkflowException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.addComponent(feedback);
            }
        });
        add(form);
        
        
        
        
        
        add(new ListView("propertiesList", new ArrayList<String>(task.propertyKeySet())) {
            @Override
            protected void populateItem(ListItem item) {
                item.add(new Label("propertiesLabel", item.getModel()));
            }
        });
    }
}
