package org.openengsb.ui.taskbox;

import java.util.Iterator;

import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.openengsb.core.common.taskbox.TaskboxService;
import org.openengsb.core.common.taskbox.model.Task;

@SuppressWarnings("serial")
public class TaskDataProvider extends SortableDataProvider<Task> implements IFilterStateLocator {

    private TaskboxService taskboxService;
    private Task filter;

    @Override
    public Iterator<? extends Task> iterator(int first, int count) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IModel<Task> model(Task object) {
        return new LoadableTaskModel(object.getTaskId(), taskboxService);
    }

    public void setTaskboxService(TaskboxService taskboxService) {
        this.taskboxService = taskboxService;
    }

    @Override
    public Object getFilterState() {
        return filter;
    }

    @Override
    public void setFilterState(Object state) {
        filter = (Task) state;
    }

}
