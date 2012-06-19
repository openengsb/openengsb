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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.ConnectorValidationFailedException;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.ekb.ModelDescription;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.xlink.model.XLinkTemplate;
import org.openengsb.core.api.xlink.model.XLinkToolView;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.common.xlink.ExampleObjectOrientedModel;
import org.openengsb.core.persistence.internal.CorePersistenceServiceBackend;
import org.openengsb.core.persistence.internal.DefaultConfigPersistenceService;
import org.openengsb.core.persistence.internal.DefaultPersistenceManager;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullDomainImpl;

public class ConnectorManagerTest extends AbstractOsgiMockServiceTest {

    private DefaultOsgiUtilsService serviceUtils;
    private ConnectorManager serviceManager;
    private ConnectorRegistrationManager serviceRegistrationManagerImpl;
    private ConnectorInstanceFactory factory;
    private DefaultConfigPersistenceService configPersistence;

    @Before
    public void setUp() throws Exception {
        registerMockedDomainProvider();
        registerMockedFactory();
        registerConfigPersistence();
        serviceRegistrationManagerImpl = new ConnectorRegistrationManager();
        serviceRegistrationManagerImpl.setBundleContext(bundleContext);
        serviceUtils = new DefaultOsgiUtilsService(bundleContext);
        createServiceManager();
    }

    private void registerConfigPersistence() {
        final CorePersistenceServiceBackend<String> persistenceBackend = new CorePersistenceServiceBackend<String>();
        DefaultPersistenceManager persistenceManager = new DefaultPersistenceManager();
        persistenceManager.setPersistenceRootDir("target/" + UUID.randomUUID().toString());
        persistenceBackend.setPersistenceManager(persistenceManager);
        persistenceBackend.setBundleContext(bundleContext);
        persistenceBackend.init();
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.CONFIGURATION_ID, Constants.CONFIG_CONNECTOR);
        props.put(Constants.BACKEND_ID, "dummy");
        configPersistence = new DefaultConfigPersistenceService(persistenceBackend);
        registerService(configPersistence, props, ConfigPersistenceService.class);
    }

    private void createServiceManager() {
        ConnectorManagerImpl serviceManagerImpl = new ConnectorManagerImpl();
        serviceManagerImpl.setRegistrationManager(serviceRegistrationManagerImpl);
        serviceManagerImpl.setConfigPersistence(configPersistence);
        serviceManagerImpl.setxLinkBaseUrl("http://localhost/openXLink");
        serviceManagerImpl.setxLinkExpiresIn(3);
        serviceManager = serviceManagerImpl;
    }

    @Test
    public void testCreateService_shouldCreateInstanceWithFactory() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("answer", "42");
        Map<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription("test", "testc", attributes, properties);

        serviceManager.create(connectorDescription);

        serviceUtils.getService("(foo=bar)", 100L);
    }

    @Test
    public void testUpdateService_shouldUpdateInstance() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("answer", "42");
        Map<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription("test", "testc", attributes, properties);

        String uuid = serviceManager.create(connectorDescription);

        connectorDescription.getProperties().put("foo", "42");
        connectorDescription.getAttributes().put("answer", "43");
        serviceManager.update(uuid, connectorDescription);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateServiceWithInvalidAttributes_shouldFail() throws Exception {
        Map<String, String> errorMessages = new HashMap<String, String>();
        errorMessages.put("all", "because I don't like you");
        when(factory.getValidationErrors(anyMap())).thenReturn(errorMessages);
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("answer", "42");
        Map<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription("test", "testc", attributes, properties);

        try {
            serviceManager.create(connectorDescription);
            fail("Exception expected");
        } catch (ConnectorValidationFailedException e) {
            assertThat(e.getErrorMessages(), is(errorMessages));
        }

        try {
            serviceUtils.getService(NullDomain.class, 100L);
            fail("service is available, but shouldn't be");
        } catch (OsgiServiceNotAvailableException e) {
            // expected. No service should be available because the attributes were invalid
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testForceCreateServiceWithInvalidAttributes_shouldCreateConnector() throws Exception {
        Map<String, String> errorMessages = new HashMap<String, String>();
        errorMessages.put("all", "because I don't like you");
        when(factory.getValidationErrors(anyMap())).thenReturn(errorMessages);
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put("answer", "42");
        Map<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription("test", "testc", attributes, properties);

        serviceManager.forceCreate(connectorDescription);

        try {
            serviceUtils.getService("(foo=bar)", 100L);
        } catch (OsgiServiceNotAvailableException e) {
            fail("service should be available because validation should have been skipped");
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateServiceWithInvalidAttributes_shouldLeaveServiceUnchanged() throws Exception {
        Map<String, String> errorMessages = new HashMap<String, String>();
        errorMessages.put("all", "because I don't like you");
        when(factory.getValidationErrors(any(Connector.class), anyMap())).thenReturn(errorMessages);

        Map<String, String> attributes = new HashMap<String, String>();
        Map<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription("test", "testc", attributes, properties);

        String connectorId = serviceManager.create(connectorDescription);
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
            // expected. The properties should not have been updated, so no service is available.
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testForceUpdateServiceWithInvalidAttributes_shouldUpdateService() throws Exception {
        Map<String, String> errorMessages = new HashMap<String, String>();
        errorMessages.put("all", "because I don't like you");
        when(factory.getValidationErrors(any(Connector.class), anyMap())).thenReturn(errorMessages);

        Map<String, String> attributes = new HashMap<String, String>();
        Map<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription("test", "testc", attributes, properties);

        String connectorId = serviceManager.create(connectorDescription);
        serviceUtils.getService("(foo=bar)", 1L);

        connectorDescription.getProperties().put("foo", "42");
        serviceManager.forceUpdate(connectorId, connectorDescription);

        try {
            serviceUtils.getService("(foo=bar)", 1L);
            fail("Service is only available with the old attributes");
        } catch (OsgiServiceNotAvailableException e) {
            // expected. The attributes have been overwritten
        }

        try {
            serviceUtils.getService("(foo=42)", 1L);
        } catch (OsgiServiceNotAvailableException e) {
            fail("Service should be available with the new properties, since validation should have been skipped");
        }
    }

    @Test
    public void testDeleteService_shouldNotBeAvailableAnymore() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        Map<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        ConnectorDescription connectorDescription = new ConnectorDescription("test", "testc", attributes, properties);

        String connectorId = serviceManager.create(connectorDescription);

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
    
    @Test
    public void testConnectToXLink_ReturnsTemplate() {
        String connectorId = "test+test+test";
        String hostId = "127.0.0.1";
        String toolName = "myTool";
        HashMap<ModelDescription, List<XLinkToolView>> modelsToViews
            = createModelViewsMap(toolName);
        XLinkTemplate template 
            = serviceManager.connectToXLink(connectorId, hostId, toolName, modelsToViews);
        assertNotNull(template);
    }
    
    @Test
    public void testConnectToXLink_TemplateContainsCorrectIdentifier() {
        String connectorId = "test+test+test";
        String hostId = "127.0.0.1";
        String toolName = "myTool";
        HashMap<ModelDescription, List<XLinkToolView>> modelsToViews
            = createModelViewsMap(toolName);
        XLinkTemplate template 
            = serviceManager.connectToXLink(connectorId, hostId, toolName, modelsToViews);
        assertTrue(template.getConnectorId().contains(urlEncodeParameter(connectorId)));
    }    
    
    private static String urlEncodeParameter(String parameter) {
        try {
            return URLEncoder.encode(parameter, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
        }
        return parameter;
    }    
    
    @Test
    public void testConnectToXLink_TemplateViewToModels_ContainsAllViews() {
        String connectorId = "test+test+test";
        String hostId = "127.0.0.1";
        String toolName = "myTool";
        String viewId1 = "exampleViewId_1";
        String viewId2 = "exampleViewId_2";
        HashMap<ModelDescription, List<XLinkToolView>> modelsToViews
            = createModelViewsMap(toolName);
        XLinkTemplate template 
            = serviceManager.connectToXLink(connectorId, hostId, toolName, modelsToViews);
        assertNotNull(template.getViewToModels().get(viewId1));
        assertNotNull(template.getViewToModels().get(viewId2));
    }   
    
    @Test
    public void testGetXLinkRegistration_isEmptyOnInitial() {
        String hostId = "127.0.0.1";
        assertTrue(serviceManager.getXLinkRegistration(hostId).isEmpty());
    } 
   
    @Test
    public void testGetXLinkRegistration_returnsConnectedRegistration() {
        String connectorId = "test+test+test";
        String hostId = "127.0.0.1";
        String toolName = "myTool";
        HashMap<ModelDescription, List<XLinkToolView>> modelsToViews
            = createModelViewsMap(toolName);
        serviceManager.connectToXLink(connectorId, hostId, toolName, modelsToViews);
        assertFalse(serviceManager.getXLinkRegistration(hostId).isEmpty());
    }     
    
    @Test
    public void testGetXLinkRegistration_returnsToolRegistrationGlobals() {
        String connectorId = "test+test+test";
        String hostId = "127.0.0.1";
        String toolName = "myTool";
        HashMap<ModelDescription, List<XLinkToolView>> modelsToViews
            = createModelViewsMap(toolName);
        XLinkTemplate template 
            = serviceManager.connectToXLink(connectorId, hostId, toolName, modelsToViews);
        assertThat(serviceManager.getXLinkRegistration(hostId).get(0).getHostId(), is(hostId));
        assertThat(serviceManager.getXLinkRegistration(hostId).get(0).getConnectorId(), is(connectorId));
        assertThat(serviceManager.getXLinkRegistration(hostId).get(0).getToolName(), is(toolName));
    }     
    
    @Test
    public void testGetXLinkRegistration_returnsToolRegistrationTemplate() {
        String connectorId = "test+test+test";
        String hostId = "127.0.0.1";
        String toolName = "myTool";
        String viewId1 = "exampleViewId_1";
        String viewId2 = "exampleViewId_2";
        HashMap<ModelDescription, List<XLinkToolView>> modelsToViews
            = createModelViewsMap(toolName);
        XLinkTemplate template 
            = serviceManager.connectToXLink(connectorId, hostId, toolName, modelsToViews);
        assertTrue(template.getConnectorId().contains(urlEncodeParameter(connectorId)));
        assertNotNull(template.getViewToModels().get(viewId1));
        assertNotNull(template.getViewToModels().get(viewId2));
    }     
    
    @Test
    public void testDisconnectFromXLink_OnMissingRegistration_NoFail() {
        String connectorId = "test+test+test";
        String hostId = "127.0.0.1";
        serviceManager.disconnectFromXLink(connectorId, hostId);
    }         
    
    @Test
    public void testDisconnectFromXLink_isEmptyAfterDisconnect() {
        String connectorId = "test+test+test";
        String hostId = "127.0.0.1";
        String toolName = "myTool";
        HashMap<ModelDescription, List<XLinkToolView>> modelsToViews
            = createModelViewsMap(toolName);
        serviceManager.connectToXLink(connectorId, hostId, toolName, modelsToViews);
        serviceManager.disconnectFromXLink(connectorId, hostId);
        assertTrue(serviceManager.getXLinkRegistration(hostId).isEmpty());
    }     
    
    private HashMap<ModelDescription, List<XLinkToolView>> createModelViewsMap(String toolName) {
        String viewId1 = "exampleViewId_1";
        String viewId2 = "exampleViewId_2";
        HashMap<ModelDescription, List<XLinkToolView>> modelsToViews 
            = new HashMap<ModelDescription, List<XLinkToolView>>();  
        HashMap<String, String> descriptions  = new HashMap<String, String>();
        List<XLinkToolView> views = new ArrayList<XLinkToolView>();
        
        descriptions.put("en", "This is a demo view.");
        descriptions.put("de", "Das ist eine demonstration view.");
        views = new ArrayList();
        views.add(new XLinkToolView(viewId1, toolName, descriptions));
        views.add(new XLinkToolView(viewId2, toolName, descriptions));        
        
        modelsToViews.put(new ModelDescription(ExampleObjectOrientedModel.class.getName(), "3.0.0.SNAPSHOT"), views);
        return modelsToViews;
    }

    @Test
    public void createConnectorWithSkipDomainType_shouldNotInvokeSetDomainType() throws Exception {
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(Constants.SKIP_SET_DOMAIN_TYPE, "true");
        Map<String, Object> properties = new Hashtable<String, Object>();
        properties.put("foo", "bar");
        NullDomainImpl mock2 = mock(NullDomainImpl.class);
        when(factory.createNewInstance(anyString())).thenReturn(mock2);
        ConnectorDescription connectorDescription = new ConnectorDescription("test", "testc", attributes, properties);
        serviceManager.create(connectorDescription);
        verify(mock2, never()).setDomainId(anyString());
        verify(mock2, never()).setConnectorId(anyString());
    }

    private void registerMockedFactory() throws Exception {
        factory = mock(ConnectorInstanceFactory.class);
        when(factory.createNewInstance(anyString())).thenReturn(new NullDomainImpl());
        Hashtable<String, Object> factoryProps = new Hashtable<String, Object>();
        factoryProps.put(Constants.CONNECTOR_KEY, "testc");
        factoryProps.put(Constants.DOMAIN_KEY, "test");
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
}
