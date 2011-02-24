package org.openengsb.core.common.workflow.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowEditorServiceImpl implements WorkflowEditorService {

    private final Map<String, Workflow> workflows = new HashMap<String, Workflow>();

    private Workflow currentWorkflow;

    @Override
    public List<String> getWorkflowNames() {
        ArrayList<String> arrayList = new ArrayList<String>(this.workflows.keySet());
        Collections.sort(arrayList);
        return arrayList;
    }

    @Override
    public Workflow loadWorkflow(String name) {
        if (workflows.containsKey(name)) {
            currentWorkflow = workflows.get(name);
            return currentWorkflow;
        } else {
            throw new IllegalArgumentException("Workflow Name doesn't exist");
        }
    }

    @Override
    public Workflow getCurrentWorkflow() {
        return currentWorkflow;
    }

    @Override
    public void saveCurrentWorkflow() {
        // TODO Auto-generated method stub

    }

    @Override
    public void createWorkflow(String name) {
        if (workflows.containsKey(name)) {
            throw new IllegalArgumentException("Workflow with same name already exists");
        } else {
            Workflow workflow = new Workflow();
            workflow.setName(name);
            workflows.put(name, workflow);
            currentWorkflow = workflow;
        }
    }
}
