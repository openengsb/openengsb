package org.openengsb.core.common.taskbox.model;

import org.openengsb.core.common.workflow.model.InternalWorkflowEvent;
import org.openengsb.core.common.workflow.model.ProcessBag;

public class TaskFinishedEvent extends InternalWorkflowEvent {
    public TaskFinishedEvent(ProcessBag processBag){
        super(processBag);
        this.setType("TaskFinished");
    }
}
