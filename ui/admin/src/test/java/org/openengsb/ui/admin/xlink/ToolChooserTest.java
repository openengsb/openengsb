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

package org.openengsb.ui.admin.xlink;

import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.aop.MethodInvocation;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ThreadContext;
import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.api.xlink.internal.ui.ToolChooserLogic;
import org.openengsb.core.api.xlink.internal.ui.XLinkMock;
import org.openengsb.core.api.xlink.model.ModelToViewsTuple;
import org.openengsb.core.api.xlink.model.XLinkConnectorView;
import org.openengsb.core.api.xlink.model.XLinkConstants;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.services.internal.ConnectorManagerImpl;
import org.openengsb.core.services.internal.security.model.ShiroContext;
import org.openengsb.domain.SQLCode.SQLCodeDomain;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.openengsb.domain.authorization.AuthorizationDomain.Access;
import org.openengsb.ui.admin.AbstractUITest;
import org.openengsb.ui.admin.xlink.mocking.ExampleObjectOrientedModel;
import org.openengsb.ui.admin.xlink.mocking.XLinkMockImpl;
import org.ops4j.pax.wicket.test.spring.ApplicationContextMock;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

public class ToolChooserTest extends AbstractUITest {
    
    private OsgiUtilsService mockedServiceUtils;
    private ApplicationContextMock customContext;
    private ToolChooserLogic toolChooserLogic;
    private XLinkMock xLinkMock;
    private SQLCodeDomain connector;
    
    @Before
    public void setup() throws Exception {
        mockOsgiService();
        startSecurityManager();
        customMockContext();
        setupTesterWithSpringMockContext();
        mockRegistrationOfTools();
        
    }
    
    @After
    public void cleanupShiro() {
        // although a new thread should be spawned because of the 
        // DedicatedThread @Rule, but we want to be really sure
        ThreadContext.unbindSecurityManager();
        ThreadContext.unbindSubject();
    }
    
    private void startSecurityManager() {
        DefaultSecurityManager sm = new DefaultSecurityManager();
        sm.setAuthenticator(new Authenticator() {
            @Override
            public AuthenticationInfo authenticate(AuthenticationToken authenticationToken)
                throws AuthenticationException {
                return new SimpleAuthenticationInfo(new SimplePrincipalCollection(authenticationToken.getPrincipal(),
                    "openengsb"), authenticationToken.getCredentials());
            }
        });
        SecurityUtils.setSecurityManager(sm);
        ThreadContext.bind(sm);

        AuthorizationDomain authorizer = mock(AuthorizationDomain.class);
        when(authorizer.checkAccess(eq("admin"), any(MethodInvocation.class))).thenReturn(Access.GRANTED);

    }
    
    private void mockOsgiService() throws Exception {
        mockedServiceUtils = mock(OsgiUtilsService.class);
        ModelRegistry registry = mock(ModelRegistry.class);
        when(mockedServiceUtils.getService(ModelRegistry.class)).thenReturn(registry);
        Class clazz = ExampleObjectOrientedModel.class;
        when(registry.loadModel(isA(ModelDescription.class))).thenReturn(clazz);  
        connector = mock(SQLCodeDomain.class);
        when(mockedServiceUtils.getService("(service.pid=test2+test2+test2)", 100L)).thenReturn(connector);
        when(mockedServiceUtils.getService("(service.pid=test1+test1+test1)", 100L)).thenReturn(connector);
    }
    
    private void customMockContext() throws UserExistsException {
        ((ConnectorManagerImpl) serviceManager).setUtilsService(mockedServiceUtils);
        xLinkMock = new XLinkMockImpl(mockedServiceUtils, new ShiroContext());
        toolChooserLogic = new ToolChooserLogicImpl((ConnectorManagerImpl)serviceManager);
        customContext = new ApplicationContextMock();
        customContext.putBean("osgiUtilsService", mockedServiceUtils);
        customContext.putBean("toolChooserLogic", toolChooserLogic);     
        customContext.putBean("xLinkMock", xLinkMock);
    }

    private void setupTesterWithSpringMockContext() {
        tester.getApplication().getComponentInstantiationListeners().remove(defaultPaxWicketInjector);
        tester.getApplication().getComponentInstantiationListeners()
            .add(new PaxWicketSpringBeanComponentInjector(tester.getApplication(), customContext));
    }   
    
    public void mockRegistrationOfTools() throws Exception {
        String hostId = "localhost";
                
        String toolNameA = "Tool A";        
        String connectorIdA = "test1+test1+test1";
        
        String toolNameB = "Tool B";
        String connectorIdB = "test2+test2+test2";       
        //test2%2Btest2%2Btest2
        
        registerTool_ExampleObjectOrientedModel(hostId, toolNameA, connectorIdA);
        registerTool_ExampleObjectOrientedModel(hostId, toolNameB, connectorIdB);        
    }
    
    private void registerTool_ExampleObjectOrientedModel(String hostId, String toolName, String connectorId) 
        throws Exception {
        
        ModelToViewsTuple[] modelsToViews 
            = new ModelToViewsTuple[1];  
        String viewIdExampleObjectOrientedModel1 = "viewId_ExampleObjectOrientedModel_1";
        String viewIdExampleObjectOrientedModel2 = "viewId_ExampleObjectOrientedModel_2";
        
        HashMap<String, String> descriptions  = new HashMap<String, String>();
        descriptions.put("en", "This is an ExampleObjectOrientedModel view.");
        descriptions.put("de", "Das ist eine ExampleObjectOrientedModel view.");
        
        List<XLinkConnectorView> views = new ArrayList<XLinkConnectorView>();
        views.add(new XLinkConnectorView(viewIdExampleObjectOrientedModel1, "View 1", descriptions));
        views.add(new XLinkConnectorView(viewIdExampleObjectOrientedModel2, "View 2", descriptions));          
        
        modelsToViews[0] = 
                new ModelToViewsTuple(
                        new ModelDescription(
                                ExampleObjectOrientedModel.class.getName(),
                                "3.0.0.SNAPSHOT")
                        , views);

        serviceManager.connectToXLink(connectorId, hostId, toolName, modelsToViews);       
    }      
    
    private void setupCommonXLinkParams(PageParameters params) {
        params.add(XLinkConstants.XLINK_EXPIRATIONDATE_KEY, getExpirationDate(3));
        params.add(XLinkConstants.XLINK_MODELCLASS_KEY, ExampleObjectOrientedModel.class.getName());
        params.add(XLinkConstants.XLINK_VERSION_KEY, "3.0.0.SNAPSHOT");
        params.add(XLinkConstants.XLINK_CONTEXTID_KEY, "ExampleContext");         
    }
    
    private void setupLocalSwitchXLinkParams(PageParameters params) {
        params.add(XLinkConstants.XLINK_CONNECTORID_KEY, "test2+test2+test2");
        params.add(XLinkConstants.XLINK_VIEW_KEY, "viewId_ExampleObjectOrientedModel_2");        
    }    
    
    private void setupIdentfierParamsForExampleOOModel(PageParameters params) {
        ObjectMapper mapper = new ObjectMapper();
        ExampleObjectOrientedModel model = new ExampleObjectOrientedModel();
        model.setOoClassName("testClass");
        model.setOoMethodName("testMethod");
        model.setOoPackageName("testPackage");
        String identifyingString = null;
        try {
            identifyingString = mapper.writeValueAsString(model);
        } catch (IOException ex) {
            Logger.getLogger(ToolChooserTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        params.add(XLinkConstants.XLINK_IDENTIFIER_KEY, identifyingString);    
    }    
    
    private void setupNessecaryHeader() {
        tester.addRequestHeader(XLinkConstants.XLINK_HOST_HEADERNAME, "localhost");
    }
    
    private String getExpirationDate(int futureDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, futureDays);
        Format formatter = new SimpleDateFormat(XLinkConstants.DATEFORMAT);
        return formatter.format(calendar.getTime());
    }    
    
    @Test
    public void testOpenToolChooserPage_withValidLink_isRenderedSuccessfull() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(ToolChooserPage.class);
        tester.assertContains("Tool B");
        tester.assertContains("View 2");
    }
    
    @Test   
    public void testOpenToolChooserPage_LinkMissingVersionParam_toUserErrorPage() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove(XLinkConstants.XLINK_VERSION_KEY);
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains(XLinkConstants.XLINK_VERSION_KEY);
    }
  
    @Test    
    public void testOpenToolChooserPage_LinkMissingDateParam_toUserErrorPage() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove(XLinkConstants.XLINK_EXPIRATIONDATE_KEY);
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains(XLinkConstants.XLINK_EXPIRATIONDATE_KEY);
    }
    
    @Test
    public void testOpenToolChooserPage_LinkMissingModelParam_toUserErrorPage() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove(XLinkConstants.XLINK_MODELCLASS_KEY);
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains(XLinkConstants.XLINK_MODELCLASS_KEY);
    }
    
    @Test   
    public void testOpenToolChooserPage_LinkMissingContextParam_toUserErrorPage() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove(XLinkConstants.XLINK_CONTEXTID_KEY);
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains(XLinkConstants.XLINK_CONTEXTID_KEY);
    }      
    
    @Test    
    public void testOpenToolChooserPage_LinkMissingIdentifierParam_toUserErrorPage() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove(XLinkConstants.XLINK_IDENTIFIER_KEY);
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains(XLinkConstants.XLINK_IDENTIFIER_KEY);
    }   
    
    @Test
    public void testOpenLocalSwitchPage_withValidLink_isRenderedSuccessfull() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupLocalSwitchXLinkParams(params);
        setupNessecaryHeader();
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(MachineResponsePage.class);
        assertThat(tester.getLastResponse().getStatus(), is(MockHttpServletResponse.SC_OK));
        verify(connector).openXLinks(any(Object[].class), anyString());
    }    
    
    @Test
    public void testOpenLocalSwitchPage_LinkHasWrongConnectorId_toMachineErrorPage() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupLocalSwitchXLinkParams(params);
        setupNessecaryHeader();
        
        params.remove(XLinkConstants.XLINK_CONNECTORID_KEY);
        params.add(XLinkConstants.XLINK_CONNECTORID_KEY, "test3+test3+test3");
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(MachineResponsePage.class);
        tester.assertContains("ConnectorId");
        assertThat(tester.getLastResponse().getStatus(), is(tester.getLastResponse().SC_BAD_REQUEST));
    }
    
    @Test
    public void testOpenLocalSwitchPage_LinkHasWrongViewId_toMachineErrorPage() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupLocalSwitchXLinkParams(params);
        setupNessecaryHeader();
        
        params.remove(XLinkConstants.XLINK_VIEW_KEY);
        params.add(XLinkConstants.XLINK_VIEW_KEY, "exampleViewId_wrong");   
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(MachineResponsePage.class);
        tester.assertContains("ViewId");
        assertThat(tester.getLastResponse().getStatus(), is(tester.getLastResponse().SC_BAD_REQUEST));
    }   
    
}
