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

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilteredAbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.GoAndClearFilter;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.TextFilteredPropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.openengsb.core.workflow.api.TaskboxException;
import org.openengsb.core.workflow.api.model.Task;
import org.openengsb.ui.common.taskbox.WebTaskboxService;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskOverviewPanel extends Panel {

    private static final long serialVersionUID = -8724223987824602812L;

    public static final Logger LOGGER = LoggerFactory.getLogger(TaskOverviewPanel.class);

    private TaskDataProvider dataProvider = new TaskDataProvider();
    private Panel panel = (Panel) new EmptyPanel("taskPanel").setOutputMarkupId(true);

    @PaxWicketBean(name = "webtaskboxService")
    private WebTaskboxService webtaskboxService;

    @SuppressWarnings("serial")
    public TaskOverviewPanel(String id) {
        super(id);
        setOutputMarkupId(true);
        ArrayList<IColumn<Task>> columns = new ArrayList<IColumn<Task>>();
        IColumn<Task> actionsColumn = new FilteredAbstractColumn<Task>(Model.of("Actions")) {
            @Override
            public Component getFilter(String componentId, FilterForm<?> form) {
                return new GoAndClearFilter(componentId, form);
            }

            @Override
            @SuppressWarnings("rawtypes")
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                final Task task = (Task) rowModel.getObject();
                cellItem.add(new UserActionsPanel(componentId, task));
            }
        };
        columns.add(actionsColumn);
        columns.add(new TextFilteredPropertyColumn<Task, String>(Model.of("TaskId"), "taskId", "taskId"));
        columns.add(new TextFilteredPropertyColumn<Task, String>(Model.of("TaskType"), "taskType", "taskType"));
        columns
            .add(new TextFilteredPropertyColumn<Task, String>(Model.of("Description"), "description", "description"));
        columns.add(new PropertyColumn<Task>(Model.of("TaskCreationTimestamp"), "taskCreationTimestamp",
            "taskCreationTimestamp"));
        FilterForm<Task> form = new FilterForm<Task>("form", dataProvider);
        DefaultDataTable<Task> dataTable = new DefaultDataTable<Task>("dataTable", columns, dataProvider, 15);
        dataTable.addTopToolbar(new FilterToolbar(dataTable, form, dataProvider));
        form.add(dataTable);
        add(form);
        add(panel);
    }

    private class UserActionsPanel extends Panel {

        private static final long serialVersionUID = -8163071854733837122L;

        private Task task;

        @SuppressWarnings("serial")
        public UserActionsPanel(String id, Task t) {
            super(id);
            task = t;
            AjaxLink<Task> link = new AjaxLink<Task>("taskLink") {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    try {
                        Panel newPanel = webtaskboxService.getTaskPanel(task, "taskPanel");
                        newPanel.setOutputMarkupId(true);
                        panel.replaceWith(newPanel);
                        panel = newPanel;
                        target.add(panel);
                    } catch (TaskboxException e) {
                        LOGGER.error("Taskbox panel could not be started", e);
                    }
                }
            };
            link.add(new Label("linkLabel", String.format("%s (%s)", task == null ? "null" : task.getName(),
                task == null ? "null" : task.getTaskType())));
            add(link);
        }

    }

}
