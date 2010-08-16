package org.openengsb.ui.web.service.impl;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.easymock.internal.MocksControl.MockType;
import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.config.Domain;
import org.openengsb.core.config.DomainProvider;
import org.openengsb.core.config.ServiceManager;
import org.openengsb.ui.web.fixtures.log.LogDomain;
import org.openengsb.ui.web.fixtures.log.LogDomainProvider;
import org.openengsb.ui.web.fixtures.log.StdoutLogService;
import org.openengsb.ui.web.fixtures.log.StdoutLogServiceManager;
import org.openengsb.ui.web.mock.BundleContextMock;
import org.openengsb.ui.web.mock.ServiceReferenceMock;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class OsgiDomainServiceTest {

    private final List<DomainProvider> providers = Arrays.asList(new DomainProvider[] { new LogDomainProvider() });

    private final List<ServiceManager> serviceManagers = Arrays
            .asList(new ServiceManager[] { new StdoutLogServiceManager() });

    private final ServiceReference[] serviceReferences = new ServiceReference[] { new ServiceReferenceMock("name", "id") };

    private final BundleContext bundleContext = new BundleContextMock(serviceManagers);

    @Test
    public void testGetDomains() throws InvalidSyntaxException {
        BundleContext mock = Mockito.mock(BundleContext.class);
        Mockito.when(mock.getAllServiceReferences(LogDomain.class.getName(), null)).thenReturn(serviceReferences);
        OsgiDomainService service = new OsgiDomainService(bundleContext);
        service.setDomains(providers);
        List<DomainProvider> domains = service.domains();
        assertNotSame(providers, domains);
        assertEquals(1, domains.size());
        DomainProvider domainProviderFromService = domains.get(0);
        DomainProvider domainProviderLocal = providers.get(0);
        assertEquals(domainProviderLocal.getName(), domainProviderFromService.getName());
        assertEquals(domainProviderLocal.getId(), domainProviderFromService.getId());
        assertEquals(domainProviderLocal.getDescription(), domainProviderFromService.getDescription());
        assertEquals(domainProviderLocal.getDomainInterface(), domainProviderFromService.getDomainInterface());
    }

    @Test
    public void serviceManagersForDomainClass() throws InvalidSyntaxException {
        BundleContext mock = Mockito.mock(BundleContext.class);
        ServiceReferenceMock serviceReferenceMock = new ServiceReferenceMock("Name", "Id");
        ServiceReference[] serviceManagerReferences = new ServiceReference[] { serviceReferenceMock };
        Mockito.when(
                mock.getAllServiceReferences(ServiceManager.class.getName(),
                        "(domain=" + LogDomain.class.getName() + ")")).thenReturn(serviceManagerReferences);
        Mockito.when(mock.getService(serviceReferenceMock)).thenReturn(new StdoutLogServiceManager());

        OsgiDomainService service = new OsgiDomainService(mock);
        List<ServiceManager> serviceManagersForDomain = service.serviceManagersForDomain(LogDomain.class);
        assertEquals(1, serviceManagersForDomain.size());
        ServiceManager serviceManager = serviceManagersForDomain.get(0);
        assertEquals(LogDomain.class.getName(), serviceManager.getDescriptor().getServiceInterfaceId());
        assertEquals(StdoutLogService.class.getName(), serviceManager.getDescriptor().getId());
    }
    
    @Test
    public void serviceReferencesForConnector() throws InvalidSyntaxException{
        BundleContext mock = Mockito.mock(BundleContext.class);
        Mockito.when(mock.getAllServiceReferences(StdoutLogService.class.getName(), null)).thenReturn(serviceReferences);
        OsgiDomainService service = new OsgiDomainService(mock);
        List<ServiceReference> serviceReferencesForConnector = service.serviceReferencesForConnector(StdoutLogService.class);
        assertEquals(1, serviceReferencesForConnector.size());
        assertSame(this.serviceReferences[0], serviceReferencesForConnector.get(0));
    }
}
