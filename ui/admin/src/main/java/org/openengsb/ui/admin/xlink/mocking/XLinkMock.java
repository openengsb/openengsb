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

package org.openengsb.ui.admin.xlink.mocking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.xlink.model.XLinkModelInformation;
import org.openengsb.core.api.xlink.model.XLinkToolView;
import org.openengsb.core.common.xlink.ExampleObjectOrientedModel;

/**
 */
public final class XLinkMock {
    
    private XLinkMock() {
        
    }
    
    public static void transformAndOpenMatch(
            String sourceModelClass, 
            String sourceModelVersion, 
            Map<String, String> sourceModelIdentifierMap, 
            String destinationModelClass, 
            String destinationModelVersion, 
            String connectorToCall, 
            String viewToCall) {
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "transformAndOpenMatch was called:");
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "sourceModelClass - " + sourceModelClass);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "sourceModelVersion - " + sourceModelVersion);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "destinationModelClass - " + destinationModelClass);
        Logger.getLogger(XLinkMock.class.getName()).log(
                Level.INFO, "destinationModelVersion - " +  destinationModelVersion);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "connectorToCall - " + connectorToCall);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "viewToCall - " + viewToCall);
        //todo check if transformation can be done
        if (isTransformationPossible(sourceModelClass, 
                sourceModelVersion, destinationModelClass, destinationModelVersion)) {
            Object modelObjectSource = queryEngine(sourceModelClass, sourceModelVersion, sourceModelIdentifierMap);
            Object modelObjectsDestination = transformModelObject(sourceModelClass, 
                    sourceModelVersion, destinationModelClass, destinationModelVersion, modelObjectSource);
            String cId = connectorToCall;
            openPotentialMatches(modelObjectsDestination, cId, viewToCall);
        }
    }
    
    private static Object queryEngine(
            String sourceModelClass, 
            String sourceModelVersion, 
            Map<String, String> sourceModelIdentifierMap) {
        //todo
        return null;
    }
    private static Object transformModelObject(
            String sourceModelClass, 
            String sourceModelVersion, 
            String destinationModelClass, 
            String destinationModelVersion, 
            Object modelObjectSource) {
        //todo
        return null;
    }
    private static void openPotentialMatches(
            Object modelObjectsDestination, 
            String connectorToCall, 
            String viewToCall) {
        //todo
    }
    
    public static  boolean isTransformationPossible(
            String srcModelClass, 
            String srcModelVersion, 
            String destModelClass, 
            String destModelVersion) {
        //todo implement check here
        return true;
    }    
    
    public static void dummyRegistrationOfTools(ConnectorManager serviceManager) {
        HashMap<XLinkModelInformation, List<XLinkToolView>> modelsToViews 
            = new HashMap<XLinkModelInformation, List<XLinkToolView>>();  
        String viewId1 = "exampleViewId_1";
        String viewId2 = "exampleViewId_2";
        
        HashMap<String, String> descriptions  = new HashMap<String, String>();
        descriptions.put("en", "This is a demo view.");
        descriptions.put("de", "Das ist eine demonstration view.");
        
        List<XLinkToolView> views = new ArrayList<XLinkToolView>();
        views.add(new XLinkToolView(viewId1, "View 1", descriptions));
        views.add(new XLinkToolView(viewId2, "View 2", descriptions));          
        
        modelsToViews.put(new XLinkModelInformation(ExampleObjectOrientedModel.class.getName(), "1.0"), views); 
        
        String toolName1 = "Tool A";
        String toolName2 = "Tool B";
        String hostId = "localhost:8090";
        String cId1 = "test1+test1+test1";
        String cId2 = "test2+test2+test2";
        
        //test2+test2+test2
        //test2%2Btest2%2Btest2
        serviceManager.connectToXLink(cId1, hostId, toolName1, modelsToViews);
        serviceManager.connectToXLink(cId2, hostId, toolName2, modelsToViews);
    }
    
    /*private static List<XLinkLocalTool> createMockToolList() {
        List<XLinkLocalTool> tools = new ArrayList<XLinkLocalTool>();
        XLinkLocalTool dummyTool1 = new XLinkLocalTool();
        dummyTool1.setId(new ConnectorId());
        
        String viewId1 = "exampleViewId_1";
        String viewId2 = "exampleViewId_2";
        
        HashMap<String, String> descriptions  = new HashMap<String, String>();
        descriptions.put("en", "This is a demo view.");
        descriptions.put("de", "Das ist eine demonstration view.");
        
        List<XLinkToolView> views = new ArrayList<XLinkToolView>();
        views.add(new XLinkToolView(viewId1, "View 1", descriptions));
        views.add(new XLinkToolView(viewId2, "View 2", descriptions));
        
        HashMap<String, List<XLinkToolView>> modelsToViews = new HashMap<String, List<XLinkToolView>>(); 
        modelsToViews.put(ExampleObjectOrientedModel.class.getName(), views);
        
        dummyTool1.setAvailableViews(views);
        dummyTool1.setToolName("Tool A");     
        tools.add(dummyTool1);
        XLinkLocalTool dummyTool2 = new XLinkLocalTool();
        dummyTool2.setId(new ConnectorId());
        dummyTool2.setAvailableViews(views);
        dummyTool2.setToolName("Tool B");     
        tools.add(dummyTool2);
        return tools;
    }    

    private static XLinkTemplate createMockTemplate() {
        HashMap<XLinkModelInformation, List<XLinkToolView>> modelsToViews 
            = new HashMap<XLinkModelInformation, List<XLinkToolView>>();  
        String viewId1 = "exampleViewId_1";
        String viewId2 = "exampleViewId_2";
        
        HashMap<String, String> descriptions  = new HashMap<String, String>();
        descriptions.put("en", "This is a demo view.");
        descriptions.put("de", "Das ist eine demonstration view.");
        
        List<XLinkToolView> views = new ArrayList<XLinkToolView>();
        views.add(new XLinkToolView(viewId1, "View 1", descriptions));
        views.add(new XLinkToolView(viewId2, "View 2", descriptions));          
        
        modelsToViews.put(new XLinkModelInformation(ExampleObjectOrientedModel.class.getName(), "1.0"), views);  
        String connectorId = "exampleConnectorId";
        String servletUrl = "http://openengsb.org/registryServlet.html";
        int expiresInDays = 3;
        List<XLinkLocalTool> registeredTools = null;        
        return XLinkUtils.prepareXLinkTemplate(
                servletUrl, connectorId, modelsToViews, expiresInDays, registeredTools);  
    }*/
   
}
