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
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.ServiceInstanceFactory;
import org.openengsb.core.api.ServiceManager;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.DummyPersistenceManager;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullDomainImpl;
import org.osgi.framework.BundleContext;

public class ServiceManagerTest extends AbstractOsgiMockServiceTest {

    private DefaultOsgiUtilsService serviceUtils;
    private ServiceManager serviceManager;
    private ServiceRegistrationManagerImpl serviceRegistrationManagerImpl;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        registerMockedDomainProvider();
        registerMockedFactory();
        registerConfigPersistence();
        serviceRegistrationManagerImpl = new ServiceRegistrationManagerImpl();
        serviceRegistrationManagerImpl.setBundleContext(bundleContext);
        createServiceManager();
    }

    private void registerConfigPersistence() {
        final CorePersistenceServiceBackend persistenceBackend = new CorePersistenceServiceBackend();
        DummyPersistenceManager persistenceManager = new DummyPersistenceManager();
        persistenceBackend.setPersistenceManager(persistenceManager);
        persistenceBackend.setBundleContext(bundleContext);
        persistenceBackend.init();
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.CONFIGURATION_ID, Constants.CONNECTOR);
        props.put(Constants.BACKEND_ID, "dummy");
        registerService(new DefaultConfigPersistenceService(persistenceBackend), props, ConfigPersistenceService.class);
    }

    private void createServiceManager() {
        ServiceManagerImpl serviceManagerImpl = new ServiceManagerImpl();
        serviceManagerImpl.setRegistrationManager(serviceRegistrationManagerImpl);
        serviceManager = serviceManagerImpl;
    }

    @Test
    public void testCreateService_shouldCreateInstanceWithFactory() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("answer", "42");
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);

        ConnectorId connectorId = ConnectorId.generate("test", "testc");
        serviceManager.createService(connectorId, connectorDescription);

        serviceUtils.getService("(foo=bar)", 100L);
    }

    @SuppressWarnings("unchecked")
    private void registerMockedFactory() {
        ServiceInstanceFactory factory = mock(ServiceInstanceFactory.class);
        when(factory.createServiceInstance(anyString(), any(Map.class))).thenReturn(new NullDomainImpl());
        Hashtable<String, Object> factoryProps = new Hashtable<String, Object>();
        factoryProps.put(Constants.CONNECTOR, "testc");
        registerService(factory, factoryProps, ServiceInstanceFactory.class);
    }

    private void registerMockedDomainProvider() {
        DomainProvider domainProvider = mock(DomainProvider.class);
        when(domainProvider.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
            @Override
            public Class<? extends Domain> answer(InvocationOnMock invocation) throws Throwable {
                return NullDomain.class;
            }
        });
        Hashtable<String, Object> domainProviderProps = new Hashtable<String, Object>();
        domainProviderProps.put("domain", "test");
        registerService(domainProvider, domainProviderProps, DomainProvider.class);
    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        serviceUtils = new DefaultOsgiUtilsService();
        serviceUtils.setBundleContext(bundleContext);
        OpenEngSBCoreServices.setOsgiServiceUtils(serviceUtils);
        registerService(serviceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
    }
}
