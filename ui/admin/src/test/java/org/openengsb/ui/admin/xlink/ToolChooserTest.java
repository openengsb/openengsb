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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.core.Is.is;
import org.junit.After;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.verify;
import org.openengsb.connector.usernamepassword.internal.UsernamePasswordServiceImpl;
import org.openengsb.connector.wicketacl.WicketPermission;
import org.openengsb.connector.wicketacl.internal.WicketAclServiceImpl;
import org.openengsb.core.api.CompositeConnectorStrategy;
import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.DomainProvider;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.persistence.ConfigPersistenceService;
import org.openengsb.core.api.security.SecurityAttributeProvider;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.api.xlink.model.ModelToViewsTupel;
import org.openengsb.core.api.xlink.model.XLinkToolView;
import org.openengsb.core.common.SecurityAttributeProviderImpl;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.persistence.internal.DefaultConfigPersistenceService;
import org.openengsb.core.security.OpenEngSBShiroAuthenticator;
import org.openengsb.core.security.internal.AdminAccessConnector;
import org.openengsb.core.security.internal.AffirmativeBasedAuthorizationStrategy;
import org.openengsb.core.security.internal.SecurityInterceptor;
import org.openengsb.core.security.internal.model.RootPermission;
import org.openengsb.core.services.internal.ConnectorManagerImpl;
import org.openengsb.core.services.internal.ConnectorRegistrationManager;
import org.openengsb.core.services.internal.DefaultWiringService;
import org.openengsb.core.services.internal.virtual.CompositeConnectorProvider;
import org.openengsb.core.services.xlink.XLinkUtils;
import org.openengsb.core.test.DummyConfigPersistenceService;
import org.openengsb.core.test.UserManagerStub;
import org.openengsb.core.workflow.api.RuleManager;
import org.openengsb.domain.DomainModelSQL.DomainModelSQLDomain;
import org.openengsb.domain.auditing.AuditingDomain;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.openengsb.domain.authorization.AuthorizationDomain.Access;
import org.openengsb.labs.delegation.service.ClassProvider;
import org.openengsb.labs.delegation.service.internal.ClassProviderImpl;
import org.openengsb.ui.admin.AbstractUITest;
import org.openengsb.ui.admin.model.OpenEngSBFallbackVersion;
import org.openengsb.ui.admin.xlink.mocking.ExampleObjectOrientedModel;
import org.openengsb.ui.api.OpenEngSBVersionService;
import org.ops4j.pax.wicket.test.spring.ApplicationContextMock;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

public class ToolChooserTest extends AbstractUITest {
    
    private OsgiUtilsService mockedServiceUtils;
    private ApplicationContextMock customContext;
    private DomainModelSQLDomain connector;
    
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
        // although a new thread should be spawned because of the DedicatedThread @Rule, but we want to be really sure
        ThreadContext.unbindSecurityManager();
        ThreadContext.unbindSubject();
    }
    
    private void startSecurityManager(){
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
    
    private void mockOsgiService() throws Exception{
        mockedServiceUtils = mock(OsgiUtilsService.class);
        ModelRegistry registry = mock(ModelRegistry.class);
        when(mockedServiceUtils.getService(ModelRegistry.class)).thenReturn(registry);
        Class clazz = ExampleObjectOrientedModel.class;
        when(registry.loadModel(isA(ModelDescription.class))).thenReturn(clazz);  
        connector = mock(DomainModelSQLDomain.class);
        when(mockedServiceUtils.getService("(service.pid=test2+test2+test2)", 100L)).thenReturn(connector);
    }
    
    private void customMockContext() throws UserExistsException{
        customContext = new ApplicationContextMock();
        customContext.putBean("osgiUtilsService", mockedServiceUtils);
        customContext.putBean("serviceManager", serviceManager);      
    }

    private void setupTesterWithSpringMockContext() {
        tester.getApplication().getComponentInstantiationListeners().remove(defaultPaxWicketInjector);
        tester.getApplication().getComponentInstantiationListeners()
            .add(new PaxWicketSpringBeanComponentInjector(tester.getApplication(), customContext));
    }   
    
   public void mockRegistrationOfTools(){
        String hostId = "localhost";
                
        String toolName_A = "Tool A";        
        String connectorId_A = "test1+test1+test1";
        
        String toolName_B = "Tool B";
        String connectorId_B = "test2+test2+test2";       
        //test2%2Btest2%2Btest2
        
        registerTool_ExampleObjectOrientedModel(hostId, toolName_A, connectorId_A);
        registerTool_ExampleObjectOrientedModel(hostId, toolName_B, connectorId_B);        
    }
    
    private void registerTool_ExampleObjectOrientedModel(String hostId, String toolName, String connectorId) {
        
        ModelToViewsTupel[] modelsToViews 
            = new ModelToViewsTupel[1];  
        String viewId_ExampleObjectOrientedModel_1 = "viewId_ExampleObjectOrientedModel_1";
        String viewId_ExampleObjectOrientedModel_2 = "viewId_ExampleObjectOrientedModel_2";
        
        HashMap<String, String> descriptions  = new HashMap<String, String>();
        descriptions.put("en", "This is an ExampleObjectOrientedModel view.");
        descriptions.put("de", "Das ist eine ExampleObjectOrientedModel view.");
        
        List<XLinkToolView> views = new ArrayList<XLinkToolView>();
        views.add(new XLinkToolView(viewId_ExampleObjectOrientedModel_1, "View 1", descriptions));
        views.add(new XLinkToolView(viewId_ExampleObjectOrientedModel_2, "View 2", descriptions));          
        
        modelsToViews[0] = 
                new ModelToViewsTupel(
                        new ModelDescription(
                                ExampleObjectOrientedModel.class.getName(),
                                "3.0.0.SNAPSHOT")
                        , views);

        serviceManager.connectToXLink(connectorId, hostId, toolName, modelsToViews);       
    }      
    
    private void setupCommonXLinkParams(PageParameters params) {
        params.add(XLinkUtils.XLINK_EXPIRATIONDATE_KEY, getExpirationDate(3));
        params.add(XLinkUtils.XLINK_MODELCLASS_KEY, ExampleObjectOrientedModel.class.getName());
        params.add(XLinkUtils.XLINK_VERSION_KEY, "3.0.0.SNAPSHOT");
        params.add(XLinkUtils.XLINK_CONTEXTID_KEY, "ExampleContext");         
    }
    
    private void setupLocalSwitchXLinkParams(PageParameters params) {
        params.add(XLinkUtils.XLINK_CONNECTORID_KEY, "test2+test2+test2");
        params.add(XLinkUtils.XLINK_VIEW_KEY, "viewId_ExampleObjectOrientedModel_2");        
    }    
    
    private void setupIdentfierParamsForExampleOOModel(PageParameters params) {
        ObjectMapper mapper = new ObjectMapper();
        ExampleObjectOrientedModel model = new ExampleObjectOrientedModel();
        model.setOOClassName("testClass");
        model.setOOMethodName("testMethod");
        model.setOOPackageName("testPackage");
        String identifyingString = null;
        try {
            identifyingString = mapper.writeValueAsString(model);
        } catch (IOException ex) {
            Logger.getLogger(ToolChooserTest.class.getName()).log(Level.SEVERE, null, ex);
        } 
        
        params.add(XLinkUtils.XLINK_IDENTIFIER_KEY,identifyingString);    
    }    
    
    private void setupNessecaryHeader() {
        tester.addRequestHeader(XLinkUtils.XLINK_HOST_HEADERNAME, "localhost");
    }
    
    private String getExpirationDate(int futureDays) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, futureDays);
        Format formatter = new SimpleDateFormat(XLinkUtils.DATEFORMAT);
        return formatter.format(calendar.getTime());
    }    
    
    @Test
    public void openToolChooserPage_isRenderedWithSuccessfullLink() {
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
    public void openToolChooserPage_missingGetParam_Version() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove(XLinkUtils.XLINK_VERSION_KEY);
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains(XLinkUtils.XLINK_VERSION_KEY);
    }
  
    @Test    
    public void openToolChooserPage_missingGetParam_Date() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove(XLinkUtils.XLINK_EXPIRATIONDATE_KEY);
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains(XLinkUtils.XLINK_EXPIRATIONDATE_KEY);
    }
    
    @Test
    public void openToolChooserPage_missingGetParam_ModelClass() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove(XLinkUtils.XLINK_MODELCLASS_KEY);
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains(XLinkUtils.XLINK_MODELCLASS_KEY);
    }
    
    @Test   
    public void openToolChooserPage_missingGetParam_Context() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove(XLinkUtils.XLINK_CONTEXTID_KEY);
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains(XLinkUtils.XLINK_CONTEXTID_KEY);
    }      
    
    @Test    
    public void openToolChooserPage_missingIdentifier() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupNessecaryHeader();
        
        params.remove(XLinkUtils.XLINK_IDENTIFIER_KEY);
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(UserResponsePage.class);
        tester.assertContains(XLinkUtils.XLINK_IDENTIFIER_KEY);
    }   
    
    @Test
    public void openLocalSwitchPage_isRenderedWithSuccessfullLink() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupLocalSwitchXLinkParams(params);
        setupNessecaryHeader();
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(MachineResponsePage.class);
        assertThat(tester.getLastResponse().getStatus(),is(tester.getLastResponse().SC_OK));
        verify(connector).openXLinks(anyList(), anyString());
    }    
    
    @Test
    public void openLocalSwitchPage_WrongConnectorId() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupLocalSwitchXLinkParams(params);
        setupNessecaryHeader();
        
        params.remove(XLinkUtils.XLINK_CONNECTORID_KEY);
        params.add(XLinkUtils.XLINK_CONNECTORID_KEY, "test3+test3+test3");
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(MachineResponsePage.class);
        tester.assertContains("ConnectorId");
        assertThat(tester.getLastResponse().getStatus(),is(tester.getLastResponse().SC_BAD_REQUEST));
    }
    
    @Test
    public void openLocalSwitchPage_WrongViewId() {
        PageParameters params = new PageParameters();
        setupCommonXLinkParams(params);
        setupIdentfierParamsForExampleOOModel(params);
        setupLocalSwitchXLinkParams(params);
        setupNessecaryHeader();
        
        params.remove(XLinkUtils.XLINK_VIEW_KEY);
        params.add(XLinkUtils.XLINK_VIEW_KEY, "exampleViewId_wrong");   
        
        tester.startPage(ToolChooserPage.class, params);
        tester.assertRenderedPage(MachineResponsePage.class);
        tester.assertContains("ViewId");
        assertThat(tester.getLastResponse().getStatus(),is(tester.getLastResponse().SC_BAD_REQUEST));
    }   
    
}
