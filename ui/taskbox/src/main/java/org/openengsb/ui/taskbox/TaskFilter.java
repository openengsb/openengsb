package org.openengsb.ui.taskbox;

import org.openengsb.core.common.taskbox.model.Task;

@SuppressWarnings("serial")
public class TaskFilter extends Task {

    public static TaskFilter createTaskFilter() {
        TaskFilter filter = new TaskFilter();
        filter.removeAllProperties();
        filter.setEmpty();
        return filter;
    }

    public boolean match(Task task) {

        if (this.getTaskId() != null) {
            if (!task.getTaskId().startsWith(this.getTaskId())) {
                return false;
            }
        }

        if (this.getTaskType() != null) {
            if (!task.getTaskType().startsWith(this.getTaskType())) {
                return false;
            }
        }

        if (this.getDescription() != null) {
            if (!(task.getDescription().toLowerCase().indexOf(this.getDescription().toLowerCase()) > -1)) {
                return false;
            }
        }

        return true;
    }
}
