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

package org.openengsb.core.services.internal;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.model.ConnectorDefinition;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.NullDomainImpl;

public class ConnectorRegistrationManagerImplTest extends AbstractOsgiMockServiceTest {

    @Test
    public void testRegisterConnectorWithSameNameAfterRemoved_shouldNotFail() throws Exception {
        DefaultOsgiUtilsService defaultOsgiUtilsService = new DefaultOsgiUtilsService();
        defaultOsgiUtilsService.setBundleContext(bundleContext);
        ConnectorRegistrationManagerImpl connectorRegistrationManagerImpl = new ConnectorRegistrationManagerImpl();
        connectorRegistrationManagerImpl.setBundleContext(bundleContext);
        ConnectorInstanceFactory connectorInstanceFactoryMock = mock(ConnectorInstanceFactory.class);
        Connector connectorMock = mock(Connector.class);
        when(connectorInstanceFactoryMock.createNewInstance(anyString())).thenReturn(connectorMock);
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("connector", "a");
        props.put("domain", "a");
        registerService(connectorInstanceFactoryMock, props, ConnectorInstanceFactory.class);
        DomainProvider domainProviderMock = mock(DomainProvider.class);
        when(domainProviderMock.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) throws Throwable {
                return NullDomainImpl.class;
            }
        });
        Dictionary<String, Object> propsDomainProvider = new Hashtable<String, Object>();
        propsDomainProvider.put("domain", "a");
        registerService(domainProviderMock, propsDomainProvider, DomainProvider.class);
        ConnectorDefinition connectorId = new ConnectorDefinition("a", "a", "a");
        connectorRegistrationManagerImpl.updateRegistration(connectorId, new ConnectorDescription(
            new HashMap<String, String>(), new HashMap<String, Object>()));
        connectorRegistrationManagerImpl.remove(connectorId);
        connectorRegistrationManagerImpl.updateRegistration(new ConnectorDefinition("a", "a", "a"),
            new ConnectorDescription(new HashMap<String, String>(), new HashMap<String, Object>()));
    }

    @Test(expected = IllegalStateException.class)
    public void testNoDomainProviderAvailableForType_shouldThrowIllegalArgumentException() throws Exception {
        DefaultOsgiUtilsService defaultOsgiUtilsService = new DefaultOsgiUtilsService();
        defaultOsgiUtilsService.setBundleContext(bundleContext);
        ConnectorRegistrationManagerImpl connectorRegistrationManagerImpl = new ConnectorRegistrationManagerImpl();
        connectorRegistrationManagerImpl.setBundleContext(bundleContext);
        ConnectorInstanceFactory connectorInstanceFactoryMock = mock(ConnectorInstanceFactory.class);
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("connector", "a");
        props.put("domain", "a");
        registerService(connectorInstanceFactoryMock, props, ConnectorInstanceFactory.class);
        DomainProvider domainProviderMock = mock(DomainProvider.class);
        when(domainProviderMock.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) throws Throwable {
                return NullDomainImpl.class;
            }
        });
        Dictionary<String, Object> propsDomainProvider = new Hashtable<String, Object>();
        propsDomainProvider.put("domain", "a");
        registerService(domainProviderMock, propsDomainProvider, DomainProvider.class);
        ConnectorDefinition connectorId = new ConnectorDefinition("a", "a", "a");
        connectorRegistrationManagerImpl.updateRegistration(connectorId, new ConnectorDescription(
            new HashMap<String, String>(), new HashMap<String, Object>()));
    }

}
