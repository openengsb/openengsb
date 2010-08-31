/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.ui.web;

import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.config.Domain;
import org.openengsb.core.config.DomainProvider;
import org.openengsb.ui.web.editor.BeanArgumentPanel;
import org.openengsb.ui.web.editor.SimpleArgumentPanel;
import org.openengsb.ui.web.model.MethodId;
import org.openengsb.ui.web.model.ServiceId;
import org.openengsb.ui.web.service.DomainService;
import org.osgi.framework.ServiceReference;

public class TestClientTest {

    public interface TestInterface extends Domain {
        void update(String id, String name);

        void update(TestBean test);
    }

    public class TestService implements TestInterface {
        public boolean called = false;
        public TestBean test;

        @Override
        public void update(String id, String name) {
            called = true;
        }

        @Override
        public void update(TestBean test) {
            this.test = test;
        }

        public String getName(String id) {
            return "";
        }
    }

    private WicketTester tester;
    private ApplicationContextMock context;
    private TestService testService;
    private FormTester formTester;

    @Before
    public void setup() {
        tester = new WicketTester();
        context = new ApplicationContextMock();
        context.putBean(mock(ContextCurrentService.class));
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

    @Ignore
    @Test
    public void testShowServiceInstancesInDropdown() throws Exception {
        List<ServiceReference> expected = setupAndStartTestClientPage();

        @SuppressWarnings("rawtypes")
        Form<?> form = (Form) tester.getComponentFromLastRenderedPage("methodCallForm");
        @SuppressWarnings("unchecked")
        DropDownChoice<ServiceId> result = (DropDownChoice<ServiceId>) form.get("serviceList");

        Assert.assertNotNull(result);
        Assert.assertEquals(expected.size(), result.getChoices().size());
    }

    @Test
    public void testCreateMethodListDropDown() throws Exception {
        setupAndStartTestClientPage();

        tester.assertComponent("methodCallForm:methodList", DropDownChoice.class);
    }

    @Ignore("not adapted to tree yet")
    @Test
    public void testServiceListSelect() throws Exception {
        setupAndStartTestClientPage();

        setServiceInDropDown(0);

        @SuppressWarnings("unchecked")
        DropDownChoice<ServiceId> serviceList = (DropDownChoice<ServiceId>) tester
                .getComponentFromLastRenderedPage("methodCallForm:serviceList");
        ServiceId modelObject = serviceList.getModelObject();
        ServiceId reference = new ServiceId(TestService.class.getName(), "test");
        Assert.assertEquals(reference.toString(), modelObject.toString());
    }

    @Ignore("not adapted to tree yet")
    @Test
    public void testShowMethodListInDropDown() throws Exception {
        setupAndStartTestClientPage();
        @SuppressWarnings("unchecked")
        DropDownChoice<MethodId> methodList = (DropDownChoice<MethodId>) tester
                .getComponentFromLastRenderedPage("methodCallForm:methodList");

        setServiceInDropDown(0);

        List<? extends MethodId> choices = methodList.getChoices();
        List<Method> choiceMethods = new ArrayList<Method>();
        for (MethodId mid : choices) {
            choiceMethods.add(TestInterface.class.getMethod(mid.getName(), mid.getArgumentTypesAsClasses()));
        }
        Assert.assertEquals(Arrays.asList(TestInterface.class.getMethods()), choiceMethods);
    }

    @Test
    public void testCreateArgumentList() throws Exception {
        setupAndStartTestClientPage();

        Component argList = tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");

        Assert.assertNotNull(argList);
    }

    @Ignore("not adapted to tree yet")
    @Test
    public void testCreateTextFieldsFor2StringArguments() throws Exception {
        setupAndStartTestClientPage();
        RepeatingView argList = (RepeatingView) tester
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

    @Ignore("not adapted to tree yet")
    @Test
    public void testCreateTextFieldsForBean() throws Exception {
        setupAndStartTestClientPage();
        RepeatingView argList = (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");

        setServiceInDropDown(0);
        setMethodInDropDown(1);

        Assert.assertEquals(1, argList.size());
        Assert.assertEquals(BeanArgumentPanel.class, argList.get("arg0").getClass());

        RepeatingView panel = (RepeatingView) argList.get("arg0:fields");
        Assert.assertEquals(2, panel.size());
    }

    @Ignore("not adapted to tree yet")
    @Test
    public void testPerformMethodCall() throws Exception {
        setupAndStartTestClientPage();
        RepeatingView argList = (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");

        setServiceInDropDown(0);
        setMethodInDropDown(0);

        for (int i = 0; i < argList.size(); i++) {
            formTester.setValue("argumentListContainer:argumentList:arg" + i + ":value", "test");
        }
        tester.executeAjaxEvent("methodCallForm", "onsubmit");

        Assert.assertTrue(testService.called);
    }

    @Ignore("not adapted to tree yet")
    @Test
    public void testPerformMethodCallWithBeanArgument() throws Exception {
        setupAndStartTestClientPage();

        setServiceInDropDown(0);
        setMethodInDropDown(1);

        formTester.setValue("argumentListContainer:argumentList:arg0:fields:id:row:field", "42");
        formTester.setValue("argumentListContainer:argumentList:arg0:fields:name:row:field", "test");

        tester.executeAjaxEvent("methodCallForm", "onsubmit");

        Assert.assertNotNull(testService.test);
    }

    private void setServiceInDropDown(int index) {
        formTester.select("serviceList", index);
        tester.executeAjaxEvent("methodCallForm:serviceList", "onchange");
    }

    @Ignore("not adapted to tree yet")
    @Test
    public void testSelectMethodTwice() throws Exception {
        setupAndStartTestClientPage();
        RepeatingView argList = (RepeatingView) tester
                .getComponentFromLastRenderedPage("methodCallForm:argumentListContainer:argumentList");

        setServiceInDropDown(0);
        setMethodInDropDown(0);
        tester.executeAjaxEvent("methodCallForm:methodList", "onchange");

        Assert.assertEquals(2, argList.size());
    }

    private List<ServiceReference> setupAndStartTestClientPage() {
        final List<ServiceReference> expected = new ArrayList<ServiceReference>();
        ServiceReference serviceReferenceMock = Mockito.mock(ServiceReference.class);
        Mockito.when(serviceReferenceMock.getProperty("id")).thenReturn("test");
        expected.add(serviceReferenceMock);
        expected.add(serviceReferenceMock);
        DomainService managedServicesMock = Mockito.mock(DomainService.class);
        Mockito.when(managedServicesMock.getManagedServiceInstances()).thenAnswer(new Answer<List<ServiceReference>>() {
            @Override
            public List<ServiceReference> answer(InvocationOnMock invocation) throws Throwable {
                return expected;
            }
        });
        DomainProvider domainProviderMock = Mockito.mock(DomainProvider.class);
        Mockito.when(domainProviderMock.getName()).thenReturn("testDomain");
        Mockito.when(domainProviderMock.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) throws Throwable {
                return TestInterface.class;
            }
        });
        final List<DomainProvider> expectedProviders = new ArrayList<DomainProvider>();
        expectedProviders.add(domainProviderMock);
        Mockito.when(managedServicesMock.domains()).thenAnswer(new Answer<List<DomainProvider>>() {
            @Override
            public List<DomainProvider> answer(InvocationOnMock invocation) throws Throwable {
                return expectedProviders;
            }
        });

        Mockito.when(managedServicesMock.serviceReferencesForConnector(TestInterface.class)).thenReturn(expected);

        testService = new TestService();
        Mockito.when(managedServicesMock.getService(Mockito.any(ServiceReference.class))).thenReturn(testService);
        Mockito.when(managedServicesMock.getService(Mockito.anyString(), Mockito.anyString())).thenReturn(testService);
        context.putBean(managedServicesMock);
        setupTesterWithSpringMockContext();

        tester.startPage(TestClient.class);
        formTester = tester.newFormTester("methodCallForm");
        return expected;
    }

    private void setupIndexPage() {
        DomainService domainServiceMock = Mockito.mock(DomainService.class);
        context.putBean(domainServiceMock);
        setupTesterWithSpringMockContext();
    }

    private void setupTesterWithSpringMockContext() {
        tester.getApplication().addComponentInstantiationListener(
                new SpringComponentInjector(tester.getApplication(), context, true));
    }
}
