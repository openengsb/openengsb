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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.model.ConfigItem;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.persistence.InvalidConfigurationException;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.WorkflowConverter;
import org.openengsb.core.api.workflow.WorkflowEditorService;
import org.openengsb.core.api.workflow.WorkflowValidationResult;
import org.openengsb.core.api.workflow.WorkflowValidator;
import org.openengsb.core.api.workflow.model.ActionRepresentation;
import org.openengsb.core.api.workflow.model.EndRepresentation;
import org.openengsb.core.api.workflow.model.EventRepresentation;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.api.workflow.model.WorkflowRepresentation;
import org.openengsb.core.services.internal.WorkflowEditorServiceImpl;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullEvent;
import org.openengsb.ui.admin.AbstractUITest;
import org.openengsb.ui.admin.workflowEditor.action.EditAction;
import org.openengsb.ui.admin.workflowEditor.end.SetEnd;

public class WorkflowEditorTest extends AbstractUITest {

    private WorkflowEditorService service;
    private RuleManager ruleManager;
    private WorkflowConverter workflowConverter;
    private ConfigPersistenceService workflowPersistence;
    private List<WorkflowValidator> validators;

    @Before
    public void setup() throws InvalidConfigurationException, PersistenceException {
        validators = new ArrayList<WorkflowValidator>();
        tester = new WicketTester();
        workflowPersistence = mock(ConfigPersistenceService.class);
        when(workflowPersistence.load(null)).thenReturn(new ArrayList<ConfigItem<?>>());
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.CONFIGURATION_ID, "WORKFLOW");
        registerService(workflowPersistence, props, ConfigPersistenceService.class);
        createWorkflowEditorService();
        ruleManager = mock(RuleManager.class);
        context.putBean(ruleManager);
        context.putBean("validators", validators);
        workflowConverter = mock(WorkflowConverter.class);
        context.putBean(workflowConverter);
        tester.getApplication().addComponentInstantiationListener(
            new SpringComponentInjector(tester.getApplication(), context, true));
        tester.startPage(new WorkflowEditor());
    }

    private void createWorkflowEditorService() throws PersistenceException {
        service = new WorkflowEditorServiceImpl();
        context.putBean("workflowEditorService", service);
    }

    @Test
    public void withoutWorkflow_partsshouldBeInvisible() {
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
    public void selectWorkflow_shouldShowWorkflowActionDescriptions() {
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

        selectWorkflowAndIfLoadedCorrectly(string, 1);

        assertThat(string, equalTo(service.getCurrentWorkflow().getName()));
    }

    private void selectWorkflowAndIfLoadedCorrectly(String string, int item) {
        FormTester formTester = tester.newFormTester("workflowSelectForm");
        formTester.select("workflowSelect", item);
        formTester.submit();

        tester.assertRenderedPage(WorkflowEditor.class);
        tester.assertLabel("currentWorkflowName", string);
    }

    @Test
    public void callCreateWorkflow_shouldCreateWorkflow() {
        FormTester createEmptyWorkflow = tester.newFormTester("workflowCreateForm");
        createEmptyWorkflow.submit();

        tester.assertRenderedPage(WorkflowEditor.class);
        assertThat(service.getWorkflowNames().size(), equalTo(0));
        assertThat(service.getCurrentWorkflow(), equalTo(null));
        tester.assertLabel("currentWorkflowName", "Please create Workflow first");

        createWorkflow();

        assertThat("Name", equalTo(service.getCurrentWorkflow().getName()));
        tester.assertRenderedPage(EditAction.class);
    }

    @Test
    public void removeAction_shouldRemoveActionFromWorkflow() {
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
    public void removeEvent_shouldRemoveEventFromWorkflow() {
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
    public void createAndSetEndNode_shouldBeShownInEditor() {
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
    public void exportWorkflow_shouldCallRuleManagerAddWithConverterReturnAndAddGlobal() throws RuleBaseException {
        WorkflowValidator validator = mock(WorkflowValidator.class);
        when(validator.validate(Mockito.any(WorkflowRepresentation.class))).thenReturn(
            new WorkflowValidationResultImplementation(true, new String[0]));
        validators.add(validator);
        service.createWorkflow("workflow");
        ActionRepresentation root = service.getCurrentWorkflow().getRoot();
        root.setDomain(NullDomain.class);
        root.setLocation("location");
        tester.startPage(WorkflowEditor.class);
        String converted = "converted";
        Mockito.when(workflowConverter.convert(service.getCurrentWorkflow())).thenReturn(converted);
        exportWorkflow();
        ArgumentCaptor<RuleBaseElementId> captor = ArgumentCaptor.forClass(RuleBaseElementId.class);
        Mockito.verify(ruleManager).add(captor.capture(), Mockito.eq(converted));
        RuleBaseElementId value = captor.getValue();
        assertThat(value.getType(), equalTo(RuleBaseElementType.Process));
        assertThat(value.getName(), equalTo("workflow"));
        assertThat(value.getPackageName(), equalTo(RuleBaseElementId.DEFAULT_RULE_PACKAGE));
        verify(validator).validate(service.getCurrentWorkflow());
    }

    public void exportWorkflow() {
        FormTester export = tester.newFormTester("export");
        export.submit();
    }

    @Test
    public void testSave_shouldCallPersistenceMethod() throws InvalidConfigurationException, PersistenceException {
        tester.assertInvisible("saveForm");
        service.createWorkflow("Workflow");
        tester.startPage(WorkflowEditor.class);
        tester.assertVisible("saveForm");
        ArgumentCaptor<ConfigItem> captor = ArgumentCaptor.forClass(ConfigItem.class);

        FormTester saveFormTester = tester.newFormTester("saveForm");
        saveFormTester.submit();

        verify(workflowPersistence).persist(captor.capture());
        assertThat((WorkflowRepresentation) captor.getValue().getContent(), sameInstance(service.getCurrentWorkflow()));
    }

    @Test
    public void testLoad_shouldHaveLoadedWorkflows() throws InvalidConfigurationException, PersistenceException {
        List<ConfigItem<?>> items = new ArrayList<ConfigItem<?>>();
        WorkflowRepresentation rep = new WorkflowRepresentation();
        final String string = "Name";
        rep.setName(string);
        items.add(new ConfigItem<WorkflowRepresentation>(null, rep));
        when(workflowPersistence.load(Mockito.anyMap())).thenReturn(items);

        service.loadWorkflowsFromDatabase();

        tester.startPage(WorkflowEditor.class);
        tester.assertVisible("workflowSelectForm");
        selectWorkflowAndIfLoadedCorrectly("Name", 0);
    }

    @Test
    public void testExport_shouldExportWhenValidationWorks() {
        WorkflowValidator validator = mock(WorkflowValidator.class);
        final String[] errors = new String[]{ "Error1", "Error2" };
        when(validator.validate(Mockito.any(WorkflowRepresentation.class))).thenReturn(
            new WorkflowValidationResultImplementation(false, errors));
        validators.add(validator);
        service.createWorkflow("TestWorkflow");
        tester.startPage(WorkflowEditor.class);

        exportWorkflow();

        verify(ruleManager, never()).add(Mockito.any(RuleBaseElementId.class), Mockito.anyString());
        verify(workflowConverter, never()).convert(service.getCurrentWorkflow());
        tester.assertErrorMessages(errors);
    }

    private void createWorkflow() {
        FormTester createForm = tester.newFormTester("workflowCreateForm");
        createForm.setValue("name", "Name");
        createForm.submit();
    }

    private static final class WorkflowValidationResultImplementation implements WorkflowValidationResult {
        private final String[] errors;
        private final boolean result;

        private WorkflowValidationResultImplementation(boolean result, String[] errors) {
            this.result = result;
            this.errors = errors;
        }

        @Override
        public boolean isValid() {
            return result;
        }

        @Override
        public List<String> getErrors() {
            return Arrays.asList(errors);
        }
    }
}
