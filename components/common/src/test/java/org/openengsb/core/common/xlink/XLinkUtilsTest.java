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
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import org.openengsb.core.api.xlink.XLinkModelInformation;
import org.openengsb.core.api.xlink.XLinkRegisteredTool;
import org.openengsb.core.api.xlink.XLinkTemplate;
import org.openengsb.core.api.xlink.XLinkToolView;

public class XLinkUtilsTest {

    // @extract-start XLinkUtilsTestConfigsProvidedByClient
    
    /**Models supported by the tool, together with possible views*/
    private static HashMap<String, List<XLinkToolView>> modelsToViews = new HashMap<String, List<XLinkToolView>>();  
    /**Id of the Tool´s connector*/
    private static String connectorId = "exampleConnectorId";
    /**Human readable Name of the demo Tool*/
    private static String toolName = "TestTool";
    /**Key of a demo view*/
    private static String viewId_1 = "exampleViewId_1";
    /**Key of a demo view*/    
    private static String viewId_2 = "exampleViewId_2";
    /**Descriptions in different languages for a view*/    
    private static HashMap<String, String> descriptions  = new HashMap<String, String>();
    /**Composed viewdata as a list.*/
    private static List<XLinkToolView> views = new ArrayList<XLinkToolView>();
    /**Id of the Testcontext at the OpenEngSB*/
    private String contextId = "ExampleContext";
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        descriptions.put("en","This is a demo view.");
        descriptions.put("de","Das ist eine demonstration view.");
        views = new ArrayList();
        views.add(new XLinkToolView(viewId_1, toolName, descriptions));
        views.add(new XLinkToolView(viewId_2, toolName, descriptions));
        modelsToViews.put(ExampleObjectOrientedDomain.class.getName(), views);
    }
    // @extract-end
    // @extract-start XLinkUtilsTestConfigsProvidedByOpenEngSB
    
    /**BaseUrl of the xlink-servlet*/
    private String servletUrl = "http://openengsb.org/registryServlet.html";
    /**Days until the XLink expires*/
    private int expiresInDays = 3;
    /**List with already registered tools*/
    private List<XLinkRegisteredTool> registeredTools = null;
    // @extract-end

    // @extract-start XLinkUtilsTestPrepareTemplate
    @Test
    public void testPrepareXLinkTemplate() {
        XLinkTemplate xLinkTemplate =
            XLinkUtils.prepareXLinkTemplate(servletUrl, connectorId, modelsToViews, expiresInDays, registeredTools);    

        //xLinkTemplate.getBaseUrl() = 
        //http://openengsb.org/registryServlet.html?contextId=ExampleContext&expirationDate=20120427190146

        assertTrue(xLinkTemplate.getViewToModels().containsKey(viewId_1));
        assertTrue(xLinkTemplate.getViewToModels().get(viewId_1).getClassName().equals(ExampleObjectOrientedDomain.class.getName()));
    }

    // @extract-end

    // @extract-start XLinkUtilsTestGenerateValidXLinkUrl
    @Test
    public void testGenerateValidXLinkUrl() throws ClassNotFoundException {
        XLinkTemplate xLinkTemplate =
            XLinkUtils.prepareXLinkTemplate(servletUrl, connectorId, modelsToViews, expiresInDays, registeredTools);  
        List<String> values = Arrays.asList("testMethod", "testClass", "testPackage");

        XLinkModelInformation modelInformation = xLinkTemplate.getViewToModels().get(viewId_1);
        String xLinkUrl = XLinkUtils.generateValidXLinkUrl(xLinkTemplate, values, modelInformation, contextId);

        //xLinkUrl = 
        //http://openengsb.org/registryServlet.html?expirationDate=20120504202007&modelClass=org.openengsb.core.common.xlink.ExampleObjectOrientedDomain
        //&versionId=class org.openengsb.core.api.xlink.XLinkModelInformation&contextId=ExampleContext
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
        XLinkModelInformation modelInformation = xLinkTemplate.getViewToModels().get(viewId_1);
        String xLinkUrl = XLinkUtils.generateValidXLinkUrlForLocalSwitching(xLinkTemplate, values, modelInformation, contextId, viewId_1);

        //xLinkUrl =
        //http://openengsb.org/registryServlet.html?expirationDate=20120504202007&null=org.openengsb.core.common.xlink.ExampleObjectOrientedDomain
        //&versionId=class org.openengsb.core.api.xlink.XLinkModelInformation&contextId=ExampleContext
        //&OOMethodName=testMethod&OOClassName=testClass&OOPackageName=testPackage
        //&connectorId=exampleConnectorId&viewId=exampleViewId_1


        assertTrue(xLinkUrl.contains(XLinkUtils.XLINK_CONNECTORID_KEY + "=" + connectorId));
        assertTrue(xLinkUrl.contains(XLinkUtils.XLINK_VIEW_KEY + "=" + viewId_1));

    }
    // @extract-end

}
