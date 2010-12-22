package org.openengsb.ui.taskbox;

import java.util.Comparator;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.openengsb.core.common.taskbox.model.Task;

public class TaskComparator implements Comparator<Task> {

    SortParam sp;

    public TaskComparator(SortParam sp) {
        this.sp = sp;
    }

    @Override
    public int compare(Task arg0, Task arg1) {
        int ret = 0;

        if (sp.getProperty().equals("taskId")) {
            ret = arg0.getTaskId().compareTo(arg1.getTaskId());
        } else {

            if (sp.getProperty().equals("taskType")) {
                ret = arg0.getTaskType().compareTo(arg1.getTaskType());
            } else {

                if (sp.getProperty().equals("description")) {
                    ret = arg0.getDescription().compareTo(arg1.getDescription());
                } else {

                    if (sp.getProperty().equals("taskCreationTimestamp")) {
                        ret = arg0.getTaskCreationTimestamp().compareTo(arg1.getTaskCreationTimestamp());
                    }

                }
            }
        }
        if (!sp.isAscending()) {
            return ret * -1;
        }
        return ret;
    }

}
