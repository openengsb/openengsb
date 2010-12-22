package org.openengsb.ui.taskbox;

import org.apache.wicket.model.LoadableDetachableModel;
import org.openengsb.core.common.persistence.PersistenceService;
import org.openengsb.core.common.taskbox.TaskboxService;
import org.openengsb.core.common.taskbox.model.Task;

@SuppressWarnings("serial")
public class LoadableTaskModel extends LoadableDetachableModel<Task> {

    private final String taskId;
    private final TaskboxService taskboxService;

    public LoadableTaskModel(String taskId, TaskboxService taskboxService) {
        this.taskId = taskId;
        this.taskboxService = taskboxService;
    }

    @Override
    protected Task load() {
        return taskboxService.getTaskForId(taskId);
    }

}
