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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.request.target.component.PageRequestTarget;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.util.AliveEnum;
import org.openengsb.ui.web.service.DomainService;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServicesListPageTest {

    private ServiceManager serviceManagerMock;
    private WicketTester tester;
    private ApplicationContextMock context;
    private DomainService domainServiceMock;
    private ContextCurrentService contextCurrentServiceMock;
    private List<ServiceReference> managedServiceInstances;
    private List<ServiceManager> serviceManagerListMock;

    public interface TestInterface extends Domain {
        void doSomethingToChangeState();
    }

    public class TestService implements TestInterface {

        AliveEnum aliveState = AliveEnum.CONNECTING;

        @Override
        public AliveEnum getAliveState() {
            return aliveState;
        }


        @Override
        public void doSomethingToChangeState() {
            aliveState = AliveEnum.ONLINE;
        }
    }


    @Before
    @SuppressWarnings("deprecation")
    public void setup() {
        tester = new WicketTester();
        context = new ApplicationContextMock();
        serviceManagerMock = mock(ServiceManager.class);
        domainServiceMock = mock(DomainService.class);
        contextCurrentServiceMock = mock(ContextCurrentService.class);
        managedServiceInstances = new ArrayList<ServiceReference>();
        serviceManagerListMock = new ArrayList<ServiceManager>();


        context.putBean(serviceManagerMock);
        context.putBean("services", serviceManagerListMock);
        context.putBean(domainServiceMock);
        context.putBean(contextCurrentServiceMock);
        context.putBean("managedServiceInstances", managedServiceInstances);
        setupTesterWithSpringMockContext();
    }


    @Test
    public void verifyRenderedPage_ShouldBeServiceListPage() {
        tester.startPage(ServiceListPage.class);
        tester.assertRenderedPage(ServiceListPage.class);
    }

    @Test
    public void verifyListViews_ShouldBe_Connecting_Online_Disconnecting_And_Disconnected() {
        setUpDomainServiceMap();
        tester.startPage(ServiceListPage.class);
        tester.assertContains("Connecting");
        tester.assertContains("ONLINE");
        tester.assertContains("OFFLINE");
        tester.assertContains("Disconnected");
        tester.assertComponent("connectingServices", ListView.class);
        tester.assertComponent("onlineServices", ListView.class);
        tester.assertComponent("offlineServices", ListView.class);
        tester.assertComponent("disconnectedServices", ListView.class);
        Label nameLabel = (Label) tester.getComponentFromLastRenderedPage("connectingServices:0:service.name");
        assertThat(nameLabel.getDefaultModelObjectAsString(), is("testService"));
    }

    @Test
    public void verifyListViews_ServiceShouldBeAfterStateChangeInOtherList() {
        serviceManagerListMock.add(serviceManagerMock);
        ServiceReference serRef = mock(ServiceReference.class);
        when(serRef.getProperty("openengsb.service.type")).thenReturn("service");
        when(serRef.getProperty("id")).thenReturn("testService");
        when(serRef.getProperty("managerId")).thenReturn("serviceManagerId");
        managedServiceInstances.add(serRef);
        TestInterface domainService = new TestService();
        when(domainServiceMock.getService(serRef)).thenReturn(domainService);

        ServiceDescriptor serviceDescriptorMock = mock(ServiceDescriptor.class);
        when(serviceDescriptorMock.getId()).thenReturn("serviceManagerId");

        when(serviceManagerMock.getDescriptor((Locale) anyObject())).thenReturn(serviceDescriptorMock);

        tester.startPage(ServiceListPage.class);

        ListView connectingService = (ListView) tester.getComponentFromLastRenderedPage("connectingServices");
        assertThat(connectingService.getModelObject().size(), is(1));

        ListView onlineServices = (ListView) tester.getComponentFromLastRenderedPage("onlineServices");
        assertThat(onlineServices.getModelObject().size(), is(0));

        domainService.doSomethingToChangeState();

        final WebRequestCycle cycle = tester.setupRequestAndResponse();
        try {
            cycle.request(new PageRequestTarget(tester.getLastRenderedPage()));
        } finally {
            cycle.getResponse().close();
        }
        ListView onlineServicesNew = (ListView) tester.getComponentFromLastRenderedPage("onlineServices");
        assertThat(onlineServicesNew.getModelObject().size(), is(1));
    }

    @Test
    public void testIfCorrectServiceDataIsInList_ShouldReturnTheNameOfTheServiceManagerAndDescrption() {
        serviceManagerListMock.add(serviceManagerMock);
        ServiceReference serRef = mock(ServiceReference.class);
        when(serRef.getProperty("openengsb.service.type")).thenReturn("service");
        when(serRef.getProperty("id")).thenReturn("testService");
        when(serRef.getProperty("managerId")).thenReturn("serviceManagerId");
        managedServiceInstances.add(serRef);
        TestInterface domainService = new TestService();
        when(domainServiceMock.getService(serRef)).thenReturn(domainService);

        ServiceDescriptor serviceDescriptorMock = mock(ServiceDescriptor.class);
        when(serviceDescriptorMock.getId()).thenReturn("serviceManagerId");
        when(serviceDescriptorMock.getDescription()).thenReturn("testDescription");

        when(serviceManagerMock.getDescriptor((Locale) anyObject())).thenReturn(serviceDescriptorMock);
        tester.startPage(ServiceListPage.class);

        tester.debugComponentTrees();
        Label name = (Label) tester.getComponentFromLastRenderedPage("connectingServices:0:service.name");
        Label description = (Label) tester.getComponentFromLastRenderedPage("connectingServices:0:service.description");
        assertThat(name.getDefaultModelObjectAsString(), is("testService"));
        assertThat(description.getDefaultModelObjectAsString(), is("testDescription"));

    }

    @Test
    public void testVisibiltyOfInfoMessages() {
        serviceManagerListMock.add(serviceManagerMock);
        ServiceReference serRef = mock(ServiceReference.class);
        when(serRef.getProperty("openengsb.service.type")).thenReturn("service");
        when(serRef.getProperty("id")).thenReturn("testService");
        when(serRef.getProperty("managerId")).thenReturn("serviceManagerId");
        managedServiceInstances.add(serRef);
        TestInterface domainService = new TestService();
        when(domainServiceMock.getService(serRef)).thenReturn(domainService);

        ServiceDescriptor serviceDescriptorMock = mock(ServiceDescriptor.class);
        when(serviceDescriptorMock.getId()).thenReturn("serviceManagerId");
        when(serviceDescriptorMock.getDescription()).thenReturn("testDescription");

        when(serviceManagerMock.getDescriptor((Locale) anyObject())).thenReturn(serviceDescriptorMock);
        tester.startPage(ServiceListPage.class);
        tester.assertVisible("noOnServices");
        tester.assertVisible("noOffServices");
        tester.assertVisible("noDisServices");
        tester.assertInvisible("noConServices");
    }

    @Test
    public void verifyIfEditButton_OnClickShoutReturnEditorPage() {
        serviceManagerListMock.add(serviceManagerMock);
        ServiceReference serRef = mock(ServiceReference.class);
        when(serRef.getProperty("openengsb.service.type")).thenReturn("service");
        when(serRef.getProperty("id")).thenReturn("testService");
        when(serRef.getProperty("managerId")).thenReturn("serviceManagerId");
        managedServiceInstances.add(serRef);
        TestInterface domainService = new TestService();
        when(domainServiceMock.getService(serRef)).thenReturn(domainService);

        ServiceDescriptor serviceDescriptorMock = mock(ServiceDescriptor.class);
        when(serviceDescriptorMock.getId()).thenReturn("serviceManagerId");
        when(serviceDescriptorMock.getDescription()).thenReturn("testDescription");

        when(serviceManagerMock.getDescriptor((Locale) anyObject())).thenReturn(serviceDescriptorMock);
        tester.startPage(ServiceListPage.class);
        tester.debugComponentTrees();
        tester.assertComponent("connectingServices:0:updateService", Link.class);
    }


    private void setUpDomainServiceMap() {
        ServiceReference serRef = mock(ServiceReference.class);
        when(serRef.getProperty("openengsb.service.type")).thenReturn("service");
        when(serRef.getProperty("id")).thenReturn("testService");
        managedServiceInstances.add(serRef);
        TestService domainService = new TestService();
        when(domainServiceMock.getService(serRef)).thenReturn(domainService);
    }


    private void setupTesterWithSpringMockContext() {
        tester.getApplication()
            .addComponentInstantiationListener(new SpringComponentInjector(tester.getApplication(), context, true));
    }


}
