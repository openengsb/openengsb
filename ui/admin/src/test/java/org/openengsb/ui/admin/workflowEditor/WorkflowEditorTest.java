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

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openengsb.core.api.DomainService;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.WorkflowConverter;
import org.openengsb.core.api.workflow.WorkflowEditorService;
import org.openengsb.core.api.workflow.model.ActionRepresentation;
import org.openengsb.core.api.workflow.model.EndRepresentation;
import org.openengsb.core.api.workflow.model.EventRepresentation;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.api.workflow.model.WorkflowRepresentation;
import org.openengsb.core.services.internal.WorkflowEditorServiceImpl;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullEvent;
import org.openengsb.ui.admin.model.OpenEngSBVersion;
import org.openengsb.ui.admin.workflowEditor.action.EditAction;
import org.openengsb.ui.admin.workflowEditor.end.SetEnd;

public class WorkflowEditorTest {

    private WicketTester tester;
    private ApplicationContextMock mock;
    private WorkflowEditorService service;
    private RuleManager ruleManager;
    private WorkflowConverter workflowConverter;

    @Before
    public void setup() {
        tester = new WicketTester();
        mock = new ApplicationContextMock();
        service = new WorkflowEditorServiceImpl();
        mock.putBean(mock(ContextCurrentService.class));
        mock.putBean("openengsbVersion", new OpenEngSBVersion());
        mock.putBean("workflowEditorService", service);
        mock.putBean("domainService", mock(DomainService.class));
        ruleManager = mock(RuleManager.class);
        mock.putBean(ruleManager);
        workflowConverter = mock(WorkflowConverter.class);
        mock.putBean(workflowConverter);
        tester.getApplication().addComponentInstantiationListener(
            new SpringComponentInjector(tester.getApplication(), mock, true));
        tester.startPage(new WorkflowEditor());
    }

    @Test
    public void withoutWorkflow_partsShouldBeInvisible() {
        tester.assertInvisible("workflowSelectForm");
        tester.assertInvisible("treeTable");
        tester.assertInvisible("export");
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

    @Test
    public void createAndSetEndNode_ShouldBeShownInEditor() {
        service.createWorkflow("workflow");
        tester.startPage(WorkflowEditor.class);
        String setEnd = "treeTable:i:0:middleColumns:links:set-end";
        tester.clickLink(setEnd);
        tester.assertRenderedPage(SetEnd.class);
        FormTester formTester = tester.newFormTester("endSelectForm");
        formTester.setValue("name", "Name");
        formTester.submit("create");
        ActionRepresentation root = service.getCurrentWorkflow().getRoot();
        EndRepresentation end = root.getEnd();
        assertTrue(root.isLeaf());
        assertThat(end.getName(), equalTo("Name"));
        List<EndRepresentation> endNodes = service.getCurrentWorkflow().getEndNodes();
        assertThat(endNodes.size(), equalTo(1));
        assertThat(endNodes.get(0), sameInstance(end));
        tester.assertRenderedPage(WorkflowEditor.class);
        root.setEnd(null);
        assertNull(root.getEnd());
        tester.clickLink(setEnd);
        formTester = tester.newFormTester("endSelectForm");
        formTester.select("endSelect", 0);
        formTester.submit("select");
        assertThat(root.getEnd().getName(), equalTo("Name"));
        root.setEnd(null);
        tester.assertRenderedPage(WorkflowEditor.class);
        tester.clickLink(setEnd);
        formTester = tester.newFormTester("endSelectForm");
        formTester.submit("cancel");
        assertNull(root.getEnd());
    }

    @Test
    public void exportWorkflow_ShouldCallRuleManagerAddWithConverterReturn() throws RuleBaseException {
        service.createWorkflow("workflow");
        tester.startPage(WorkflowEditor.class);
        String converted = "converted";
        Mockito.when(workflowConverter.convert(service.getCurrentWorkflow())).thenReturn(converted);
        FormTester export = tester.newFormTester("export");

        export.submit();
        ArgumentCaptor<RuleBaseElementId> captor = ArgumentCaptor.forClass(RuleBaseElementId.class);
        Mockito.verify(ruleManager).add(captor.capture(), Mockito.eq(converted));
        RuleBaseElementId value = captor.getValue();
        assertThat(value.getType(), equalTo(RuleBaseElementType.Process));
        assertThat(value.getName(), equalTo("workflow"));
        assertThat(value.getPackageName(), equalTo(RuleBaseElementId.DEFAULT_RULE_PACKAGE));
    }
}
