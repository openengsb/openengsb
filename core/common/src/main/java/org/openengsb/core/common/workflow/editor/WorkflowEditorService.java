package org.openengsb.core.common.workflow.editor;

import java.util.List;

public interface WorkflowEditorService {

    public List<String> getWorkflowNames();

    public Workflow loadWorkflow(String name);

    public Workflow getCurrentWorkflow();

    public void saveCurrentWorkflow();

    public void createWorkflow(String name);
}
