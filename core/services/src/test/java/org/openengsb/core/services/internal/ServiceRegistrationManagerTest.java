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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.ConnectorRegistrationManager;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.VirtualConnectorProvider;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.OutgoingPortUtilService;
import org.openengsb.core.common.Activator;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.services.internal.virtual.ProxyConnectorProvider;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullDomainImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;

public class ServiceRegistrationManagerTest extends AbstractOsgiMockServiceTest {

    private ConnectorRegistrationManager registrationManager;
    private DefaultOsgiUtilsService serviceUtils;
    private OutgoingPortUtilService callrouter;

    @Before
    public void setUp() throws Exception {
        createDomainProviderMock(NullDomain.class, "test");
        createFactoryMock("testc", NullDomainImpl.class, "test");
        callrouter = mock(OutgoingPortUtilService.class);
        MethodResult result = MethodResult.newVoidResult();
        when(callrouter.sendMethodCallWithResult(anyString(), anyString(), any(MethodCall.class))).thenReturn(result);
        registerService(callrouter, new Hashtable<String, Object>(), OutgoingPortUtilService.class);
        ConnectorRegistrationManagerImpl serviceManagerImpl = new ConnectorRegistrationManagerImpl();
        serviceManagerImpl.setBundleContext(bundleContext);
        serviceManagerImpl.setServiceUtils(serviceUtils);
        registrationManager = serviceManagerImpl;
        ProxyConnectorProvider proxyConnectorProvider = new ProxyConnectorProvider();
        proxyConnectorProvider.setId(Constants.EXTERNAL_CONNECTOR_PROXY);
        registerService(proxyConnectorProvider, new Hashtable<String, Object>(), VirtualConnectorProvider.class);
        new Activator().start(bundleContext);
    }

    @Test
    public void testCreateService_shouldCreateInstanceWithFactory() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("answer", "42");
        Map<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);

        ConnectorId connectorId = ConnectorId.generate("test", "testc");
        registrationManager.updateRegistration(connectorId, connectorDescription);

        NullDomain service = (NullDomain) serviceUtils.getService("(foo=bar)", 100L);
        assertThat(service.getInstanceId(), is(connectorId.toString()));
    }

    @Test
    public void testDeleteService_shouldUnregisterService() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("answer", "42");
        Map<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);

        ConnectorId connectorId = ConnectorId.generate("test", "testc");
        registrationManager.updateRegistration(connectorId, connectorDescription);
        registrationManager.remove(connectorId);
        try {
            serviceUtils.getService("(foo=bar)", 100L);
            fail("service was expected to be not available");
        } catch (OsgiServiceNotAvailableException e) {
            // expected
        }
    }

    @Test
    public void testUpdateProperties_shouldMatchNewFilter() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("answer", "42");
        Map<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);

        ConnectorId connectorId = ConnectorId.generate("test", "testc");
        registrationManager.updateRegistration(connectorId, connectorDescription);
        ConnectorDescription updated = new ConnectorDescription();
        Map<String, Object> newProperties = new Hashtable<String, Object>();
        newProperties.put("foo", "xxx");
        updated.setProperties(newProperties);
        registrationManager.updateRegistration(connectorId, updated);

        serviceUtils.getService("(foo=xxx)", 100L);
        try {
            serviceUtils.getService("(foo=bar)", 100L);
            fail("service was expected to be not available");
        } catch (OsgiServiceNotAvailableException e) {
            // expected
        }
    }

    @Test
    public void testUpdateAttributes_shouldChangeInstance() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("answer", "42");
        Map<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);

        ConnectorId connectorId = ConnectorId.generate("test", "testc");
        registrationManager.updateRegistration(connectorId, connectorDescription);
        ConnectorDescription updated = new ConnectorDescription();
        Map<String, String> newAttrs = new HashMap<String, String>();
        newAttrs.put("answer", "43");
        updated.setAttributes(newAttrs);
        updated.setProperties(properties);
        registrationManager.updateRegistration(connectorId, updated);

        serviceUtils.getService("(foo=bar)", 100L);
        Filter filter = serviceUtils.makeFilter(ConnectorInstanceFactory.class, "(connector=testc)");
        ConnectorInstanceFactory factory = (ConnectorInstanceFactory) serviceUtils.getService(filter);
        verify(factory).applyAttributes(any(Domain.class), eq(newAttrs));
    }

    @Test
    public void testCreateProxy_shouldInvokeProxyFactory() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("portId", "jms+json");
        attributes.put("destination", "localhost");
        attributes.put("serviceId", "foo");
        Map<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);

        ConnectorId connectorId = ConnectorId.generate("test", Constants.EXTERNAL_CONNECTOR_PROXY);
        registrationManager.updateRegistration(connectorId, connectorDescription);

        NullDomain service = (NullDomain) serviceUtils.getService("(foo=bar)", 100L);
        service.nullMethod();
        verify(callrouter).sendMethodCallWithResult(eq("jms+json"), eq("localhost"), any(MethodCall.class));
        assertThat(service.getInstanceId(), is(connectorId.toString()));
    }

    // private void registerMockedDomainProvider() {
    // DomainProvider domainProvider = mock(DomainProvider.class);
    // when(domainProvider.getDomainInterface()).thenAnswer(new Answer<Class<? extends Domain>>() {
    // @Override
    // public Class<? extends Domain> answer(InvocationOnMock invocation) throws Throwable {
    // return NullDomain.class;
    // }
    // });
    // when(domain
    // Hashtable<String, Object> domainProviderProps = new Hashtable<String, Object>();
    // domainProviderProps.put(Constants.DOMAIN_KEY, "test");
    // registerService(domainProvider, domainProviderProps, DomainProvider.class);
    // }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        serviceUtils = new DefaultOsgiUtilsService();
        serviceUtils.setBundleContext(bundleContext);
        OpenEngSBCoreServices.setOsgiServiceUtils(serviceUtils);
        registerService(serviceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
    }
}
