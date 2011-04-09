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
import org.openengsb.core.api.InternalServiceRegistrationManager;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.ServiceInstanceFactory;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.remote.CallRouter;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodReturn;
import org.openengsb.core.api.remote.MethodReturn.ReturnType;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullDomainImpl;
import org.osgi.framework.BundleContext;

public class ServiceRegistrationManagerTest extends AbstractOsgiMockServiceTest {

    private InternalServiceRegistrationManager serviceManager;
    private DefaultOsgiUtilsService serviceUtils;
    private CallRouter callrouter;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        registerMockedDomainProvider();
        registerMockedFactory();
        callrouter = mock(CallRouter.class);
        when(callrouter.callSync(anyString(), anyString(), any(MethodCall.class))).thenReturn(
            new MethodReturn(ReturnType.Void, null, null));
        registerService(callrouter, new Hashtable<String, Object>(), CallRouter.class);
        ServiceRegistrationManagerImpl serviceManagerImpl = new ServiceRegistrationManagerImpl();
        serviceManagerImpl.setBundleContext(bundleContext);
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

        NullDomain service = (NullDomain) serviceUtils.getService("(foo=bar)", 100L);
        assertThat(service.getInstanceId(), is(connectorId.getInstanceId()));
    }

    @Test
    public void testDeleteService_shouldUnregisterService() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("answer", "42");
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);

        ConnectorId connectorId = ConnectorId.generate("test", "testc");
        serviceManager.createService(connectorId, connectorDescription);
        serviceManager.delete(connectorId);
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
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);

        ConnectorId connectorId = ConnectorId.generate("test", "testc");
        serviceManager.createService(connectorId, connectorDescription);
        ConnectorDescription updated = new ConnectorDescription();
        Dictionary<String, Object> newProperties = new Hashtable<String, Object>();
        newProperties.put("foo", "xxx");
        updated.setProperties(newProperties);
        serviceManager.update(connectorId, updated);

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
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);

        ConnectorId connectorId = ConnectorId.generate("test", "testc");
        serviceManager.createService(connectorId, connectorDescription);
        ConnectorDescription updated = new ConnectorDescription();
        Map<String, String> newAttrs = new HashMap<String, String>();
        newAttrs.put("answer", "43");
        updated.setAttributes(newAttrs);
        serviceManager.update(connectorId, updated);

        serviceUtils.getService("(foo=bar)", 100L);
        ServiceInstanceFactory factory = serviceUtils.getService(ServiceInstanceFactory.class);
        verify(factory).updateServiceInstance(any(Domain.class), eq(newAttrs));
    }

    @Test
    public void testCreateProxy_shouldInvokeProxyFactory() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("portId", "jms+json");
        attributes.put("destination", "localhost");
        attributes.put("serviceId", "foo");
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription(attributes, properties);

        ConnectorId connectorId = ConnectorId.generate("test", Constants.EXTERNAL_CONNECTOR_PROXY);
        serviceManager.createService(connectorId, connectorDescription);

        NullDomain service = (NullDomain) serviceUtils.getService("(foo=bar)", 100L);
        service.nullMethod();
        verify(callrouter).callSync(eq("jms+json"), eq("localhost"), any(MethodCall.class));
        assertThat(service.getInstanceId(), is(connectorId.getInstanceId()));
    }

    @SuppressWarnings("unchecked")
    private void registerMockedFactory() {
        ServiceInstanceFactory factory = mock(ServiceInstanceFactory.class);
        when(factory.createServiceInstance(anyString(), any(Map.class))).thenAnswer(new Answer<Domain>() {
            @Override
            public Domain answer(InvocationOnMock invocation) throws Throwable {
                return new NullDomainImpl((String) invocation.getArguments()[0]);
            }
        });
        Hashtable<String, Object> factoryProps = new Hashtable<String, Object>();
        factoryProps.put("connector", "testc");
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
