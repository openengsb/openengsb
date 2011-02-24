package org.openengsb.core.common.workflow.editor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

public class WorkflowEditorServiceImplTest {

    @Test
    public void createWorkflow_ShouldBeSetAsCurrentWorkflow() {
        WorkflowEditorServiceImpl service = new WorkflowEditorServiceImpl();
        String name = "name";
        service.createWorkflow(name);
        Workflow currentWorkflow = service.getCurrentWorkflow();
        assertThat(name, equalTo(currentWorkflow.getName()));
    }

    @Test
    public void loadWorkflow_ShouldBeSetAsCurrentWorkflow() {
        WorkflowEditorServiceImpl service = new WorkflowEditorServiceImpl();
        String name = "name";
        service.createWorkflow(name);
        String string = "123";
        service.createWorkflow(string);
        assertThat(string, equalTo(service.getCurrentWorkflow().getName()));
        service.loadWorkflow(name);
        assertThat(name, equalTo(service.getCurrentWorkflow().getName()));
    }

    @Test
    public void getWorkflowName_ShouldBeSetAsCurrentWorkflow() {
        WorkflowEditorServiceImpl service = new WorkflowEditorServiceImpl();
        String name = "name";
        service.createWorkflow(name);
        String string = "123";
        service.createWorkflow(string);
        assertThat(string, equalTo(service.getWorkflowNames().get(0)));
        assertThat(name, equalTo(service.getWorkflowNames().get(1)));
    }

    @Test
    public void callCurrentWorkflow_shouldReturnNullWhenNoWorkflowSelected() {
        assertThat(null, equalTo(new WorkflowEditorServiceImpl().getCurrentWorkflow()));
    }
}
