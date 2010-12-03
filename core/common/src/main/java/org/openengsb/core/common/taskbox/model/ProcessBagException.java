package org.openengsb.core.common.taskbox.model;

import org.openengsb.core.common.workflow.WorkflowException;

@SuppressWarnings("serial")
public class ProcessBagException extends WorkflowException {

    public ProcessBagException(String message) {
        super(message);
    }

}
