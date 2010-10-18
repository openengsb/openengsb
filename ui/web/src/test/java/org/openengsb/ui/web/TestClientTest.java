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

package org.openengsb.ui.web;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import junit.framework.Assert;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.l10n.LocalizableString;
import org.openengsb.core.common.l10n.PassThroughLocalizableString;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.core.common.util.AliveState;
import org.openengsb.ui.web.editor.BeanArgumentPanel;
import org.openengsb.ui.web.editor.SimpleArgumentPanel;
import org.openengsb.ui.web.model.MethodCall;
import org.openengsb.ui.web.model.MethodId;
import org.openengsb.ui.web.model.ServiceId;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class TestClientTest {
    private DomainService managedServicesMock;

    public interface TestInterface extends Domain {

        void update(String id, String name);

        void update(TestBean test);

    }

    public class TestService implements TestInterface {

        private boolean called = false;
        private TestBean test;

        @Override
        public void update(String id, String name) {
            if ("fail".equals(id)) {
                throw new IllegalArgumentException();
            }
            called = true;
        }

        @Override
        public void update(TestBean test) {
            this.test = test;
        }

        public String getName(String id) {
            return "";
        }

        @Override
        public AliveState getAliveState() {
            return AliveState.OFFLINE;
        }

    }

    private WicketTester tester;

    private ApplicationContextMock context;
    private TestService testService;
    private FormTester formTester;
    private boolean serviceListExpanded = true;
    private BundleContext bundleContext;

    @Before
    public void setup() {
        tester = new WicketTester();
        context = new ApplicationContextMock();
        context.putBean(mock(ContextCurrentService.class));
        bundleContext = mock(BundleContext.class);
        context.putBean(bundleContext);
    }

    @Test
    public void testLinkAppearsWithCaptionTestClient() throws Exception {
        setupIndexPage();

        tester.startPage(Index.class);

        tester.assertContains("Test Client");
    }

    @Test
    public void testParameterFormIsCreated() throws Exception {
        setupAndStartTestClientPage();

        tester.assertComponent("methodCallForm", Form.class);
    }

    @Test
    public void testServiceTreeIsCreated() throws Exception {
        setupAndStartTestClientPage();

        tester.assertComponent("methodCallForm:serviceList", LinkTree.class);
    }

    @Test
    public void testShowServiceInstancesInDropdown() throws Exception {
        List<ServiceReference> expected = setupAndStartTestClientPage();

        if (!serviceListExpanded) {
            expandServiceListTree();
        }
        for (int index = 2; index < expected.size() + 2; index++) {
            tester.assertComponent("methodCallForm:serviceList:i:" + index + ":nodeComponent:contentLink",
                AjaxLink.class);
        }
    }

    @Test
    public void testCreateMethodListDropDown() throws Exception {
        setupAndStartTestClientPage();

        tester.assertComponent("methodCallForm:methodList", DropDownChoice.class);
    }

    @Test
    public void testServiceListSelect() throws Exception {
        setupAndStartTestClientPage();
        setServiceInDropDown(0);

        @SuppressWarnings("unchecked")
        Form<MethodCall> form = (Form<MethodCall>) tester.getComponentFromLastRenderedPage("methodCallForm");
        MethodCall modelObject = form.getModelObject();
        ServiceId reference = new ServiceId(TestService.class.getName(), "test");

        Assert.assertEquals(reference.toString(), modelObject.getService().toString());
    }

    @Test
    public void testJumpToService() throws Exception {
        setupTestClientPage();
        ServiceId reference = new ServiceId(TestService.class.getName(), "test");
        tester.startPage(new TestClient(reference));
        tester.assertComponent("methodCallForm:serviceList:i:3:nodeComponent:contentLink:content", Label.class);
    }

    private void expandServiceListTree() {
        tester.clickLink("methodCallForm:serviceList:i:0:junctionLink", true);
        tester.clickLink("methodCallForm:serviceList:i:1:junctionLink", true);
        serviceListExpanded = true;
    }

    @Test
    public void testShowMethodListInDropDown() throws Exception {
        setupAndStartTestClientPage();
        @SuppressWarnings("unchecked")
        DropDownChoice<MethodId> methodList =
            (DropDownChoice<MethodId>) tester.getComponentFromLastRenderedPage("methodCallForm:methodList");

        setServiceInDropDown(0);

        List<? extends MethodId> choices = methodList.getChoices();
        List<Method> choiceMethods = new ArrayList<Method>();
        for (MethodId mid : choices) {
            choiceMethods.add(TestInterface.class.getMethod(mid.getName(), mid.getArgumentTypesAsClasses()));
        }
        Assert.assertEquals(Arrays.asList(TestInterface.class.getDeclaredMethods()), choiceMethods);
    }

    @Test
    public void testCreateArgumentList() throws Exception {
        setupAndStartTestClientPage();

        Component argList =
            tester.getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");

        Assert.assertNotNull(argList);
    }

    @Test
    public void testCreateTextFieldsFor2StringArguments() throws Exception {
        setupAndStartTestClientPage();
        RepeatingView argList =
            (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");

        setServiceInDropDown(0);
        setMethodInDropDown(0);

        Assert.assertEquals(2, argList.size());
        Iterator<? extends Component> iterator = argList.iterator();
        while (iterator.hasNext()) {
            Assert.assertEquals(SimpleArgumentPanel.class, iterator.next().getClass());
        }
    }

    private void setMethodInDropDown(int index) {
        formTester.select("methodList", index);
        tester.executeAjaxEvent("methodCallForm:methodList", "onchange");
    }

    @Test
    public void testCreateTextFieldsForBean() throws Exception {
        setupAndStartTestClientPage();
        RepeatingView argList =
            (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");

        setServiceInDropDown(0);
        setMethodInDropDown(1);

        Assert.assertEquals(1, argList.size());
        Assert.assertEquals(BeanArgumentPanel.class, argList.get("arg0").getClass());

        RepeatingView panel = (RepeatingView) argList.get("arg0:fields");
        Assert.assertEquals(2, panel.size());
    }

    @Test
    public void testPerformMethodCall() throws Exception {
        setupAndStartTestClientPage();
        RepeatingView argList =
            (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");

        setServiceInDropDown(0);
        setMethodInDropDown(0);

        for (int i = 0; i < argList.size(); i++) {
            formTester.setValue("argumentListContainer:argumentList:arg" + i + ":value", "test");
        }

        tester.executeAjaxEvent("methodCallForm:submitButton", "onclick");

        Assert.assertTrue(testService.called);
    }

    @Test
    public void testPerformMethodCallWithBeanArgument() throws Exception {
        setupAndStartTestClientPage();

        setServiceInDropDown(0);
        setMethodInDropDown(1);

        formTester.setValue("argumentListContainer:argumentList:arg0:fields:id:row:field", "42");
        formTester.setValue("argumentListContainer:argumentList:arg0:fields:name:row:field", "test");

        tester.executeAjaxEvent("methodCallForm:submitButton", "onclick");

        Assert.assertNotNull(testService.test);
    }

    private void setServiceInDropDown(int index) {
        if (!serviceListExpanded) {
            expandServiceListTree();
        }
        tester.clickLink("methodCallForm:serviceList:i:" + (index + 2) + ":nodeComponent:contentLink", true);
        tester
            .executeAjaxEvent("methodCallForm:serviceList:i:" + (index + 2) + ":nodeComponent:contentLink", "onclick");
    }

    @Test
    public void testSelectMethodTwice() throws Exception {
        setupAndStartTestClientPage();
        RepeatingView argList =
            (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");

        setServiceInDropDown(0);
        setMethodInDropDown(0);
        tester.executeAjaxEvent("methodCallForm:methodList", "onchange");

        Assert.assertEquals(2, argList.size());
    }

    @Test
    public void testFormResetAfterCall() throws Exception {
        setupAndStartTestClientPage();

        setServiceInDropDown(0);
        setMethodInDropDown(0);

        formTester.setValue("argumentListContainer:argumentList:arg0:value", "test");
        formTester.setValue("argumentListContainer:argumentList:arg1:value", "test");
        tester.executeAjaxEvent("methodCallForm:submitButton", "onclick");

        RepeatingView argList =
            (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");
        Assert.assertEquals(0, argList.size());
    }

    @Test
    public void testFeedbackPanelIsPresent() throws Exception {
        setupAndStartTestClientPage();
        tester.assertComponent("feedback", FeedbackPanel.class);
    }

    @Test
    public void testFeedbackPanelContainsText() throws Exception {
        setupAndStartTestClientPage();

        setServiceInDropDown(0);
        setMethodInDropDown(0);
        formTester.setValue("argumentListContainer:argumentList:arg0:value", "test");
        formTester.setValue("argumentListContainer:argumentList:arg1:value", "test");
        tester.executeAjaxEvent("methodCallForm:submitButton", "onclick");

        FeedbackPanel feedbackPanel = (FeedbackPanel) tester.getComponentFromLastRenderedPage("feedback");
        tester.assertInfoMessages(new String[]{"Methodcall called successfully"});
        Label message = (Label) feedbackPanel.get("feedbackul:messages:0:message");
        Assert.assertEquals("Methodcall called successfully", message.getDefaultModelObjectAsString());
    }

    @Test
    public void testExceptionInFeedback() throws Exception {
        setupAndStartTestClientPage();

        setServiceInDropDown(0);
        setMethodInDropDown(0);
        formTester.setValue("argumentListContainer:argumentList:arg0:value", "fail");
        formTester.setValue("argumentListContainer:argumentList:arg1:value", "test");
        tester.executeAjaxEvent("methodCallForm:submitButton", "onclick");
        String resultException = (String) tester.getMessages(FeedbackMessage.ERROR).get(0);
        assertThat(resultException, containsString(IllegalArgumentException.class.getName()));
    }

    @Test
    public void testSubmitButtonIslocalized() throws Exception {
        setupAndStartTestClientPage();

        Button button = (Button) tester.getComponentFromLastRenderedPage("methodCallForm:submitButton");
        String buttonValue =
            tester.getApplication().getResourceSettings().getLocalizer().getString("form.call", button);
        Assert.assertEquals(buttonValue, button.getValue());
    }

    @Test
    public void testSelectOtherService_shouldClearArgumentList() throws Exception {
        setupAndStartTestClientPage();
        setServiceInDropDown(0);
        setMethodInDropDown(0);
        setServiceInDropDown(0);
        RepeatingView argList =
            (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");
        assertThat(argList.size(), is(0));
    }

    private List<ServiceReference> setupAndStartTestClientPage() {
        final List<ServiceReference> expected = setupTestClientPage();
        tester.startPage(TestClient.class);
        formTester = tester.newFormTester("methodCallForm");
        return expected;
    }

    private List<ServiceReference> setupTestClientPage() {
        final List<ServiceReference> expected = new ArrayList<ServiceReference>();
        ServiceReference serviceReferenceMock = mock(ServiceReference.class);
        when(serviceReferenceMock.getProperty("id")).thenReturn("test");
        expected.add(serviceReferenceMock);
        expected.add(serviceReferenceMock);
        managedServicesMock = mock(DomainService.class);
        when(managedServicesMock.getAllServiceInstances()).thenAnswer(new Answer<List<ServiceReference>>() {
            @Override
            public List<ServiceReference> answer(InvocationOnMock invocation) {
                return expected;
            }
        });
        DomainProvider domainProviderMock = mock(DomainProvider.class);
        LocalizableString nameMock = mock(LocalizableString.class);
        when(nameMock.getString(Mockito.<Locale> any())).thenReturn("testDomain");
        when(domainProviderMock.getName()).thenReturn(nameMock);
        when(domainProviderMock.getDescription()).thenReturn(nameMock);
        when(domainProviderMock.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) {
                return TestInterface.class;
            }
        });
        final List<DomainProvider> expectedProviders = new ArrayList<DomainProvider>();
        expectedProviders.add(domainProviderMock);
        when(managedServicesMock.domains()).thenAnswer(new Answer<List<DomainProvider>>() {
            @Override
            public List<DomainProvider> answer(InvocationOnMock invocation) {
                return expectedProviders;
            }
        });

        Mockito.when(managedServicesMock.serviceReferencesForDomain(TestInterface.class)).thenReturn(expected);

        ServiceManager serviceManagerMock = Mockito.mock(ServiceManager.class);
        List<ServiceManager> serviceManagerList = new ArrayList<ServiceManager>();
        serviceManagerList.add(serviceManagerMock);
        Mockito.when(managedServicesMock.serviceManagersForDomain(TestInterface.class)).thenReturn(serviceManagerList);

        ServiceDescriptor serviceDescriptorMock = Mockito.mock(ServiceDescriptor.class);
        Mockito.when(serviceDescriptorMock.getName()).thenReturn(new PassThroughLocalizableString("service.name"));
        Mockito.when(serviceDescriptorMock.getDescription()).thenReturn(
            new PassThroughLocalizableString("service.description"));
        Mockito.when(serviceManagerMock.getDescriptor()).thenReturn(serviceDescriptorMock);

        testService = new TestService();
        when(managedServicesMock.getService(any(ServiceReference.class))).thenReturn(testService);
        when(managedServicesMock.getService(anyString(), anyString())).thenReturn(testService);
        context.putBean(managedServicesMock);
        setupTesterWithSpringMockContext();
        return expected;
    }

    private void setupIndexPage() {
        DomainService domainServiceMock = mock(DomainService.class);
        context.putBean(domainServiceMock);
        setupTesterWithSpringMockContext();
    }

    private void setupTesterWithSpringMockContext() {
        tester.getApplication().addComponentInstantiationListener(
            new SpringComponentInjector(tester.getApplication(), context, true));
    }

    @Test
    public void testListToCreateNewServices() {
        setupAndStartTestClientPage();
        tester.debugComponentTrees();
        tester.assertRenderedPage(TestClient.class);
        Label domainName =
            (Label) tester.getComponentFromLastRenderedPage("serviceManagementContainer:domains:0:domain.name");
        Label domainDescription =
            (Label) tester.getComponentFromLastRenderedPage("serviceManagementContainer:domains:0:domain.description");
        Label domainClass =
            (Label) tester.getComponentFromLastRenderedPage("serviceManagementContainer:domains:0:domain.class");
        Label name =
            (Label) tester
                .getComponentFromLastRenderedPage("serviceManagementContainer:domains:0:services:0:service.name");
        Label description =
            (Label) tester.getComponentFromLastRenderedPage("serviceManagementContainer:domains:"
                    + "0:services:0:service.description");
        assertThat(domainName.getDefaultModel().getObject().toString(), equalTo("testDomain"));
        assertThat(domainDescription.getDefaultModel().getObject().toString(), equalTo("testDomain"));
        assertThat(domainClass.getDefaultModel().getObject().toString(), equalTo(TestInterface.class.getName()));
        Assert.assertEquals("service.name", name.getDefaultModel().getObject());
        Assert.assertEquals("service.description", description.getDefaultModel().getObject());
    }

    @Test
    public void showEditLink() {
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
    public void testTargetLocationOfEditButton() {
        setupAndStartTestClientPage();
        try {
            ServiceReference ref = Mockito.mock(ServiceReference.class);
            Mockito.when(ref.getProperty("managerId")).thenReturn("ManagerId");
            Mockito.when(ref.getProperty("domain")).thenReturn(TestInterface.class.getName());
            ServiceReference[] refs = new ServiceReference[]{ref};
            Mockito.when(bundleContext.getServiceReferences(Domain.class.getName(), "(id=test)")).thenReturn(refs);
        } catch (InvalidSyntaxException e) {
            Assert.fail("not expected");
        }

        List<ServiceManager> managerList = new ArrayList<ServiceManager>();
        ServiceManager serviceManagerMock = Mockito.mock(ServiceManager.class);
        ServiceDescriptor serviceDescriptor = Mockito.mock(ServiceDescriptor.class);
        Mockito.when(serviceDescriptor.getId()).thenReturn("ManagerId");
        Mockito.when(serviceDescriptor.getName()).thenReturn(new PassThroughLocalizableString("ServiceName"));
        Mockito.when(serviceDescriptor.getDescription()).thenReturn(
            new PassThroughLocalizableString("ServiceDescription"));

        Mockito.when(serviceManagerMock.getDescriptor()).thenReturn(serviceDescriptor);
        Mockito.when(serviceManagerMock.getDescriptor()).thenReturn(serviceDescriptor);

        managerList.add(serviceManagerMock);
        Mockito.when(managedServicesMock.serviceManagersForDomain(TestInterface.class)).thenReturn(managerList);

        if (!serviceListExpanded) {
            expandServiceListTree();
        }
        tester.debugComponentTrees();
        tester.clickLink("methodCallForm:serviceList:i:2:nodeComponent:contentLink", true);
        AjaxButton editButton = (AjaxButton) tester.getComponentFromLastRenderedPage("methodCallForm:editButton");
        Assert.assertEquals(true, editButton.isEnabled());
        tester.executeAjaxEvent(editButton, "onclick");

        ConnectorEditorPage editorPage = Mockito.mock(ConnectorEditorPage.class);
        tester.assertRenderedPage(editorPage.getPageClass());
    }
}
