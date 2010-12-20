package org.openengsb.ui.taskbox;

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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.core.common.taskbox.model.Task;

import sun.security.krb5.internal.Ticket;

public class TaskOverviewPanel extends Panel {

    @SpringBean
    TaskDataProvider dataProvider;
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public TaskOverviewPanel(String id) {
        super(id);
        ArrayList<IColumn<Task>> columns = new ArrayList<IColumn<Task>>();

        IColumn actionsColumn = new FilteredAbstractColumn<Ticket>(Model.of("Actions")) {

            @Override
            public Component getFilter(String componentId, FilterForm form) {
                return new GoAndClearFilter(componentId, form);
            }

            @Override
            public void populateItem(Item cellItem, String componentId, IModel rowModel) {
                final Task task = (Task) rowModel.getObject();
                // TODO: ADD link to LPuschmanns Panel.
            }
        };
        columns.add(actionsColumn);

        columns.add(new TextFilteredPropertyColumn<Task, String>(Model.of("TaskId"), "taskId", "taskId"));
        columns.add(new TextFilteredPropertyColumn<Task, String>(Model.of("TaskType"), "taskType", "taskType"));
        columns.add(new TextFilteredPropertyColumn<Task, String>(Model.of("Description"), "description", "description"));
        columns.add(new PropertyColumn(Model.of("TaskCreationTimestamp"), "taskCreationTimestamp", "taskCreationTimestamp"));
        FilterForm form = new FilterForm("form", dataProvider);

        DefaultDataTable<Task> dataTable = new DefaultDataTable<Task>("dataTable", columns, dataProvider, 10);
        dataTable.addTopToolbar(new FilterToolbar(dataTable, form, dataProvider));
        form.add(dataTable);

        add(form);
    }
    
    

}
