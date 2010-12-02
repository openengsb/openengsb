package org.openengsb.core.common.taskbox.model;

public class TaskFinishedEvent extends InternalWorkflowEvent {
    public TaskFinishedEvent(ProcessBag processBag){
        super(processBag);
        this.setType("TaskFinished");
    }
}
