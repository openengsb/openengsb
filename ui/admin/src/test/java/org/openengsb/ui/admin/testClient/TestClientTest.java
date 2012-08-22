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

package org.openengsb.ui.admin.testClient;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.l10n.PassThroughLocalizableString;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.ui.admin.AbstractUITest;
import org.openengsb.ui.admin.connectorEditorPage.ConnectorEditorPage;
import org.openengsb.ui.admin.index.Index;
import org.openengsb.ui.admin.model.MethodCall;
import org.openengsb.ui.admin.model.MethodId;
import org.openengsb.ui.admin.model.ServiceId;
import org.openengsb.ui.admin.util.MethodComparator;
import org.openengsb.ui.common.OpenEngSBPage;
import org.openengsb.ui.common.editor.BeanEditorPanel;
import org.openengsb.ui.common.editor.fields.DropdownField;
import org.openengsb.ui.common.editor.fields.InputField;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;
import org.osgi.framework.ServiceReference;

public class TestClientTest extends AbstractUITest {

    public interface AnotherTestInterface extends Connector {

    }

    public class TestService implements AnotherTestInterface {

        private String instanceId;

        public TestService(String id) {
            instanceId = id;
        }

        @Override
        public AliveState getAliveState() {
            return AliveState.OFFLINE;
        }

        @Override
        public String getInstanceId() {
            return instanceId;
        }

        @Override
        public void setDomainId(String domainId) {
        }

        @Override
        public String getDomainId() {
            return null;
        }

        @Override
        public void setConnectorId(String connectorId) {
        }

        @Override
        public String getConnectorId() {
            return null;
        }

    }

    public enum UpdateEnum {
        ONE, TWO
    }

    private TestInterface testService;
    private FormTester formTester;
    private boolean serviceListExpanded = true;

    @Test
    public void testLinkAppearsWithCaptionTestClient_shouldShowTestClient() throws Exception {
        setupIndexPage();
        tester.startPage(Index.class);
        tester.assertContains("Test Client");
    }

    @Test
    public void testParameterFormIsCreated_shouldShowMethodCallForm() throws Exception {
        setupAndStartTestClientPage();
        tester.assertComponent("methodCallForm", Form.class);
    }

    @Test
    public void testServiceTreeIsCreated_shouldShowServiceList() throws Exception {
        setupAndStartTestClientPage();
        tester.assertComponent("methodCallForm:serviceList", LinkTree.class);
    }

    @Test
    public void testShowServiceInstancesInDropdown_shouldShowServices() throws Exception {
        setupAndStartTestClientPage();
        if (!serviceListExpanded) {
            expandServiceListTree();
        }
        tester.assertComponent("methodCallForm:serviceList:i:5:nodeComponent:contentLink", AjaxLink.class);
        Label serviceLabel =
            (Label) tester
                .getComponentFromLastRenderedPage("methodCallForm:serviceList:i:5:nodeComponent:contentLink:content");
        assertThat(serviceLabel.getDefaultModelObjectAsString(), containsString("test-service"));
    }

    @Test
    public void testCreateMethodListDropDown_shouldShowMethodList() throws Exception {
        setupAndStartTestClientPage();
        tester.assertComponent("methodCallForm:methodList", DropDownChoice.class);
    }

    @Test
    public void testServiceManagerList_shouldWork() throws Exception {
        setupAndStartTestClientPage();
        tester.assertComponent("serviceManagementContainer:domains:1:services:0:create.new", Link.class);
        ListView<?> component = (ListView<?>)
            tester.getComponentFromLastRenderedPage("serviceManagementContainer:domains:0:services");
        assertThat("should only display service-factories for the current domain", component.size(), is(0));
    }

    @Test
    public void testServiceListSelect_shouldSelectService() throws Exception {
        setupAndStartTestClientPage();
        setServiceInDropDown("testdomain+testconnector+test-service");

        @SuppressWarnings("unchecked")
        Form<MethodCall> form = (Form<MethodCall>) tester.getComponentFromLastRenderedPage("methodCallForm");
        MethodCall modelObject = form.getModelObject();
        ServiceId reference = new ServiceId(TestInterface.class, "testdomain+testconnector+test-service");
        Assert.assertEquals(reference.toString(), modelObject.getService().toString());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testJumpToService_shouldJumpToService() throws Exception {
        setupTestClientPage();
        String connectorId = "testdomain+testconnector+test-service";
        ServiceId reference = new ServiceId(TestInterface.class, connectorId);
        tester.startPage(new TestClient(reference));
        tester.assertComponent("methodCallForm:serviceList:i:2:nodeComponent:contentLink:content", Label.class);
        Form<MethodCall> form = (Form<MethodCall>) tester.getComponentFromLastRenderedPage("methodCallForm");
        assertThat(form.getModelObject().getService(), is(reference));
        DropDownChoice<MethodId> ddc = (DropDownChoice<MethodId>) form.get("methodList");
        assertThat(ddc.getChoices().isEmpty(), is(false));
    }

    private void expandServiceListTree() {
        tester.clickLink("methodCallForm:serviceList:i:0:junctionLink", true);
        tester.clickLink("methodCallForm:serviceList:i:1:junctionLink", true);
        serviceListExpanded = true;
    }

    @Test
    public void testShowMethodListInDropDown_shouldShowMethodList() throws Exception {
        setupAndStartTestClientPage();
        @SuppressWarnings("unchecked")
        DropDownChoice<MethodId> methodList =
            (DropDownChoice<MethodId>) tester.getComponentFromLastRenderedPage("methodCallForm:methodList");

        setServiceInDropDown("testdomain+testconnector+test-service");

        List<? extends MethodId> choices = methodList.getChoices();
        List<Method> choiceMethods = new ArrayList<Method>();
        for (MethodId mid : choices) {
            choiceMethods.add(TestInterface.class.getMethod(mid.getName(), mid.getArgumentTypes()));
        }
        List<Method> list = Arrays.asList(TestInterface.class.getMethods());
        Collections.sort(list, new MethodComparator());
        Assert.assertEquals(list, choiceMethods);
    }

    @Test
    public void testCreateArgumentList_shouldCreateArgumentList() throws Exception {
        setupAndStartTestClientPage();

        Component argList =
            tester.getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");

        Assert.assertNotNull(argList);
    }

    @Test
    public void testCreateTextFieldsFor2StringArguments_shouldShowTextFields() throws Exception {
        setupAndStartTestClientPage();
        RepeatingView argList =
            (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");

        setServiceInDropDown("testdomain+testconnector+test-service");
        setMethodInDropDown("update", String.class, String.class);
        Assert.assertEquals(2, argList.size());
        Iterator<? extends Component> iterator = argList.iterator();
        while (iterator.hasNext()) {
            Component next = iterator.next();
            tester.assertComponent(next.getPageRelativePath() + ":valueEditor", InputField.class);
        }
    }

    @Test
    public void testCreateDropdownForOptionArguments_shouldShowArgumentDropdownField() throws Exception {
        setupAndStartTestClientPage();
        RepeatingView argList =
            (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");

        setServiceInDropDown("testdomain+testconnector+test-service");
        setMethodInDropDown("update", UpdateEnum.class);

        Assert.assertEquals(1, argList.size());
        tester.assertComponent("methodCallForm:argumentListContainer:argumentList:arg0panel:valueEditor",
            DropdownField.class);
    }

    @Test
    public void testCreateTextFieldsForBean_shouldShowBeanForm() throws Exception {
        setupAndStartTestClientPage();
        RepeatingView argList =
            (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");

        setServiceInDropDown("testdomain+testconnector+test-service");
        setMethodInDropDown("update", TestBean.class);

        Assert.assertEquals(1, argList.size());
        Assert.assertEquals(BeanEditorPanel.class, argList.get("arg0panel:valueEditor").getClass());
        RepeatingView panel = (RepeatingView) argList.get("arg0panel:valueEditor:fields");
        Assert.assertEquals(2, panel.size());
    }

    @Test
    public void testPerformMethodCall_shouldCallMethod() throws Exception {
        setupAndStartTestClientPage();
        RepeatingView argList =
            (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");

        setServiceInDropDown("testdomain+testconnector+test-service");
        setMethodInDropDown("update", String.class, String.class);
        for (int i = 0; i < argList.size(); i++) {
            formTester.setValue("argumentListContainer:argumentList:arg" + i + "panel:valueEditor:field", "test");
        }

        tester.executeAjaxEvent("methodCallForm:submitButton", "onclick");
        verify(testService).update("test", "test");
    }

    @Test
    public void testPerformMethodCallOnDomain_shouldCallMethod() throws Exception {
        setupAndStartTestClientPage();
        RepeatingView argList =
            (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");

        setServiceInDropDown("testdomain+testconnector+test-service");
        setMethodInDropDown("update", String.class, String.class);
        for (int i = 0; i < argList.size(); i++) {
            formTester.setValue("argumentListContainer:argumentList:arg" + i + "panel:valueEditor:field", "test");
        }

        tester.executeAjaxEvent("methodCallForm:submitButton", "onclick");
        verify(testService).update("test", "test");
    }

    @Test
    public void testPerformMethodCallWithBeanArgument_shouldCallMethod() throws Exception {
        setupAndStartTestClientPage();

        setServiceInDropDown("testdomain+testconnector+test-service");
        setMethodInDropDown("update", TestBean.class);

        String beanPanelPath = "argumentListContainer:argumentList:arg0panel:valueEditor";
        BeanEditorPanel beanPanel =
            (BeanEditorPanel) tester.getComponentFromLastRenderedPage("methodCallForm:" + beanPanelPath);
        String idFieldId = beanPanel.getFieldViewId("id");
        String nameFieldId = beanPanel.getFieldViewId("name");
        formTester.setValue(beanPanelPath + ":fields:" + idFieldId + ":row:field", "42");
        formTester.setValue(beanPanelPath + ":fields:" + nameFieldId + ":row:field", "test");

        tester.executeAjaxEvent("methodCallForm:submitButton", "onclick");
        verify(testService).update(new TestBean("42", "test"));
    }

    @Test
    public void testPerformMethodCallWithIntegerObjectArgument_shouldCallMethod() throws Exception {
        setupAndStartTestClientPage();

        setServiceInDropDown("testdomain+testconnector+test-service");
        setMethodInDropDown("update", Integer.class);

        String beanPanelPath = "argumentListContainer:argumentList:arg0panel:valueEditor";
        formTester.setValue(beanPanelPath + ":field", "42");

        tester.executeAjaxEvent("methodCallForm:submitButton", "onclick");
        verify(testService).update(new Integer(42));
    }

    @Test
    public void testSelectMethod_shouldHaveTwoArguments() throws Exception {
        setupAndStartTestClientPage();
        RepeatingView argList =
            (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");

        setServiceInDropDown("testdomain+testconnector+test-service");
        setMethodInDropDown("update", String.class, String.class);

        Assert.assertEquals(2, argList.size());
    }

    @Test
    public void testFormResetAfterCall_shouldResetForm() throws Exception {
        setupAndStartTestClientPage();

        setServiceInDropDown("testdomain+testconnector+test-service");
        setMethodInDropDown("update", String.class, String.class);

        formTester.setValue("argumentListContainer:argumentList:arg0panel:valueEditor:field", "test");
        formTester.setValue("argumentListContainer:argumentList:arg1panel:valueEditor:field", "test");
        tester.executeAjaxEvent("methodCallForm:submitButton", "onclick");

        RepeatingView argList =
            (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");
        Assert.assertEquals(0, argList.size());
    }

    @Test
    public void testFeedbackPanelIsPresent_shouldShowFeedbackPanel() throws Exception {
        setupAndStartTestClientPage();
        tester.assertComponent("feedback", FeedbackPanel.class);
    }

    @Test
    public void testFeedbackPanelContainsText_shouldShowFeedbackPanel() throws Exception {
        setupAndStartTestClientPage();

        setServiceInDropDown("testdomain+testconnector+test-service");
        setMethodInDropDown("update", String.class, String.class);
        formTester.setValue("argumentListContainer:argumentList:arg0panel:valueEditor:field", "test");
        formTester.setValue("argumentListContainer:argumentList:arg1panel:valueEditor:field", "test");
        tester.executeAjaxEvent("methodCallForm:submitButton", "onclick");

        FeedbackPanel feedbackPanel = (FeedbackPanel) tester.getComponentFromLastRenderedPage("feedback");
        tester.assertInfoMessages(new String[]{ "Methodcall called successfully" });
        Label message = (Label) feedbackPanel.get("feedbackul:messages:0:message");
        Assert.assertEquals("Methodcall called successfully", message.getDefaultModelObjectAsString());
    }

    @Test
    public void testExceptionInFeedback_shouldShowFeedbackPanel() throws Exception {
        setupAndStartTestClientPage();

        setServiceInDropDown("testdomain+testconnector+test-service");
        setMethodInDropDown("update", String.class, String.class);
        formTester.setValue("argumentListContainer:argumentList:arg0panel:valueEditor:field", "fail");
        formTester.setValue("argumentListContainer:argumentList:arg1panel:valueEditor:field", "test");
        tester.executeAjaxEvent("methodCallForm:submitButton", "onclick");
        assertTrue(tester.getMessages(FeedbackMessage.ERROR).get(0).toString().contains("IllegalArgumentException"));
    }

    @Test
    public void testSelectOtherService_shouldClearArgumentList() throws Exception {
        setupAndStartTestClientPage();
        setServiceInDropDown("testdomain+testconnector+test-service");
        setMethodInDropDown("update", String.class, String.class);
        setServiceInDropDown("testdomain+testconnector+test-service");
        RepeatingView argList =
            (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");
        assertThat(argList.size(), is(0));
    }

    private void setupTesterWithSpringMockContext() {
        tester.getApplication().getComponentInstantiationListeners()
            .add(new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context));
    }

    @Test
    public void testListToCreateNewServices_shouldShowNewServiceForm() throws Exception {
        setupAndStartTestClientPage();
        tester.assertRenderedPage(TestClient.class);
        Label domainName =
            (Label) tester.getComponentFromLastRenderedPage("serviceManagementContainer:domains:1:domain.name");
        Label domainDescription =
            (Label) tester
                .getComponentFromLastRenderedPage("serviceManagementContainer:domains:1:domain.description");
        Label domainClass =
            (Label) tester.getComponentFromLastRenderedPage("serviceManagementContainer:domains:1:domain.class");
        Label name =
            (Label) tester
                .getComponentFromLastRenderedPage(
                "serviceManagementContainer:domains:1:services:0:service.name");
        Label description =
            (Label) tester.getComponentFromLastRenderedPage("serviceManagementContainer:domains:"
                    + "1:services:0:service.description");
        assertThat(domainName.getDefaultModel().getObject().toString(), equalTo("testdomain"));
        assertThat(domainDescription.getDefaultModel().getObject().toString(), equalTo("testdomain"));
        assertThat(domainClass.getDefaultModel().getObject().toString(), equalTo(TestInterface.class.getName()));
        Assert.assertEquals("service.name", name.getDefaultModel().getObject());
        Assert.assertEquals("service.description", description.getDefaultModel().getObject());
    }

    @Test
    public void testShowEditLink_shouldShowEditLink() throws Exception {
        List<ServiceReference> expected = setupAndStartTestClientPage();
        if (!serviceListExpanded) {
            expandServiceListTree();
        }
        for (int index = 2; index < expected.size() + 2; index++) {
            tester.assertComponent("methodCallForm:serviceList:i:" + index + ":nodeComponent:contentLink",
                AjaxLink.class);
        }
        tester.assertComponent("methodCallForm:editButton", AjaxButton.class);
        AjaxButton editButton = (AjaxButton) tester.getComponentFromLastRenderedPage("methodCallForm:editButton");
        // should be disabled when nothing is selected
        Assert.assertEquals(false, editButton.isEnabled());
    }

    @Test
    public void testShowDeleteLink_shouldShowDeleteLink() throws Exception {
        List<ServiceReference> expected = setupAndStartTestClientPage();
        if (!serviceListExpanded) {
            expandServiceListTree();
        }
        for (int index = 2; index < expected.size() + 2; index++) {
            tester.assertComponent("methodCallForm:serviceList:i:" + index + ":nodeComponent:contentLink",
                AjaxLink.class);
        }
        tester.assertComponent("methodCallForm:deleteButton", AjaxButton.class);
        AjaxButton deleteButton = (AjaxButton) tester.getComponentFromLastRenderedPage("methodCallForm:deleteButton");
        // should be disabled when nothing is selected
        Assert.assertEquals(false, deleteButton.isEnabled());
    }

    @Test
    public void testTargetLocationOfEditButton_shouldRedirectToEditorPage() throws Exception {
        setupAndStartTestClientPage();
        ServiceReference ref = Mockito.mock(ServiceReference.class);
        Mockito.when(ref.getProperty("managerId")).thenReturn("ManagerId");
        Mockito.when(ref.getProperty("domain")).thenReturn(TestInterface.class.getName());
        when(
            bundleContext.getServiceReferences(Domain.class.getName(),
                String.format("(%s=%s)", org.osgi.framework.Constants.SERVICE_PID, "test")))
            .thenReturn(new ServiceReference[]{ ref });

        ServiceDescriptor serviceDescriptor = Mockito.mock(ServiceDescriptor.class);
        Mockito.when(serviceDescriptor.getId()).thenReturn("ManagerId");
        Mockito.when(serviceDescriptor.getName()).thenReturn(new PassThroughLocalizableString("ServiceName"));
        Mockito.when(serviceDescriptor.getDescription()).thenReturn(
            new PassThroughLocalizableString("ServiceDescription"));

        if (!serviceListExpanded) {
            expandServiceListTree();
        }
        tester.clickLink("methodCallForm:serviceList:i:5:nodeComponent:contentLink", true);
        AjaxButton editButton = (AjaxButton) tester.getComponentFromLastRenderedPage("methodCallForm:editButton");
        Assert.assertEquals(true, editButton.isEnabled());
        tester.executeAjaxEvent(editButton, "onclick");

        tester.assertRenderedPage(ConnectorEditorPage.class);
    }

    @Test
    public void testFunctionDeleteButton_shouldDeleteService() throws Exception {
        setupAndStartTestClientPage();
        ServiceReference ref = Mockito.mock(ServiceReference.class);
        Mockito.when(ref.getProperty("managerId")).thenReturn("ManagerId");
        Mockito.when(ref.getProperty("domain")).thenReturn(TestInterface.class.getName());
        when(bundleContext.getServiceReferences(Domain.class.getName(), String.format("(id=%s)", "test"))).thenReturn(
            new ServiceReference[]{ ref });

        ServiceDescriptor serviceDescriptor = Mockito.mock(ServiceDescriptor.class);
        Mockito.when(serviceDescriptor.getId()).thenReturn("ManagerId");
        Mockito.when(serviceDescriptor.getName()).thenReturn(new PassThroughLocalizableString("ServiceName"));
        Mockito.when(serviceDescriptor.getDescription()).thenReturn(
            new PassThroughLocalizableString("ServiceDescription"));

        if (!serviceListExpanded) {
            expandServiceListTree();
        }
        tester.clickLink("methodCallForm:serviceList:i:5:nodeComponent:contentLink", true);
        AjaxButton deleteButton = (AjaxButton) tester.getComponentFromLastRenderedPage("methodCallForm:deleteButton");
        Assert.assertEquals(true, deleteButton.isEnabled());
        tester.executeAjaxEvent(deleteButton, "onclick");

        try {
            tester.clickLink("methodCallForm:serviceList:i:5:nodeComponent:contentLink", true);
        } catch (AssertionFailedError e) {
            return;
        }
        fail("Deletion wasn't successful.");
    }

    @Test
    public void testStartWithContextAsParam_shouldHaveCorrectContext() throws Exception {
        setupTestClientPage();
        ContextHolder.get().setCurrentContextId("foo2");
        PageParameters pageParameters = new PageParameters();
        pageParameters.set(OpenEngSBPage.CONTEXT_PARAM, new String[]{ "foo" });
        tester.startPage(TestClient.class, pageParameters);
        assertThat(ContextHolder.get().getCurrentContextId(), is("foo"));
    }

    @Test
    public void testForEachDomainVisibleInCreatePartIsAnEntryInTree_shouldWork() throws Exception {
        setupAndStartTestClientPage();
        tester.assertRenderedPage(TestClient.class);
        List<String> domains = new ArrayList<String>();
        List<String> availableInTree = new ArrayList<String>();
        List<DefaultMutableTreeNode> availableInTreeAsTreeNode = new ArrayList<DefaultMutableTreeNode>();

        Component domainsComponent = tester.getComponentFromLastRenderedPage("serviceManagementContainer:domains");
        int count = ((ArrayList<?>) domainsComponent.getDefaultModelObject()).size();

        // get all domains
        for (int i = 0; i < count; i++) {
            Component label = tester
                .getComponentFromLastRenderedPage("serviceManagementContainer:domains:" + i + ":domain.name");
            domains.add(label.getDefaultModelObjectAsString());
        }

        // get all services from the tree
        DefaultTreeModel serviceListTree = (DefaultTreeModel) tester
            .getComponentFromLastRenderedPage("methodCallForm:serviceList").getDefaultModelObject();
        count = serviceListTree.getChildCount(serviceListTree.getRoot());
        for (int i = 0; i < count; i++) {
            DefaultMutableTreeNode child =
                (DefaultMutableTreeNode) serviceListTree.getChild(serviceListTree.getRoot(), i);
            String userObject = (String) child.getUserObject();
            availableInTreeAsTreeNode.add(child);
            availableInTree.add(userObject);
        }

        for (int i = 0; i < domains.size(); i++) {
            String domain = domains.get(i);
            assertThat(availableInTree.contains(domain), is(true));
            assertThat(serviceListTree.getChildCount(availableInTreeAsTreeNode.get(i)), greaterThan(0));
        }
    }

    @Test
    public void testToSelectDefaultEndPoint_shouldDisplayDomainMethodWithArguments() throws Exception {
        setupAndStartTestClientPage();
        tester.assertRenderedPage(TestClient.class);

        setServiceInDropDown("testdomain+testconnector+test-service");
        setMethodInDropDown("update", String.class, String.class);
        RepeatingView argList =
            (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");
        Assert.assertEquals(2, argList.size());

    }

    private void setServiceInDropDown(String serviceName) {
        if (!serviceListExpanded) {
            expandServiceListTree();
        }
        tester.debugComponentTrees();
        WebMarkupContainer component =
            (WebMarkupContainer) tester.getComponentFromLastRenderedPage("methodCallForm:serviceList:i");
        for (int i = 0; i < component.size(); i++) {
            WebMarkupContainer component2 = (WebMarkupContainer) component.get(i);
            if (component2.getDefaultModelObjectAsString().startsWith(serviceName)) {
                tester.clickLink("methodCallForm:serviceList:i:" + i + ":nodeComponent:contentLink", true);
                tester.executeAjaxEvent("methodCallForm:serviceList:i:" + i + ":nodeComponent:contentLink",
                    "onclick");
                return;
            }
        }
        throw new IllegalArgumentException("Service with name " + serviceName + " not found in tree");
    }

    private void setMethodInDropDown(String name, Class<?>... parameterTypes) {
        @SuppressWarnings("unchecked")
        List<? extends MethodId> choices =
            ((DropDownChoice<MethodId>) tester.getComponentFromLastRenderedPage("methodCallForm:methodList"))
                .getChoices();
        for (int i = 0; i < choices.size(); i++) {
            MethodId methodId = choices.get(i);
            if (methodId.getName().equals(name)
                    && ArrayUtils.isEquals(methodId.getArgumentTypes(), parameterTypes)) {
                formTester.select("methodList", i);
                tester.executeAjaxEvent("methodCallForm:methodList", "onchange");
                return;
            }
        }
        throw new IllegalArgumentException(String.format("could not find method %s(%s) in dropdown (%s)", name,
            StringUtils.join(parameterTypes, ", "), choices));
    }

    private List<ServiceReference> setupAndStartTestClientPage() throws Exception {
        setupTestClientPage();
        final List<ServiceReference> expected = Arrays.asList();
        tester.startPage(TestClient.class);
        formTester = tester.newFormTester("methodCallForm");
        return expected;
    }

    private void setupTestClientPage() throws Exception {
        createProviderMocks();

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("value", "42");
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put("location.root", "domain/testdomain/default");
        serviceManager.createWithId("testdomain+testconnector+test-service",
            new ConnectorDescription("testdomain", "testconnector",
                attributes, properties));

        ServiceDescriptor serviceDescriptorMock = Mockito.mock(ServiceDescriptor.class);
        Mockito.when(serviceDescriptorMock.getName()).thenReturn(new PassThroughLocalizableString("service.name"));
        Mockito.when(serviceDescriptorMock.getDescription()).thenReturn(
            new PassThroughLocalizableString("service.description"));

        doThrow(new IllegalArgumentException()).when(testService).update(eq("fail"), anyString());
        setupTesterWithSpringMockContext();
    }

    private void createProviderMocks() {
        createDomainProviderMock(TestInterface.class, "testdomain");
        createDomainProviderMock(AnotherTestInterface.class, "anotherTestDomain");
        createConnectorProviderMock("testconnector", "testdomain");
        ConnectorInstanceFactory factory = mock(ConnectorInstanceFactory.class);
        when(factory.createNewInstance(anyString())).thenAnswer(new Answer<Connector>() {
            @Override
            public Connector answer(InvocationOnMock invocation) throws Throwable {
                TestInterface newMock = mock(TestInterface.class);
                testService = newMock;
                when(newMock.getInstanceId()).thenReturn((String) invocation.getArguments()[0]);
                return newMock;
            }
        });
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.DOMAIN_KEY, "testdomain");
        props.put(Constants.CONNECTOR_KEY, "testconnector");
        registerService(factory, props, ConnectorInstanceFactory.class);
    }

    private void setupIndexPage() {
        setupTesterWithSpringMockContext();
    }
}
