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

package org.openengsb.ui.admin.workflowEditor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.workflow.WorkflowEditorService;
import org.openengsb.core.api.workflow.model.ActionRepresentation;
import org.openengsb.core.api.workflow.model.EventRepresentation;
import org.openengsb.core.api.workflow.model.WorkflowRepresentation;
import org.openengsb.core.services.internal.WorkflowEditorServiceImpl;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullEvent;
import org.openengsb.ui.admin.model.OpenEngSBVersion;
import org.openengsb.ui.admin.workflowEditor.action.EditAction;

public class WorkflowEditorTest {

    private WicketTester tester;
    private ApplicationContextMock mock;
    private WorkflowEditorService service;

    @Before
    public void setup() {
        tester = new WicketTester();
        mock = new ApplicationContextMock();
        service = new WorkflowEditorServiceImpl();
        mock.putBean(mock(ContextCurrentService.class));
        mock.putBean("openengsbVersion", new OpenEngSBVersion());
        mock.putBean("workflowEditorService", service);
        tester.getApplication().addComponentInstantiationListener(
            new SpringComponentInjector(tester.getApplication(), mock, true));
        tester.startPage(new WorkflowEditor());
    }

    @Test
    public void withoutWorkflow_partsShouldBeInvisible() {
        tester.assertInvisible("workflowSelectForm");
        tester.assertInvisible("treeTable");
    }

    @Test
    public void clickAddAction_shouldOpenEditActionPage() {
        service.createWorkflow("Workflow");
        tester.startPage(new WorkflowEditor());
        tester.clickLink("treeTable:i:0:middleColumns:links:create-action");
        tester.assertRenderedPage(EditAction.class);
    }

    @Test
    public void selectWorkflow_ShouldShowWorkflowActionDescriptions() {
        String string = "Workflow";
        service.createWorkflow(string);
        WorkflowRepresentation currentWorkflow = service.getCurrentWorkflow();
        ActionRepresentation action = new ActionRepresentation();
        action.setDomain(NullDomain.class);
        Method method = NullDomain.class.getMethods()[0];
        action.setMethodName(method.getName());
        action.setMethodParameters(Arrays.asList(method.getParameterTypes()));
        String location = "Location";
        action.setLocation(location);
        EventRepresentation event = new EventRepresentation();
        event.setEvent(NullEvent.class);
        action.addEvent(event);
        currentWorkflow.getRoot().addAction(action);
        service.createWorkflow("Second");
        tester.startPage(WorkflowEditor.class);
        FormTester formTester = tester.newFormTester("workflowSelectForm");
        formTester.select("workflowSelect", 1);
        formTester.submit();
        tester.assertRenderedPage(WorkflowEditor.class);
        tester.assertLabel("currentWorkflowName", string);
        assertThat(string, equalTo(service.getCurrentWorkflow().getName()));
    }

    @Test
    public void callCreateWorkflow_shouldCreateWorkflow() {
        FormTester createEmptyWorkflow = tester.newFormTester("workflowCreateForm");
        createEmptyWorkflow.submit();
        tester.assertRenderedPage(WorkflowEditor.class);
        assertThat(service.getWorkflowNames().size(), equalTo(0));
        assertThat(service.getCurrentWorkflow(), equalTo(null));

        tester.assertLabel("currentWorkflowName", "Please create Workflow first");
        FormTester createForm = tester.newFormTester("workflowCreateForm");
        createForm.setValue("name", "Name");
        createForm.submit();
        assertThat("Name", equalTo(service.getCurrentWorkflow().getName()));
        tester.assertRenderedPage(EditAction.class);
    }

    @Test
    public void removeAction_ShouldRemoveActionFromWorkflow() {
        service.createWorkflow("default");
        ActionRepresentation action = new ActionRepresentation();
        action.setLocation("location");
        action.setDomain(NullDomain.class);
        action.setMethodName(NullDomain.class.getMethods()[0].getName());
        service.getCurrentWorkflow().getRoot().addAction(action);
        tester.startPage(WorkflowEditor.class);
        assertThat(service.getCurrentWorkflow().getRoot().getActions().size(), equalTo(1));
        tester.clickLink("treeTable:i:1:middleColumns:links:remove");
        assertThat(service.getCurrentWorkflow().getRoot().getActions().size(), equalTo(0));
    }

    @Test
    public void removeEvent_ShouldRemoveEventFromWorkflow() {
        service.createWorkflow("default");
        EventRepresentation event = new EventRepresentation();
        event.setEvent(NullEvent.class);
        service.getCurrentWorkflow().getRoot().addEvent(event);
        tester.startPage(WorkflowEditor.class);
        assertThat(service.getCurrentWorkflow().getRoot().getEvents().size(), equalTo(1));
        tester.clickLink("treeTable:i:1:middleColumns:links:remove");
        assertThat(service.getCurrentWorkflow().getRoot().getEvents().size(), equalTo(0));
    }

    @Test
    public void rootNode_shouldNotHaveRemoveLink() {
        service.createWorkflow("workflow");
        tester.startPage(WorkflowEditor.class);
        tester.assertInvisible("treeTable:i:0:middleColumns:links:remove");
    }
}
