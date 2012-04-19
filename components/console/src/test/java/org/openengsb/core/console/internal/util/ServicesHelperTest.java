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

package org.openengsb.core.console.internal.util;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.WiringService;
import org.openengsb.core.api.descriptor.AttributeDefinition;
import org.openengsb.core.api.l10n.LocalizableString;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullDomainImpl;
import org.osgi.framework.ServiceReference;

public class ServicesHelperTest {

    private ServicesHelper serviceHelper;
    private DefaultOsgiUtilsService osgiServiceMock;
    private ConnectorManager connectorManagerMock;

    @Before
    public void init() {
        osgiServiceMock = mock(DefaultOsgiUtilsService.class);
        final List<DomainProvider> domainProviders = new ArrayList<DomainProvider>();
        final List<Domain> domainEndpoints = new ArrayList<Domain>();
        Domain domainEndpoint = new NullDomainImpl("id");
        domainEndpoints.add(domainEndpoint);
        DomainProvider domainProviderMock = mock(DomainProvider.class);
        when(domainProviderMock.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return NullDomain.class;
            }
        });
        LocalizableString descriptionMock = mock(LocalizableString.class);
        LocalizableString nameDescritptionMock = mock(LocalizableString.class);
        when(descriptionMock.getString(any(Locale.class))).thenReturn("Dummy description");
        when(nameDescritptionMock.getString(any(Locale.class))).thenReturn("Dummy Name");
        when(domainProviderMock.getDescription()).thenReturn(descriptionMock);
        when(domainProviderMock.getName()).thenReturn(nameDescritptionMock);
        domainProviders.add(domainProviderMock);

        when(osgiServiceMock.listServices(DomainProvider.class)).thenAnswer(new Answer<List<DomainProvider>>() {
            @Override
            public List<DomainProvider> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return domainProviders;
            }
        });
        final List<ServiceReference> serviceReferences = new ArrayList<ServiceReference>();
        ServiceReference serviceReferencesMock = mock(ServiceReference.class);
        serviceReferences.add(serviceReferencesMock);
        when(serviceReferencesMock.getProperty("id")).thenReturn("dummyId");

        when(osgiServiceMock.listServiceReferences(Domain.class)).thenAnswer(new Answer<List<ServiceReference>>() {
            @Override
            public List<ServiceReference> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return serviceReferences;
            }
        });

        WiringService wiringServiceMock = mock(WiringService.class);
        when(osgiServiceMock.getService(WiringService.class)).thenReturn(wiringServiceMock);
        when(wiringServiceMock.getDomainEndpoints(NullDomain.class, "*")).thenAnswer(new Answer<List<? extends
            Domain>>() {
            @Override
            public List<? extends Domain> answer(InvocationOnMock invocationOnMock) throws Throwable {
                return domainEndpoints;
            }
        });

        connectorManagerMock = mock(ConnectorManager.class);
        when(osgiServiceMock.getService(ConnectorManager.class)).thenAnswer(new Answer<ConnectorManager>() {
            @Override
            public ConnectorManager answer(InvocationOnMock invocationOnMock) throws Throwable {
                return connectorManagerMock;
            }
        });

        serviceHelper = new ServicesHelper();
        serviceHelper.setOsgiUtilsService(osgiServiceMock);
    }

    @Test
    public void testGetRunningServiceIds() throws Exception {
        List<String> runningServiceIds = serviceHelper.getRunningServiceIds();
        assertTrue(runningServiceIds.contains("dummyId"));
    }

    @Test
    public void testGetDomainProviderNames() {
        List<String> domainProviderNames = serviceHelper.getDomainProviderNames();
        assertTrue(domainProviderNames.contains("Dummy Name"));
    }

    @Test
    public void testGetConnectorAttributes() throws PersistenceException {
        //get attributes from user but without reading from the input
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("attr1", "val1");
        attributes.put("attr2", "val2");

        AttributeDefinition atr1 = createAttributeMock("attr1", "1");
        AttributeDefinition atr2 = createAttributeMock("attr2", "2");


        List<AttributeDefinition> attributeDefinitions = new ArrayList<AttributeDefinition>();
        attributeDefinitions.add(atr1);
        attributeDefinitions.add(atr2);
        Map<String, String> connectorAttributes =
            serviceHelper.getConnectorAttributes(attributeDefinitions, attributes);
        assertTrue(connectorAttributes.get("name1").equals("val1"));
        assertTrue(connectorAttributes.get("name2").equals("val2"));
    }

    private AttributeDefinition createAttributeMock(String attr1, final String id) {
        AttributeDefinition atr2 = mock(AttributeDefinition.class);
        when(atr2.getId()).thenReturn(attr1);
        when(atr2.getName()).thenAnswer(new Answer<LocalizableString>() {
            @Override
            public LocalizableString answer(InvocationOnMock invocationOnMock) throws Throwable {
                LocalizableString nameLocalizerMock = mock(LocalizableString.class);
                when(nameLocalizerMock.getString(any(Locale.class))).thenReturn("name" + id);
                return nameLocalizerMock;
            }
        });

        when(atr2.getDefaultValue()).thenAnswer(new Answer<LocalizableString>() {
            @Override
            public LocalizableString answer(InvocationOnMock invocationOnMock) throws Throwable {
                LocalizableString nameLocalizerMock = mock(LocalizableString.class);
                when(nameLocalizerMock.getString(any(Locale.class))).thenReturn("defaultValue" + id);
                return nameLocalizerMock;
            }
        });
        when(atr2.getDescription()).thenAnswer(new Answer<LocalizableString>() {
            @Override
            public LocalizableString answer(InvocationOnMock invocationOnMock) throws Throwable {
                LocalizableString nameLocalizerMock = mock(LocalizableString.class);
                when(nameLocalizerMock.getString(any(Locale.class))).thenReturn("description" + id);
                return nameLocalizerMock;
            }
        });
        return atr2;
    }
}
