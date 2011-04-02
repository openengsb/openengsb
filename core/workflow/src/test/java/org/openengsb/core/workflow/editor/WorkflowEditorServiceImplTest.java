/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.workflow.editor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.Before;
import org.junit.Test;

public class WorkflowEditorServiceImplTest {

    private WorkflowEditorServiceImpl service;
    private String name;

    @Before
    public void before() {
        service = new WorkflowEditorServiceImpl();
        name = "name";
        service.createWorkflow(name);
    }

    @Test
    public void createWorkflow_ShouldBeSetAsCurrentWorkflow() {
        Workflow currentWorkflow = service.getCurrentWorkflow();
        assertThat(name, equalTo(currentWorkflow.getName()));
    }

    @Test
    public void loadWorkflow_ShouldBeSetAsCurrentWorkflow() {
        String string = "123";
        service.createWorkflow(string);
        assertThat(string, equalTo(service.getCurrentWorkflow().getName()));
        service.loadWorkflow(name);
        assertThat(name, equalTo(service.getCurrentWorkflow().getName()));
    }

    @Test
    public void getWorkflowName_ShouldBeInCorrectOrder() {
        String string = "123";
        service.createWorkflow(string);
        assertThat(string, equalTo(service.getWorkflowNames().get(0)));
        assertThat(name, equalTo(service.getWorkflowNames().get(1)));
    }

    @Test
    public void addEndNode_ShouldBeAddedInCorrectOrder() {
        Workflow currentWorkflow = service.getCurrentWorkflow();
        currentWorkflow.addEndNode(new End(name));
        String string = name + "1";
        currentWorkflow.addEndNode(new End(string));
        assertThat(currentWorkflow.getEndNodes().size(), equalTo(2));
        End end1 = currentWorkflow.getEndNodes().get(0);
        assertThat(end1.getName(), equalTo(name));
        assertThat(currentWorkflow.getEndNodes().get(1).getName(), equalTo(string));
        currentWorkflow.getRoot().setEnd(end1);
        assertThat(currentWorkflow.getRoot().getEnd(), sameInstance(end1));
    }

    @Test
    public void defaultEndNode_ShouldExist() {
        assertThat(service.getCurrentWorkflow().getRoot().getEnd().getName(), equalTo("Default"));
    }

    @Test
    public void callCurrentWorkflow_shouldReturnNullWhenNoWorkflowSelected() {
        assertThat(null, equalTo(new WorkflowEditorServiceImpl().getCurrentWorkflow()));
    }
}
