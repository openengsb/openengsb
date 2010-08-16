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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
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
import org.openengsb.ui.web.service.DomainService;
import org.openengsb.ui.web.service.ManagedServices;
import org.osgi.framework.ServiceReference;

public class TestClientTest {

    private WicketTester tester;
    private ApplicationContextMock context;

    @Before
    public void setup() {
        tester = new WicketTester();
        context = new ApplicationContextMock();
    }

    @Test
    public void testNavigateFromIndexToTestclient() throws Exception {
        setupIndexPage();

        tester.startPage(Index.class);
        tester.clickLink("testclientlink");

        tester.assertRenderedPage(TestClient.class);
    }

    @Test
    public void testLinkAppearsWithCaptionTestClient() throws Exception {
        setupIndexPage();

        tester.startPage(Index.class);

        tester.assertContains("Test Client");
    }

    @Test
    public void testParameterFormIsCreated() throws Exception {
        setupTestClientPage();
        tester.startPage(TestClient.class);

        tester.assertComponent("methodCallForm", Form.class);
    }

    @Test
    public void testShowServiceInstancesInDropdown() throws Exception {
        List<ServiceReference> expected = setupTestClientPage();

        tester.startPage(TestClient.class);

        @SuppressWarnings("rawtypes")
        Form<?> form = (Form) tester.getComponentFromLastRenderedPage("methodCallForm");
        @SuppressWarnings("unchecked")
        DropDownChoice<ServiceReference> result = (DropDownChoice<ServiceReference>) form.get("serviceList");

        Assert.assertNotNull(result);
        Assert.assertSame(expected, result.getChoices());
    }

    @Test
    public void testServiceInstancesDropDownIsLabeledWithSerivces() throws Exception {
        setupTestClientPage();

        tester.startPage(TestClient.class);

        tester.assertContains("Services: ");
    }

    @Test
    public void testCreateMethodListDropDown() throws Exception {
        setupTestClientPage();

        tester.startPage(TestClient.class);

        Form<?> form = (Form) tester.getComponentFromLastRenderedPage("methodCallForm");
        Assert.assertNotNull(form.get("methodList"));
    }

    @Test
    public void testServiceListSelect() throws Exception {
        setupTestClientPage();

        tester.startPage(TestClient.class);

        @SuppressWarnings("rawtypes")
        Form<?> form = (Form) tester.getComponentFromLastRenderedPage("methodCallForm");
        @SuppressWarnings("unchecked")
        DropDownChoice<Method> serviceList = (DropDownChoice<Method>) form.get("serviceList");
        FormTester formTester = tester.newFormTester("methodCallForm");
        formTester.select("serviceList", 0);

        serviceList.getValue();
    }

    @Ignore("in progress")
    @Test
    public void testShowMethodListInDropDown() throws Exception {
        setupTestClientPage();

        tester.startPage(TestClient.class);

        @SuppressWarnings("rawtypes")
        Form<?> form = (Form) tester.getComponentFromLastRenderedPage("methodCallForm");
        @SuppressWarnings("unchecked")
        DropDownChoice<Method> methodList = (DropDownChoice<Method>) form.get("methodList");
        FormTester formTester = tester.newFormTester("methodCallForm");

        formTester.select("serviceList", 0);

        List<? extends Method> choices = methodList.getChoices();
        Assert.assertFalse(choices.isEmpty());
    }

    private List<ServiceReference> setupTestClientPage() {
        final List<ServiceReference> expected = new ArrayList<ServiceReference>();
        ServiceReference serviceReferenceMock = Mockito.mock(ServiceReference.class);
        expected.add(serviceReferenceMock);
        DomainService managedServicesMock = Mockito.mock(DomainService.class);
        Mockito.when(managedServicesMock.getManagedServiceInstances()).thenAnswer(new Answer<List<ServiceReference>>(){
            @Override
            public List<ServiceReference> answer(InvocationOnMock invocation) throws Throwable {
                return expected;
            }
        });
        context.putBean(managedServicesMock);
        setupTesterWithSpringMockContext();
        return expected;
    }

    private void setupIndexPage() {
        DomainService domainServiceMock = Mockito.mock(DomainService.class);
        context.putBean(domainServiceMock);
        ManagedServices managedServicesMock = Mockito.mock(ManagedServices.class);
        context.putBean(managedServicesMock);
        setupTesterWithSpringMockContext();
    }

    private void setupTesterWithSpringMockContext() {
        tester.getApplication().addComponentInstantiationListener(
                new SpringComponentInjector(tester.getApplication(), context, true));
    }
}
