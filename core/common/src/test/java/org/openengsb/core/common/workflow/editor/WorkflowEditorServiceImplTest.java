/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
