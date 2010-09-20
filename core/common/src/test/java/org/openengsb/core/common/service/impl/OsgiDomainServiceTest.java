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

package org.openengsb.core.common.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.service.impl.OsgiDomainService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.sameInstance;

import static org.junit.Assert.assertThat;

import static org.junit.matchers.JUnitMatchers.hasItem;

public class OsgiDomainServiceTest {

    private BundleContext contextMock;
    private OsgiDomainService service;
    private List<DomainProvider> providerList;
    private ArrayList<ServiceReference> allServices;
    private ServiceReference instanceServiceReferenceMock;
    private ServiceReference serviceManagerReferenceMock;
    private ServiceManager serviceManagerMock;
    private DummyDomain serviceMock;

    private static interface DummyDomain extends Domain {
    }

    private ServiceReference[] getServiceReferenceArray() {
        return allServices.toArray(new ServiceReference[allServices.size()]);
    }

    @Before
    public void setUp() throws Exception {
        contextMock = mock(BundleContext.class);
        allServices = new ArrayList<ServiceReference>();
        when(contextMock.getAllServiceReferences(DummyDomain.class.getName(), null)).thenReturn(
                getServiceReferenceArray());

        service = new OsgiDomainService();
        service.setBundleContext(contextMock);

        DomainProvider provider = mock(DomainProvider.class);
        providerList = new ArrayList<DomainProvider>();
        providerList.add(provider);
        service.setDomains(providerList);

        instanceServiceReferenceMock = mock(ServiceReference.class);
        String filter = String.format("(domain=%s)", DummyDomain.class.getName());
        when(contextMock.getAllServiceReferences(DummyDomain.class.getName(), filter)).thenReturn(
                new ServiceReference[] { instanceServiceReferenceMock });
        when(contextMock.getAllServiceReferences(DummyDomain.class.getName(), null)).thenReturn(
                new ServiceReference[] { instanceServiceReferenceMock });
        when(contextMock.getAllServiceReferences(Domain.class.getName(), null)).thenReturn(
                new ServiceReference[] { instanceServiceReferenceMock });
        String idFilter = String.format("(id=%s)", "42");
        when(contextMock.getAllServiceReferences(DummyDomain.class.getName(), idFilter)).thenReturn(
                new ServiceReference[] { instanceServiceReferenceMock });
        serviceMock = mock(DummyDomain.class);
        when(contextMock.getService(instanceServiceReferenceMock)).thenReturn(serviceMock);

        serviceManagerReferenceMock = mock(ServiceReference.class);
        filter = String.format("(domain=%s)", DummyDomain.class.getName());
        when(contextMock.getAllServiceReferences(ServiceManager.class.getName(), filter)).thenReturn(
                new ServiceReference[] { serviceManagerReferenceMock });
        serviceManagerMock = mock(ServiceManager.class);
        when(contextMock.getService(serviceManagerReferenceMock)).thenReturn(serviceManagerMock);
    }

    @Test
    public void testGetDomains() throws Exception {
        List<DomainProvider> domains = service.domains();
        assertThat(domains, not(sameInstance(providerList)));
        assertThat(domains.get(0).getId(), equalTo(providerList.get(0).getId()));
    }

    @Test
    public void serviceManagersForDomainClass() throws InvalidSyntaxException {
        List<ServiceManager> serviceManagers = service.serviceManagersForDomain(DummyDomain.class);

        assertThat(serviceManagers, hasItem(serviceManagerMock));
    }

    @Test
    public void serviceReferencesForDomain() throws InvalidSyntaxException {
        List<ServiceReference> serviceReferencesForConnector = service.serviceReferencesForDomain(DummyDomain.class);

        assertThat(serviceReferencesForConnector, hasItem(instanceServiceReferenceMock));
    }

    @Test
    public void testGetAllServiceInstances() throws Exception {
        @SuppressWarnings("unchecked")
        List<ServiceReference> allServiceInstances = (List<ServiceReference>) service.getAllServiceInstances();

        assertThat(allServiceInstances, hasItem(instanceServiceReferenceMock));
        assertThat(allServiceInstances, not(hasItem(serviceManagerReferenceMock)));
    }

    @Test
    public void testGetAllServicveInstancesWhenThereAreNoServices() throws Exception {
        when(contextMock.getAllServiceReferences(anyString(), eq((String) null))).thenReturn(null);
        assertThat(service.getAllServiceInstances().isEmpty(), is(true));
    }

    @Test
    public void testGetService() throws Exception {
        ServiceManager result = (ServiceManager) service.getService(serviceManagerReferenceMock);
        assertThat(result, sameInstance(serviceManagerMock));
    }

    @Test
    public void testGetServiceById() throws Exception {
        DummyDomain service2 = (DummyDomain) service.getService(DummyDomain.class.getName(), "42");
        assertThat(service2, sameInstance(serviceMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNonExistingService_shouldThrowIllegalArgumentException() throws Exception {
        service.getService(DummyDomain.class.getName(), "21");
    }
}
