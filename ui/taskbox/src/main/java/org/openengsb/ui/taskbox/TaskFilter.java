package org.openengsb.ui.taskbox;

import org.openengsb.core.common.taskbox.model.Task;

public class TaskFilter extends Task {
    public boolean match(Task task) {
        boolean ret = true;
        
        //TODO: Reflection for comparing properties with startswith.
        return ret;
    }
}
