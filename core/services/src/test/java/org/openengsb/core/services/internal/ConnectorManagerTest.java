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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
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
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.common.CorePersistenceServiceBackend;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.DummyPersistenceManager;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullDomainImpl;
import org.osgi.framework.BundleContext;

public class ConnectorManagerTest extends AbstractOsgiMockServiceTest {

    private DefaultOsgiUtilsService serviceUtils;
    private ConnectorManager serviceManager;
    private ConnectorRegistrationManagerImpl serviceRegistrationManagerImpl;
    private ConnectorInstanceFactory factory;

    @Before
    public void setUp() throws Exception {
        registerMockedDomainProvider();
        registerMockedFactory();
        registerConfigPersistence();
        serviceRegistrationManagerImpl = new ConnectorRegistrationManagerImpl();
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
        ConnectorManagerImpl serviceManagerImpl = new ConnectorManagerImpl();
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
        serviceManager.create(connectorId, connectorDescription);

        serviceUtils.getService("(foo=bar)", 100L);
    }

    @Test
    public void testUpdateService_shouldUpdateInstance() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("answer", "42");
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);

        ConnectorId connectorId = ConnectorId.generate("test", "testc");
        serviceManager.create(connectorId, connectorDescription);

        connectorDescription.getProperties().put("foo", "42");
        connectorDescription.getAttributes().put("answer", "43");
        serviceManager.update(connectorId, connectorDescription);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateServiceWithInvalidAttributes_shouldFail() throws Exception {
        Map<String, String> errorMessages = new HashMap<String, String>();
        errorMessages.put("all", "because I don't like you");
        when(factory.getValidationErrors(anyMap())).thenReturn(errorMessages);
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("answer", "42");
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);

        ConnectorId connectorId = ConnectorId.generate("test", "testc");
        try {
            serviceManager.create(connectorId, connectorDescription);
            fail("Exception expected");
        } catch (ConnectorValidationFailedException e) {
            assertThat(e.getErrorMessages(), is(errorMessages));
        }

        try {
            serviceUtils.getService(NullDomain.class, 100L);
            fail("service is available, but shouldn't be");
        } catch (OsgiServiceNotAvailableException e) {
            // expected
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateServiceWithInvalidAttributes_shouldLeaveServiceUnchanged() throws Exception {
        Map<String, String> errorMessages = new HashMap<String, String>();
        errorMessages.put("all", "because I don't like you");
        when(factory.getValidationErrors(any(Domain.class), anyMap())).thenReturn(errorMessages);

        Map<String, String> attributes = new HashMap<String, String>();
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);

        ConnectorId connectorId = ConnectorId.generate("test", "testc");
        serviceManager.create(connectorId, connectorDescription);
        serviceUtils.getService("(foo=bar)", 1L);

        connectorDescription.getProperties().put("foo", "42");
        try {
            serviceManager.update(connectorId, connectorDescription);
            fail("Exception expected");
        } catch (ConnectorValidationFailedException e) {
            assertThat(e.getErrorMessages(), is(errorMessages));
        }

        try {
            serviceUtils.getService("(foo=bar)", 1L);
        } catch (OsgiServiceNotAvailableException e) {
            fail("Service is not available with the old attributes");
        }

        try {
            serviceUtils.getService("(foo=42)", 1L);
            fail("Service should not be available with the new properties, but it is");
        } catch (OsgiServiceNotAvailableException e) {
            // expected
        }
    }

    @Test
    public void testDeleteService_shouldNotBeAvailableAnymore() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);

        ConnectorId connectorId = ConnectorId.generate("test", "testc");
        serviceManager.create(connectorId, connectorDescription);

        serviceManager.delete(connectorId);

        try {
            serviceUtils.getService("(foo=bar)", 100L);
            fail("service should not be available anymore");
        } catch (OsgiServiceNotAvailableException e) {
            // expected
        }

        try {
            serviceManager.getAttributeValues(connectorId);
            fail("service was still in persistence after deletion");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    private void registerMockedFactory() throws Exception {
        factory = mock(ConnectorInstanceFactory.class);
        when(factory.createNewInstance(anyString())).thenReturn(new NullDomainImpl());
        Hashtable<String, Object> factoryProps = new Hashtable<String, Object>();
        factoryProps.put(Constants.CONNECTOR, "testc");
        registerService(factory, factoryProps, ConnectorInstanceFactory.class);
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
