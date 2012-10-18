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
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullDomainImpl;
import org.openengsb.core.util.DefaultOsgiUtilsService;

public class ConnectorRegistrationManagerImplTest extends AbstractOsgiMockServiceTest {

    private ConnectorRegistrationManager connectorRegistrationManager;
    private ConnectorInstanceFactory connectorInstanceFactory;

    @Before
    public void setUp() throws Exception {
        DefaultOsgiUtilsService defaultOsgiUtilsService = new DefaultOsgiUtilsService();
        defaultOsgiUtilsService.setBundleContext(bundleContext);
        connectorRegistrationManager = new ConnectorRegistrationManager();
        connectorRegistrationManager.setBundleContext(bundleContext);
        connectorInstanceFactory = mock(ConnectorInstanceFactory.class);
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("connector", "a");
        props.put("domain", "a");
        registerService(connectorInstanceFactory, props, ConnectorInstanceFactory.class);
    }

    @Test
    public void testRegisterConnectorWithSameNameAfterRemoved_shouldNotFail() throws Exception {
        createDomainProviderMock(NullDomain.class, "a");
        Connector connectorMock = mock(Connector.class);
        when(connectorInstanceFactory.createNewInstance(anyString())).thenReturn(connectorMock);
        String connectorId = UUID.randomUUID().toString();
        connectorRegistrationManager.updateRegistration(connectorId, new ConnectorDescription("a", "a",
                new HashMap<String, String>(), new HashMap<String, Object>()));
        connectorRegistrationManager.remove(connectorId);
        connectorRegistrationManager.updateRegistration(connectorId,
                new ConnectorDescription("a", "a", new HashMap<String, String>(), new HashMap<String, Object>()));
    }

    @Test(expected = IllegalStateException.class)
    public void testNoDomainProviderAvailableForType_shouldThrowIllegalArgumentException() throws Exception {
        DomainProvider domainProviderMock = mock(DomainProvider.class);
        when(domainProviderMock.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) throws Throwable {
                return NullDomainImpl.class;
            }
        });
        String connectorId = UUID.randomUUID().toString();
        connectorRegistrationManager.updateRegistration(connectorId, new ConnectorDescription("a", "a",
                new HashMap<String, String>(), new HashMap<String, Object>()));
    }
}
