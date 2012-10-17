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

package org.openengsb.core.services.internal.xlink;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.xlink.model.XLinkConnector;
import org.openengsb.core.api.xlink.model.XLinkConnectorView;
import org.openengsb.core.api.xlink.model.XLinkConstants;
import org.openengsb.core.api.xlink.model.XLinkUrlBlueprint;
import org.openengsb.core.ekb.api.ModelRegistry;
import org.openengsb.core.services.xlink.XLinkDemonstrationUtils;
import org.openengsb.core.services.xlink.XLinkUtils;

public class XLinkUtilsTest {

    // @extract-start XLinkUtilsTestConfigsProvidedByClient
    
    /**Models supported by the tool, together with possible views*/
    private static HashMap<ModelDescription, List<XLinkConnectorView>> modelsToViews 
        = new HashMap<ModelDescription, List<XLinkConnectorView>>();  
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
    private static List<XLinkConnectorView> views = new ArrayList<XLinkConnectorView>();
    /**Id of the Testcontext at the OpenEngSB*/
    private String contextId = "ExampleContext";
    /**Modelclass to use for Testing*/
    private static Class exampleModelClass = ExampleObjectOrientedModel.class;
    /**Version of ModelClass*/
    private static String exampleModelClassVersion = "3.0.0.SNAPSHOT";
    
    private static OsgiUtilsService serviceFinder;
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        descriptions.put("en", "This is a demo view.");
        descriptions.put("de", "Das ist eine demonstration view.");
        views = new ArrayList();
        views.add(new XLinkConnectorView(viewId1, toolName, descriptions));
        views.add(new XLinkConnectorView(viewId2, toolName, descriptions));
        modelsToViews.put(new ModelDescription(exampleModelClass.getName(), exampleModelClassVersion), views);
    
        serviceFinder = mock(OsgiUtilsService.class);
        ModelRegistry registry = mock(ModelRegistry.class);
        when(serviceFinder.getService(ModelRegistry.class)).thenReturn(registry);
        
        when(registry.loadModel(isA(ModelDescription.class))).thenReturn(exampleModelClass); 
        
    }
    // @extract-end
    // @extract-start XLinkUtilsTestConfigsProvidedByOpenEngSB
    
    /**BaseUrl of the xlink-servlet*/
    private String servletUrl = "http://openengsb.org/registryServlet.html";
    /**Days until the XLink expires*/
    private int expiresInDays = 3;
    /**List with already registered tools*/
    private List<XLinkConnector> registeredTools = null;
    // @extract-end

    // @extract-start XLinkUtilsTestPrepareTemplate
    @Test
    public void testPrepareXLinkTemplate() {
        XLinkUrlBlueprint xLinkTemplate =
            XLinkUtils.prepareXLinkTemplate(servletUrl, connectorId, modelsToViews, expiresInDays, registeredTools);    

        //xLinkTemplate.getBaseUrl() = 
        //http://openengsb.org/registryServlet.html?expirationDate=20120519212036

        assertTrue(xLinkTemplate.getViewToModels().containsKey(viewId1));
        assertTrue(xLinkTemplate.getViewToModels().get(viewId1)
                .getModelClassName().equals(exampleModelClass.getName()));
    }

    // @extract-end

    // @extract-start XLinkUtilsTestGenerateValidXLinkUrl
    @Test
    public void testGenerateValidXLinkUrl() throws ClassNotFoundException, 
        IOException, NoSuchFieldException, IllegalArgumentException, 
        IllegalAccessException {
        XLinkUrlBlueprint xLinkTemplate =
                XLinkUtils.prepareXLinkTemplate(servletUrl, 
                connectorId, modelsToViews, expiresInDays, registeredTools);  
        List<Object> values = new ArrayList<Object>(Arrays.asList("testMethod", "testClass", "testPackage"));

        ModelDescription modelInformation = xLinkTemplate.getViewToModels().get(viewId1);
        String modelAsJsonString = XLinkDemonstrationUtils
            .serializeModelObjectToJSON(values, modelInformation, serviceFinder);
        String xLinkUrl = XLinkDemonstrationUtils
            .generateValidXLinkUrl(xLinkTemplate, modelInformation, contextId, modelAsJsonString);
        
        //(encoded) xLinkUrl = 
        //http://openengsb.org/registryServlet.html?expirationDate=20120728183009
        //&modelClass=org.openengsb.core.common.xlink.ExampleObjectOrientedModel
        //&versionId=3.0.0.SNAPSHOT&contextId=ExampleContext
        //&identifier=%7B%22openEngSBModelEntries%22%3A%5B%7B%22key%22%3A%22
        //OOMethodName%22%2C%22value%22%3A%22testMethod%22%2C%22type%22%3A%22
        //java.lang.String%22%7D%2C%7B%22key%22%3A%22OOClassName%22%2C%22value%22%3A%22
        //testClass%22%2C%22type%22%3A%22java.lang.String%22%7D%2C%7B%22key%22%3A%22
        //OOPackageName%22%2C%22value%22%3A%22testPackage%22%2C%22type%22%3A%22
        //java.lang.String%22%7D%5D%2C%22oomethodName%22%3A%22testMethod%22%2C%22ooclassName
        //%22%3A%22testClass%22%2C%22oopackageName%22%3A%22testPackage%22%7D


        assertTrue(xLinkUrl.contains("ooMethodName%22%3A%22testMethod"));
        assertTrue(xLinkUrl.contains("ooClassName%22%3A%22testClass"));
        assertTrue(xLinkUrl.contains("ooPackageName%22%3A%22testPackage"));

    }

    // @extract-end

    // @extract-start XLinkUtilsTestGenerateValidXLinkUrlLocalSwitching
    @Test
    public void testGenerateValidXLinkUrlForLocalSwitching() throws ClassNotFoundException, 
        IOException, NoSuchFieldException, 
        IllegalArgumentException, IllegalAccessException {
        XLinkUrlBlueprint xLinkTemplate =
            XLinkUtils.prepareXLinkTemplate(servletUrl, connectorId, modelsToViews, expiresInDays, registeredTools);  
        List<Object> values = new ArrayList<Object>(Arrays.asList("testMethod", "testClass", "testPackage"));
        ModelDescription modelInformation = xLinkTemplate.getViewToModels().get(viewId1);
        String modelAsJsonString
            = XLinkDemonstrationUtils.serializeModelObjectToJSON(values, modelInformation, serviceFinder);     
        String xLinkUrl = XLinkDemonstrationUtils.generateValidXLinkUrlForLocalSwitching(xLinkTemplate, 
                modelInformation, contextId, viewId1, modelAsJsonString);

        //(encoded) xLinkUrl =
        //http://openengsb.org/registryServlet.html?expirationDate=20120728183009
        //&modelClass=org.openengsb.core.common.xlink.ExampleObjectOrientedModel
        //&versionId=3.0.0.SNAPSHOT&contextId=ExampleContext
        //&identifier=%7B%22openEngSBModelEntries%22%3A%5B%7B%22key%22%3A%22
        //OOMethodName%22%2C%22value%22%3A%22testMethod%22%2C%22type%22%3A%22
        //java.lang.String%22%7D%2C%7B%22key%22%3A%22OOClassName%22%2C%22value%22%3A%22
        //testClass%22%2C%22type%22%3A%22java.lang.String%22%7D%2C%7B%22key%22%3A%22
        //OOPackageName%22%2C%22value%22%3A%22testPackage%22%2C%22type%22%3A%22
        //java.lang.String%22%7D%5D%2C%22oomethodName%22%3A%22testMethod%22%2C%22ooclassName
        //%22%3A%22testClass%22%2C%22oopackageName%22%3A%22testPackage%22%7D
        //&connectorId=exampleConnectorId&viewId=exampleViewId_1


        assertTrue(xLinkUrl.contains(XLinkConstants.XLINK_CONNECTORID_KEY + "=" + connectorId));
        assertTrue(xLinkUrl.contains(XLinkConstants.XLINK_VIEW_KEY + "=" + viewId1));

    }
    // @extract-end

}
