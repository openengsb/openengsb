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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
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
import org.openengsb.core.common.l10n.PassThroughLocalizableString;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.core.common.util.AliveState;
import org.osgi.framework.ServiceReference;

public class ServicesListPageTest {

    private ServiceManager serviceManagerMock;
    private WicketTester tester;
    private DomainService domainServiceMock;
    private List<ServiceReference> managedServiceInstances;
    private List<ServiceManager> serviceManagerListMock;

    public interface TestInterface extends Domain {
        void doSomethingToChangeState();
    }

    public static class TestService implements TestInterface {

        private AliveState aliveState = AliveState.CONNECTING;

        @Override
        public AliveState getAliveState() {
            return aliveState;
        }

        @Override
        public void doSomethingToChangeState() {
            aliveState = AliveState.ONLINE;
        }
    }

    @Before
    public void setup() {
        Locale.setDefault(new Locale("en"));
        tester = new WicketTester();
        ApplicationContextMock context = new ApplicationContextMock();
        serviceManagerMock = mock(ServiceManager.class);
        domainServiceMock = mock(DomainService.class);
        ContextCurrentService contextCurrentServiceMock = mock(ContextCurrentService.class);
        managedServiceInstances = new ArrayList<ServiceReference>();
        serviceManagerListMock = new ArrayList<ServiceManager>();

        context.putBean(serviceManagerMock);
        context.putBean("services", serviceManagerListMock);
        context.putBean(domainServiceMock);
        context.putBean(contextCurrentServiceMock);
        context.putBean("managedServiceInstances", managedServiceInstances);
        tester.getApplication()
            .addComponentInstantiationListener(new SpringComponentInjector(tester.getApplication(), context, true));
    }

    @Test
    public void verifyRenderedPage_ShouldBeServiceListPage() {
        tester.startPage(ServiceListPage.class);
        tester.assertRenderedPage(ServiceListPage.class);
    }

    @Test
    public void verifyListViews_ShouldBe_Connecting_Online_Disconnecting_And_Disconnected() {
        ServiceReference serRef = mock(ServiceReference.class);
        when(serRef.getProperty("openengsb.service.type")).thenReturn("service");
        when(serRef.getProperty("id")).thenReturn("testService");
        managedServiceInstances.add(serRef);
        TestService domainService = new TestService();
        when(domainServiceMock.getService(serRef)).thenReturn(domainService);

        tester.startPage(ServiceListPage.class);
        tester.dumpPage();
        tester.assertContains("Connecting");
        tester.assertContains("ONLINE");
        tester.assertContains("OFFLINE");
        tester.assertContains("Disconnected");
        tester.assertComponent("connectingServicePanel:connectingServices", ListView.class);
        tester.assertComponent("onlineServicePanel:onlineServices", ListView.class);
        tester.assertComponent("offlineServicePanel:offlineServices", ListView.class);
        tester.assertComponent("disconnectedServicePanel:disconnectedServices", ListView.class);
        Label nameLabel = (Label) tester
            .getComponentFromLastRenderedPage("connectingServicePanel:connectingServices:0:service.name");
        assertThat(nameLabel.getDefaultModelObjectAsString(), is("testService"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void verifyListViews_ServiceShouldBeAfterStateChangeInOtherList() {
        TestInterface domainService = setUpServicesMap();
        ServiceDescriptor serviceDescriptorMock = mock(ServiceDescriptor.class);
        when(serviceDescriptorMock.getId()).thenReturn("serviceManagerId");
        when(serviceDescriptorMock.getName()).thenReturn(new PassThroughLocalizableString("name"));
        when(serviceDescriptorMock.getDescription()).thenReturn(new PassThroughLocalizableString("desc"));
        when(serviceManagerMock.getDescriptor()).thenReturn(serviceDescriptorMock);
        tester.startPage(ServiceListPage.class);

        ListView<ServiceReference> connectingService = (ListView<ServiceReference>) tester
            .getComponentFromLastRenderedPage("connectingServicePanel:connectingServices");
        assertThat(connectingService.getModelObject().size(), is(1));

        ListView<ServiceReference> onlineServices = (ListView<ServiceReference>) tester
            .getComponentFromLastRenderedPage("onlineServicePanel:onlineServices");
        assertThat(onlineServices.getModelObject().size(), is(0));

        domainService.doSomethingToChangeState();

        final WebRequestCycle cycle = tester.setupRequestAndResponse();
        try {
            cycle.request(new PageRequestTarget(tester.getLastRenderedPage()));
        } finally {
            cycle.getResponse().close();
        }
        ListView<ServiceReference> onlineServicesNew = (ListView<ServiceReference>) tester
            .getComponentFromLastRenderedPage("onlineServicePanel:onlineServices");
        assertThat(onlineServicesNew.getModelObject().size(), is(1));
    }

    @Test
    public void testIfCorrectServiceDataIsInList_ShouldReturnTheNameOfTheServiceManagerAndDescrption() {
        setUpServicesMap();

        tester.startPage(ServiceListPage.class);

        tester.debugComponentTrees();
        Label name = (Label) tester
            .getComponentFromLastRenderedPage("connectingServicePanel:connectingServices:0:service.name");
        Label description = (Label) tester
            .getComponentFromLastRenderedPage("connectingServicePanel:connectingServices:0:service.description");
        assertThat(name.getDefaultModelObjectAsString(), is("testService"));
        assertThat(description.getDefaultModelObjectAsString(), is("testDescription"));

    }

    @Test
    public void testVisibiltyOfInfoMessages() {
        serviceManagerListMock.add(serviceManagerMock);

        ServiceDescriptor serviceDescriptorMock = mock(ServiceDescriptor.class);
        when(serviceDescriptorMock.getId()).thenReturn("serviceManagerId");
        when(serviceDescriptorMock.getDescription()).thenReturn(new PassThroughLocalizableString("testDescription"));
        when(serviceManagerMock.getDescriptor()).thenReturn(serviceDescriptorMock);

        tester.startPage(ServiceListPage.class);
        tester.assertVisible("connectingServicePanel:noConServices");
        tester.assertVisible("connectingServicePanel:noConServices");
        tester.assertVisible("onlineServicePanel:noOnServices");
        tester.assertVisible("offlineServicePanel:noOffServices");
        tester.assertVisible("disconnectedServicePanel:noDisServices");
    }

    @Test
    public void verifyIfEditButtonAndDeleteButtonExist_ShouldReturnTrue() {
        setUpServicesMap();

        tester.startPage(ServiceListPage.class);
        tester.assertComponent("connectingServicePanel:connectingServices:0:updateService", AjaxLink.class);
        tester.assertComponent("connectingServicePanel:connectingServices:0:deleteService", AjaxLink.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteLink_AllListsShouldBeEmptyAfterwards() {
        setUpServicesMap();
        tester.startPage(ServiceListPage.class);
        ListView<ServiceReference> connectingServices = (ListView<ServiceReference>) tester
            .getComponentFromLastRenderedPage("connectingServicePanel:connectingServices");
        assertThat(connectingServices.size(), is(1));

        tester.assertComponent("connectingServicePanel:connectingServices:0:updateService", AjaxLink.class);
        tester.assertComponent("connectingServicePanel:connectingServices:0:deleteService", AjaxLink.class);
        tester.clickLink("connectingServicePanel:connectingServices:0:deleteService", true);
        tester.debugComponentTrees();

        ListView<ServiceReference> updateService = (ListView<ServiceReference>) tester
            .getComponentFromLastRenderedPage("connectingServicePanel:connectingServices");
        assertThat(updateService.size(), is(0));
        tester.assertVisible("connectingServicePanel:noConServices");
        tester.assertVisible("onlineServicePanel:noOnServices");
        tester.assertVisible("offlineServicePanel:noOffServices");
        tester.assertVisible("disconnectedServicePanel:noDisServices");
    }

    private TestInterface setUpServicesMap() {
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
        when(serviceDescriptorMock.getDescription()).thenReturn(new PassThroughLocalizableString("testDescription"));
        when(serviceManagerMock.getDescriptor()).thenReturn(serviceDescriptorMock);
        return domainService;
    }
}
