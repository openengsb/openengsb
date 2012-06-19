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

package org.openengsb.core.common.xlink;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.isA;

import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.ekb.ModelDescription;
import org.openengsb.core.api.ekb.ModelRegistry;
import org.openengsb.core.api.xlink.model.XLinkLocalTool;
import org.openengsb.core.api.xlink.model.XLinkTemplate;
import org.openengsb.core.api.xlink.model.XLinkToolView;

public class XLinkUtilsTest {

    // @extract-start XLinkUtilsTestConfigsProvidedByClient
    
    /**Models supported by the tool, together with possible views*/
    private static HashMap<ModelDescription, List<XLinkToolView>> modelsToViews 
        = new HashMap<ModelDescription, List<XLinkToolView>>();  
    /**Id of the Tool´s connector*/
    private static String connectorId = "exampleConnectorId";
    /**Human readable Name of the demo Tool*/
    private static String toolName = "TestTool";
    /**Key of a demo view*/
    private static String viewId1 = "exampleViewId_1";
    /**Key of a demo view*/    
    private static String viewId2 = "exampleViewId_2";
    /**Descriptions in different languages for a view*/    
    private static HashMap<String, String> descriptions  = new HashMap<String, String>();
    /**Composed viewdata as a list.*/
    private static List<XLinkToolView> views = new ArrayList<XLinkToolView>();
    /**Id of the Testcontext at the OpenEngSB*/
    private String contextId = "ExampleContext";
    
    private static OsgiUtilsService serviceFinder;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        descriptions.put("en", "This is a demo view.");
        descriptions.put("de", "Das ist eine demonstration view.");
        views = new ArrayList();
        views.add(new XLinkToolView(viewId1, toolName, descriptions));
        views.add(new XLinkToolView(viewId2, toolName, descriptions));
        modelsToViews.put(new ModelDescription(ExampleObjectOrientedModel.class.getName(), "3.0.0.SNAPSHOT"), views);
    
        //mocking
        serviceFinder = mock(OsgiUtilsService.class);
        ModelRegistry registry = mock(ModelRegistry.class);
        when(serviceFinder.getService(ModelRegistry.class)).thenReturn(registry);
        
        Class clazz = ExampleObjectOrientedModel.class;
        when(registry.loadModel(isA(ModelDescription.class))).thenReturn(clazz); 
        
    }
    // @extract-end
    // @extract-start XLinkUtilsTestConfigsProvidedByOpenEngSB
    
    /**BaseUrl of the xlink-servlet*/
    private String servletUrl = "http://openengsb.org/registryServlet.html";
    /**Days until the XLink expires*/
    private int expiresInDays = 3;
    /**List with already registered tools*/
    private List<XLinkLocalTool> registeredTools = null;
    // @extract-end

    // @extract-start XLinkUtilsTestPrepareTemplate
    @Test
    public void testPrepareXLinkTemplate() {
        XLinkTemplate xLinkTemplate =
            XLinkUtils.prepareXLinkTemplate(servletUrl, connectorId, modelsToViews, expiresInDays, registeredTools);    

        //xLinkTemplate.getBaseUrl() = 
        //http://openengsb.org/registryServlet.html?expirationDate=20120519212036

        assertTrue(xLinkTemplate.getViewToModels().containsKey(viewId1));
        assertTrue(xLinkTemplate.getViewToModels().get(viewId1)
                .getModelClassName().equals(ExampleObjectOrientedModel.class.getName()));
    }

    // @extract-end

    // @extract-start XLinkUtilsTestGenerateValidXLinkUrl
    @Test
    public void testGenerateValidXLinkUrl() throws ClassNotFoundException {
        XLinkTemplate xLinkTemplate =
            XLinkUtils.prepareXLinkTemplate(servletUrl, connectorId, modelsToViews, expiresInDays, registeredTools);  
        List<String> values = Arrays.asList("testMethod", "testClass", "testPackage");

        ModelDescription modelInformation = xLinkTemplate.getViewToModels().get(viewId1);
        String xLinkUrl = XLinkUtils.generateValidXLinkUrl(xLinkTemplate, values, modelInformation, contextId, serviceFinder);

        //xLinkUrl = 
        //http://openengsb.org/registryServlet.html?
        //expirationDate=20120519212036&modelClass=org.openengsb.core.common.xlink.ExampleObjectOrientedModel
        //&versionId=3.0.0.SNAPSHOT&contextId=ExampleContext
        //&OOMethodName=testMethod&OOClassName=testClass&OOPackageName=testPackage

        assertTrue(xLinkUrl.contains("OOMethodName=testMethod"));
        assertTrue(xLinkUrl.contains("OOClassName=testClass"));
        assertTrue(xLinkUrl.contains("OOPackageName=testPackage"));

    }

    // @extract-end

    // @extract-start XLinkUtilsTestGenerateValidXLinkUrlLocalSwitching
    @Test
    public void testGenerateValidXLinkUrlForLocalSwitching() throws ClassNotFoundException {
        XLinkTemplate xLinkTemplate =
            XLinkUtils.prepareXLinkTemplate(servletUrl, connectorId, modelsToViews, expiresInDays, registeredTools);  
        List<String> values = Arrays.asList("testMethod", "testClass", "testPackage");
        ModelDescription modelInformation = xLinkTemplate.getViewToModels().get(viewId1);
        String xLinkUrl = XLinkUtils.generateValidXLinkUrlForLocalSwitching(xLinkTemplate, 
                values, modelInformation, contextId, viewId1, serviceFinder);

        //xLinkUrl =
        //http://openengsb.org/registryServlet.html?
        //expirationDate=20120519212036&modelClass=org.openengsb.core.common.xlink.ExampleObjectOrientedModel
        //&versionId=3.0.0.SNAPSHOT&contextId=ExampleContext
        //&OOMethodName=testMethod&OOClassName=testClass&OOPackageName=testPackage
        //&connectorId=exampleConnectorId&viewId=exampleViewId_1

        assertTrue(xLinkUrl.contains(XLinkUtils.XLINK_CONNECTORID_KEY + "=" + connectorId));
        assertTrue(xLinkUrl.contains(XLinkUtils.XLINK_VIEW_KEY + "=" + viewId1));

    }
    // @extract-end

}
