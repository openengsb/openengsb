package org.openengsb.ui.web;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.util.AliveEnum;
import org.openengsb.ui.web.service.DomainService;
import org.osgi.framework.ServiceReference;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServicesListPageTest {

    private ServiceManager serviceManagerMock;
    private WicketTester tester;
    private ApplicationContextMock context;
    private DomainService servicesMock;
    private ContextCurrentService contextCurrentServiceMock;
    private List<ServiceReference> managedServiceInstances;


    public interface TestInterface extends Domain {

    }

    public class TestService implements TestInterface {

        @Override
        public AliveEnum getAliveState() {
            return AliveEnum.CONNECTING;
        }
    }


    @Before
    @SuppressWarnings("deprecation")
    public void setup() {
        tester = new WicketTester();
        context = new ApplicationContextMock();
        serviceManagerMock = mock(ServiceManager.class);
        servicesMock = mock(DomainService.class);
        contextCurrentServiceMock = mock(ContextCurrentService.class);
        managedServiceInstances = new ArrayList<ServiceReference>();
        context.putBean(serviceManagerMock);
        context.putBean(servicesMock);
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
        tester.debugComponentTrees();
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

    private void setUpDomainServiceMap() {
        ServiceReference serRef = mock(ServiceReference.class);
        when(serRef.getProperty("openengsb.service.type")).thenReturn("service");
        when(serRef.getProperty("id")).thenReturn("testService");
        managedServiceInstances.add(serRef);
        TestService domainService = new TestService();
        when(servicesMock.getService(serRef)).thenReturn(domainService);
    }


    private void setupTesterWithSpringMockContext() {
        tester.getApplication()
            .addComponentInstantiationListener(new SpringComponentInjector(tester.getApplication(), context, true));
    }


}
