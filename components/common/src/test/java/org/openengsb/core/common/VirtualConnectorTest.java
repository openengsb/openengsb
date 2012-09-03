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

package org.openengsb.core.common;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.VirtualConnectorProvider;
import org.openengsb.core.common.internal.VirtualConnectorManager;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.util.DefaultOsgiUtilsService;
import org.openengsb.core.util.FilterUtils;
import org.osgi.framework.Filter;

public class VirtualConnectorTest extends AbstractOsgiMockServiceTest {

    private OsgiUtilsService utilsService;
    private VirtualConnectorProvider virtualConnectorProvider;
    private VirtualConnectorManager pseudoConnectorManager;

    static class DummyVirtualConnector extends VirtualConnector {

        public DummyVirtualConnector(String instanceId) {
            super(instanceId);
        }

        @Override
        protected Object doInvoke(Object proxy, Method method, Object[] args) throws Throwable {
            return null;
        }

    }

    static class DummyVirtualConnectorFactory extends VirtualConnectorFactory<DummyVirtualConnector> {
        protected DummyVirtualConnectorFactory(DomainProvider domainProvider) {
            super(domainProvider);
        }

        @Override
        protected DummyVirtualConnector createNewHandler(String id) {
            return new DummyVirtualConnector(id);
        }

        @Override
        protected void updateHandlerAttributes(DummyVirtualConnector handler, Map<String, String> attributes) {

        }

        @Override
        public Map<String, String> getValidationErrors(Connector instance, Map<String, String> attributes) {
            return Collections.emptyMap();
        }

        @Override
        public Map<String, String> getValidationErrors(Map<String, String> attributes) {
            return Collections.emptyMap();
        }

    }

    @Before
    public void setUp() throws Exception {
        utilsService = new DefaultOsgiUtilsService(bundleContext);
        virtualConnectorProvider = mock(VirtualConnectorProvider.class);
        when(virtualConnectorProvider.createFactory(any(DomainProvider.class))).thenAnswer(
            new Answer<ConnectorInstanceFactory>() {
                @Override
                public ConnectorInstanceFactory answer(InvocationOnMock invocation) throws Throwable {
                    DomainProvider domainProvider = (DomainProvider) invocation.getArguments()[0];
                    return new DummyVirtualConnectorFactory(domainProvider);
                }
            });
        when(virtualConnectorProvider.getId()).thenReturn("virtual-test-connector");
        pseudoConnectorManager = new VirtualConnectorManager(bundleContext);
    }

    @After
    public void tearDown() throws Exception {
        pseudoConnectorManager.stop();
    }

    @Test
    public void testRegisterDomainProvider_shouldRegisterFactory() throws Exception {
        pseudoConnectorManager.start();
        registerService(virtualConnectorProvider, new Hashtable<String, Object>(), VirtualConnectorProvider.class);
        createDomainProviderMock(NullDomain.class, "test");
        Filter filter =
            FilterUtils.makeFilter(ConnectorInstanceFactory.class,
                "(&(domain=test)(connector=virtual-test-connector))");
        utilsService.getService(filter, 500);
    }

    @Test
    public void testRegisterVirtualProvider_shouldRegisterFactory() throws Exception {
        pseudoConnectorManager.start();
        createDomainProviderMock(NullDomain.class, "test");
        registerService(virtualConnectorProvider, new Hashtable<String, Object>(), VirtualConnectorProvider.class);
        Filter filter =
            FilterUtils.makeFilter(ConnectorInstanceFactory.class,
                "(&(domain=test)(connector=virtual-test-connector))");
        utilsService.getService(filter, 500);
    }

    @Test
    public void registerVirtualAndDomainProviderAndStartManagerLater_shouldRegisterFactory() throws Exception {
        createDomainProviderMock(NullDomain.class, "test");
        registerService(virtualConnectorProvider, new Hashtable<String, Object>(), VirtualConnectorProvider.class);
        pseudoConnectorManager.start();
        Filter filter =
            FilterUtils.makeFilter(ConnectorInstanceFactory.class,
                "(&(domain=test)(connector=virtual-test-connector))");
        utilsService.getService(filter, 500);
    }
}
