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

package org.openengsb.ui.admin.serviceListPage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Locale;

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanelTester;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.request.target.component.PageRequestTarget;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.ConnectorProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.l10n.PassThroughLocalizableString;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.NullDomainImpl;
import org.openengsb.ui.admin.AbstractUITest;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ServicesListPageTest extends AbstractUITest {

    @Before
    public void setup() throws Exception {
        Locale.setDefault(new Locale("en"));

        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("connector", "bla");

        // tester.getApplication().addComponentInstantiationListener(
        // new SpringComponentInjector(tester.getApplication(), context, true));

    }

    private void startPage() {
        tester.startPage(ServiceListPage.class);
        AjaxLazyLoadPanelTester.executeAjaxLazyLoadPanel(tester, tester.getLastRenderedPage());
    }

    @Test
    public void verifyRenderedPage_ShouldBeServiceListPage() {
        startPage();

        tester.assertRenderedPage(ServiceListPage.class);
    }

    @Test
    public void verifyListViews_ShouldBe_Connecting_Online_Disconnecting_And_Disconnected() {
        NullDomainImpl domainService = new NullDomainImpl();
        domainService.setAliveState(AliveState.CONNECTING);

        startPage();
        tester.dumpPage();
        tester.assertContains("Connecting");
        tester.assertContains("ONLINE");
        tester.assertContains("OFFLINE");
        tester.assertContains("Disconnected");
        tester.assertComponent("lazy:content:connectingServicePanel:connectingServices", ListView.class);
        tester.assertComponent("lazy:content:onlineServicePanel:onlineServices", ListView.class);
        tester.assertComponent("lazy:content:offlineServicePanel:offlineServices", ListView.class);
        tester.assertComponent("lazy:content:disconnectedServicePanel:disconnectedServices", ListView.class);
        Label nameLabel =
            (Label) tester.getComponentFromLastRenderedPage("lazy:content:"
                    + "connectingServicePanel:connectingServices:0:service.name");
        assertThat(nameLabel.getDefaultModelObjectAsString(), is("testService"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void verifyListViews_ServiceShouldBeAfterStateChangeInOtherList() {
        NullDomainImpl domainService = setUpServicesMap();
        ServiceDescriptor serviceDescriptorMock = mock(ServiceDescriptor.class);
        when(serviceDescriptorMock.getId()).thenReturn("serviceManagerId");
        when(serviceDescriptorMock.getName()).thenReturn(new PassThroughLocalizableString("name"));
        when(serviceDescriptorMock.getDescription()).thenReturn(new PassThroughLocalizableString("desc"));
        ConnectorProvider mock2 = mock(ConnectorProvider.class);
        when(mock2.getDescriptor()).thenReturn(serviceDescriptorMock);
        Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put("domain", "test");
        props.put("connector", "test");
        registerService(mock2, props, ConnectorProvider.class);
        startPage();

        ListView<ServiceReference> connectingService =
            (ListView<ServiceReference>) tester
                .getComponentFromLastRenderedPage("lazy:content:connectingServicePanel:connectingServices");
        assertThat(connectingService.getModelObject().size(), is(1));

        ListView<ServiceReference> onlineServices =
            (ListView<ServiceReference>) tester
                .getComponentFromLastRenderedPage("lazy:content:onlineServicePanel:onlineServices");
        assertThat(onlineServices.getModelObject().size(), is(0));

        domainService.setAliveState(AliveState.ONLINE);

        final WebRequestCycle cycle = tester.setupRequestAndResponse();
        try {
            cycle.request(new PageRequestTarget(tester.getLastRenderedPage()));
        } finally {
            cycle.getResponse().close();
        }
        ListView<ServiceReference> onlineServicesNew =
            (ListView<ServiceReference>) tester
                .getComponentFromLastRenderedPage("lazy:content:onlineServicePanel:onlineServices");
        assertThat(onlineServicesNew.getModelObject().size(), is(1));
    }

    @Test
    public void testIfCorrectServiceDataIsInList_ShouldReturnTheNameOfTheServiceManagerAndDescrption() {
        setUpServicesMap();

        startPage();

        tester.debugComponentTrees();
        Label name =
            (Label) tester.getComponentFromLastRenderedPage("lazy:content:"
                    + "connectingServicePanel:connectingServices:0:service.name");
        Label description =
            (Label) tester.getComponentFromLastRenderedPage("lazy:content:"
                    + "connectingServicePanel:connectingServices:0:service.description");
        assertThat(name.getDefaultModelObjectAsString(), is("testService"));
        assertThat(description.getDefaultModelObjectAsString(), is("testDescription"));

    }

    @Test
    public void testVisibiltyOfInfoMessages() {
        ServiceDescriptor serviceDescriptorMock = mock(ServiceDescriptor.class);
        when(serviceDescriptorMock.getId()).thenReturn("serviceManagerId");
        when(serviceDescriptorMock.getDescription()).thenReturn(new PassThroughLocalizableString("testDescription"));

        startPage();
        tester.assertVisible("lazy:content:connectingServicePanel:noConServices");
        tester.assertVisible("lazy:content:connectingServicePanel:noConServices");
        tester.assertVisible("lazy:content:onlineServicePanel:noOnServices");
        tester.assertVisible("lazy:content:offlineServicePanel:noOffServices");
        tester.assertVisible("lazy:content:disconnectedServicePanel:noDisServices");
    }

    @Test
    public void verifyIfEditButtonAndDeleteButtonExist_ShouldReturnTrue() {
        setUpServicesMap();

        startPage();
        tester
            .assertComponent("lazy:content:connectingServicePanel:connectingServices:0:updateService", AjaxLink.class);
        tester
            .assertComponent("lazy:content:connectingServicePanel:connectingServices:0:deleteService", AjaxLink.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteLink_AllListsShouldBeEmptyAfterwards() {
        setUpServicesMap();
        startPage();
        ListView<ServiceReference> connectingServices =
            (ListView<ServiceReference>) tester
                .getComponentFromLastRenderedPage("lazy:content:connectingServicePanel:connectingServices");
        assertThat(connectingServices.size(), is(1));

        tester
            .assertComponent("lazy:content:connectingServicePanel:connectingServices:0:updateService", AjaxLink.class);
        tester
            .assertComponent("lazy:content:connectingServicePanel:connectingServices:0:deleteService", AjaxLink.class);
        tester.clickLink("lazy:content:connectingServicePanel:connectingServices:0:deleteService", true);
        tester.debugComponentTrees();

        ListView<ServiceReference> updateService =
            (ListView<ServiceReference>) tester
                .getComponentFromLastRenderedPage("lazy:content:connectingServicePanel:connectingServices");
        assertThat(updateService.size(), is(0));
        tester.assertVisible("lazy:content:connectingServicePanel:noConServices");
        tester.assertVisible("lazy:content:onlineServicePanel:noOnServices");
        tester.assertVisible("lazy:content:offlineServicePanel:noOffServices");
        tester.assertVisible("lazy:content:disconnectedServicePanel:noDisServices");
    }

    private NullDomainImpl setUpServicesMap() {
        ServiceReference serRef = mock(ServiceReference.class);
        when(serRef.getProperty("openengsb.service.type")).thenReturn("service");
        when(serRef.getProperty("id")).thenReturn("testService");
        when(serRef.getProperty("connector")).thenReturn("bla");
        NullDomainImpl domainService = new NullDomainImpl();
        ServiceDescriptor serviceDescriptorMock = mock(ServiceDescriptor.class);
        when(serviceDescriptorMock.getId()).thenReturn("serviceManagerId");
        when(serviceDescriptorMock.getDescription()).thenReturn(new PassThroughLocalizableString("testDescription"));
        domainService.setAliveState(AliveState.CONNECTING);
        return domainService;
    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        DefaultOsgiUtilsService osgiServiceUtils = new DefaultOsgiUtilsService();
        osgiServiceUtils.setBundleContext(bundleContext);
        registerService(osgiServiceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
        OpenEngSBCoreServices.setOsgiServiceUtils(osgiServiceUtils);
    }
}
