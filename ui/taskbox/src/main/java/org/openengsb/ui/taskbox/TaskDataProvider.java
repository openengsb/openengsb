package org.openengsb.ui.taskbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.model.IModel;
import org.openengsb.core.common.taskbox.TaskboxService;
import org.openengsb.core.common.taskbox.model.Task;

@SuppressWarnings("serial")
public class TaskDataProvider extends SortableDataProvider<Task> implements IFilterStateLocator {

    private TaskboxService taskboxService;
    private TaskFilter filter;
    private List<Task> list;

    @Override
    public Iterator<? extends Task> iterator(int first, int count) {
        initList();

        List<Task> ret = list;
        if (ret.size() > (first + count)) {
            ret = ret.subList(first, first + count);
        } else {
            ret = ret.subList(first, ret.size());
        }
        return ret.iterator();
    }

    private void initList() {
        if (list == null) {
            SortParam sp = this.getSort();
            list=getSortedandFilteredList(sp);   
        }
    }

    private List<Task> getSortedandFilteredList(SortParam sortParam) {
        List<Task> result = taskboxService.getOpenTasks();

        if (result != null) {
            filterTasks(result, filter);
            Collections.sort(result, new TaskComparator());
        } else {
            result = new ArrayList<Task>();
        }
        return result;
        
    }

    private void filterTasks(List<Task> result, TaskFilter filter2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int size() {
        initList();
        return list.size();
    }

    @Override
    public void detach() {
        list = null;
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
        filter = (TaskFilter) state;
    }

}
