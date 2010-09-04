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
package org.openengsb.ui.web.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.openengsb.core.config.Domain;
import org.openengsb.core.config.DomainProvider;
import org.openengsb.core.config.ServiceManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;


public class OsgiDomainServiceTest {

    private static interface DummyDomain extends Domain {
    }

    private static class DummyInstance implements DummyDomain {
    }

    @Test
    public void testGetDomains() throws InvalidSyntaxException {
        BundleContext contextMock = Mockito.mock(BundleContext.class);
        List<ServiceReference> references = new ArrayList<ServiceReference>();
        ServiceReference[] serviceReferences = references.toArray(new ServiceReference[] {});
        Mockito.when(contextMock.getAllServiceReferences(DummyDomain.class.getName(), null))
                .thenReturn(serviceReferences);
        List<DomainProvider> domainProvider = new ArrayList<DomainProvider>();
        DomainProvider provider = Mockito.mock(DomainProvider.class);
        domainProvider.add(provider);

        OsgiDomainService service = new OsgiDomainService(contextMock);
        service.setDomains(domainProvider);
        List<DomainProvider> domains = service.domains();

        assertNotSame(domainProvider, domains);
        assertEquals(1, domains.size());
        DomainProvider domainProviderFromService = domains.get(0);
        DomainProvider domainProviderLocal = domainProvider.get(0);
        assertEquals(domainProviderLocal.getName(), domainProviderFromService.getName());
        assertEquals(domainProviderLocal.getId(), domainProviderFromService.getId());
        assertEquals(domainProviderLocal.getDescription(), domainProviderFromService.getDescription());
        assertEquals(domainProviderLocal.getDomainInterface(), domainProviderFromService.getDomainInterface());
    }

    @Test
    public void serviceManagersForDomainClass() throws InvalidSyntaxException {
        BundleContext contextMock = Mockito.mock(BundleContext.class);
        List<ServiceReference> references = new ArrayList<ServiceReference>();
        ServiceReference singleReference = Mockito.mock(ServiceReference.class);
        references.add(singleReference);
        ServiceReference[] serviceReferences = references.toArray(new ServiceReference[] {});
        Mockito.when(contextMock.getAllServiceReferences(DummyDomain.class.getName(), null))
                .thenReturn(serviceReferences);
        List<DomainProvider> domainProvider = new ArrayList<DomainProvider>();
        DomainProvider provider = Mockito.mock(DomainProvider.class);
        domainProvider.add(provider);
        Mockito.when(
                contextMock.getAllServiceReferences(ServiceManager.class.getName(),
                        "(domain=" + DummyDomain.class.getName() + ")")).thenReturn(serviceReferences);
        ServiceManager serviceManagerMock = Mockito.mock(ServiceManager.class);
        Mockito.when(contextMock.getService(singleReference)).thenReturn(serviceManagerMock);

        OsgiDomainService service = new OsgiDomainService(contextMock);
        List<ServiceManager> serviceManagersForDomain = service.serviceManagersForDomain(DummyDomain.class);

        assertEquals(1, serviceManagersForDomain.size());
        assertSame(serviceManagerMock, serviceManagersForDomain.get(0));
    }

    @Test
    public void serviceReferencesForConnector() throws InvalidSyntaxException {
        BundleContext contextMock = Mockito.mock(BundleContext.class);
        List<ServiceReference> references = new ArrayList<ServiceReference>();
        ServiceReference singleReference = Mockito.mock(ServiceReference.class);
        references.add(singleReference);
        ServiceReference[] serviceReferences = references.toArray(new ServiceReference[] {});
        Mockito.when(contextMock.getAllServiceReferences(DummyInstance.class.getName(), null)).thenReturn(
                serviceReferences);

        OsgiDomainService service = new OsgiDomainService(contextMock);
        List<ServiceReference> serviceReferencesForConnector = service
                .serviceReferencesForConnector(DummyInstance.class);

        assertEquals(1, serviceReferencesForConnector.size());
        assertSame(singleReference, serviceReferencesForConnector.get(0));
    }
}
