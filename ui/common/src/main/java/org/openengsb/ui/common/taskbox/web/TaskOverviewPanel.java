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
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilteredAbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.GoAndClearFilter;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.TextFilteredPropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.api.workflow.TaskboxException;
import org.openengsb.core.api.workflow.model.Task;
import org.openengsb.ui.common.taskbox.WebTaskboxService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class TaskOverviewPanel extends Panel {

    public static final Logger LOGGER = LoggerFactory.getLogger(TaskOverviewPanel.class);

    private TaskDataProvider dataProvider = new TaskDataProvider();
    private Panel panel = new EmptyPanel("taskPanel");

    @SpringBean(name = "webtaskboxService")
    private WebTaskboxService webtaskboxService;

    public TaskOverviewPanel(String id) {
        super(id);
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

        FilterForm<Object> form = new FilterForm<Object>("form", dataProvider);

        DefaultDataTable<Task> dataTable = new DefaultDataTable<Task>("dataTable", columns, dataProvider, 15);
        dataTable.addTopToolbar(new FilterToolbar(dataTable, form, dataProvider));
        form.add(dataTable);
        add(form);
        add(panel);

    }

    private class UserActionsPanel extends Panel {

        private Task task;

        public UserActionsPanel(String id, Task t) {
            super(id);
            task = t;
            Link<Task> link = new Link<Task>("taskLink") {
                @Override
                public void onClick() {
                    try {
                        Panel newPanel = webtaskboxService.getTaskPanel(task, "taskPanel");
                        newPanel.setOutputMarkupId(true);
                        panel.replaceWith(newPanel);
                        panel = newPanel;
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
