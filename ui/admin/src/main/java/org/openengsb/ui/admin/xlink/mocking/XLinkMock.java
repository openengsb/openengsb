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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.xlink.model.XLinkLocalTool;
import org.openengsb.core.api.xlink.model.XLinkModelInformation;
import org.openengsb.core.api.xlink.model.XLinkTemplate;
import org.openengsb.core.api.xlink.model.XLinkToolView;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.core.common.xlink.XLinkUtils;

/**
 */
public final class XLinkMock {
    
    private XLinkMock(){
        
    }
    
    private static ConnectorId getConnectorIdClassForStringId(String id){
        //todo fetch ConnectorInstance for String Id
        return null;
    }
    
    public static XLinkModelInformation getModelClass(String cId, String viewId){
        ConnectorId connectorId = getConnectorIdClassForStringId(cId);
        return getModelClassOfView(connectorId,viewId);
    }
    
    private static XLinkModelInformation getModelClassOfView(ConnectorId connectorId, String viewId){
        XLinkTemplate template = getXLinkTemplateFromConnector(connectorId);
        return template.getViewToModels().get(viewId);
    }
    
    //tod testing
    public static List<String> getModelIdentifierToModelId(String modelId, String versionId) throws ClassNotFoundException{
        //Todo fetch real identifiers
        OpenEngSBModel model = createInstanceOfModelClass(modelId, versionId);
        List<OpenEngSBModelEntry> entries = model.getOpenEngSBModelEntries();
        List<String> identifierKeyNames = new ArrayList<String>();
        for(OpenEngSBModelEntry entry : entries){
            identifierKeyNames.add(entry.getKey());
        }
        return identifierKeyNames;
    }
    
    //todo convert class string to class object
    private static OpenEngSBModel createInstanceOfModelClass(String clazz, String versionId) throws ClassNotFoundException{
        return ModelUtils.createEmptyModelObject(ExampleObjectOrientedDomain.class) ;
    }      
    
    public static void transformAndOpenMatch(String sourceModelClass, String sourceModelVersion, Map<String,String> sourceModelIdentifierMap, String destinationModelClass, String destinationModelVersion, String connectorToCall, String viewToCall){
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "transformAndOpenMatch was called:");
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "sourceModelClass - "+sourceModelClass);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "sourceModelVersion - "+sourceModelVersion);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "destinationModelClass - "+destinationModelClass);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "destinationModelVersion - "+destinationModelVersion);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "connectorToCall - "+connectorToCall);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "viewToCall - "+viewToCall);
        //todo check if transformation can be done
        Object modelObjectSource = queryEngine(sourceModelClass, sourceModelVersion, sourceModelIdentifierMap);
        Object modelObjectsDestination = transformModelObject(sourceModelClass, sourceModelVersion, destinationModelClass, destinationModelVersion, modelObjectSource);
        openPotentialMatches(modelObjectsDestination, getConnectorIdClassForStringId(connectorToCall), viewToCall);
    }
    
    private static Object queryEngine(String sourceModelClass, String sourceModelVersion, Map<String,String> sourceModelIdentifierMap){
        //todo
        return null;
    }
    private static Object transformModelObject(String sourceModelClass, String sourceModelVersion, String destinationModelClass, String destinationModelVersion, Object modelObjectSource){
        //todo
        return null;
    }
    private static void openPotentialMatches(Object modelObjectsDestination, ConnectorId connectorToCall, String viewToCall){
        //todo
    }
    
    public static List<XLinkLocalTool> getRegisteredToolsFromUser(String hostId){
        List<ConnectorId> connectors = getConnectorIdsFromHost(hostId);
        return getToolsFromConnectors(connectors);
    }
    
    private static List<XLinkLocalTool> createMockToolList(){
        List<XLinkLocalTool> tools = new ArrayList<XLinkLocalTool>();
        XLinkLocalTool dummyTool1 = new XLinkLocalTool();
        dummyTool1.setId(new ConnectorId());
        
        String viewId_1 = "exampleViewId_1";
        String viewId_2 = "exampleViewId_2";
        
        HashMap<String, String> descriptions  = new HashMap<String, String>();
        descriptions.put("en","This is a demo view.");
        descriptions.put("de","Das ist eine demonstration view.");
        
        List<XLinkToolView> views = new ArrayList<XLinkToolView>();
        views.add(new XLinkToolView(viewId_1, "View 1", descriptions));
        views.add(new XLinkToolView(viewId_2, "View 2", descriptions));
        
        HashMap<String, List<XLinkToolView>> modelsToViews = new HashMap<String, List<XLinkToolView>>(); 
        modelsToViews.put(ExampleObjectOrientedDomain.class.getName(), views);
        
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
    
    private static List<XLinkLocalTool> getToolsFromConnectors(List<ConnectorId> connectors){
        List<XLinkLocalTool> tools = new ArrayList();
        for(ConnectorId connectorId : connectors){
            tools.add(getToolsFromConnector(connectorId));
        }
        //todo return tools;
        return createMockToolList();
    }
    
    private static XLinkLocalTool getToolsFromConnector(ConnectorId connectorId){
        String name = getToolNameFromConnector(connectorId);
        List<XLinkToolView> availableViews = getViewsFromConnector(connectorId);
        return new XLinkLocalTool(connectorId, name, availableViews);
    }
    
    private static List<XLinkToolView> getViewsFromConnector(ConnectorId connectorId) {
        HashMap<String, List<XLinkToolView>> modelToViews = getModelViewCombinationsFromConnector(connectorId);
        List<XLinkToolView> viewList = new ArrayList<XLinkToolView>();
        for(String className: modelToViews.keySet()){
            List<XLinkToolView> currentViewList = modelToViews.get(className);
            for(XLinkToolView view : currentViewList){
                if(!viewList.contains(view))viewList.add(view);
            }
        }
        return viewList;
    }        

    private static String getToolNameFromConnector(ConnectorId connectorId) {
        //todo fetch toolName for ConnectorId
        return null;
    }
    
    private static List<ConnectorId> getConnectorIdsFromHost(String hostId){
        //todo fetch all connectors to a hostid
        return Arrays.asList(new ConnectorId("dummyDomainType", "dummyConnectorType", "dummyInstanceId"));
    }    
    
    private static XLinkTemplate getXLinkTemplateFromConnector(ConnectorId connectorId){
        //todo fetch template
        return createMockTemplate();
    }
    
    private static XLinkTemplate createMockTemplate(){
        HashMap<XLinkModelInformation, List<XLinkToolView>> modelsToViews = new HashMap<XLinkModelInformation, List<XLinkToolView>>();  
        String viewId_1 = "exampleViewId_1";
        String viewId_2 = "exampleViewId_2";
        
        HashMap<String, String> descriptions  = new HashMap<String, String>();
        descriptions.put("en","This is a demo view.");
        descriptions.put("de","Das ist eine demonstration view.");
        
        List<XLinkToolView> views = new ArrayList<XLinkToolView>();
        views.add(new XLinkToolView(viewId_1, "View 1", descriptions));
        views.add(new XLinkToolView(viewId_2, "View 2", descriptions));          
        
        modelsToViews.put(new XLinkModelInformation(ExampleObjectOrientedDomain.class.getName(),"1.0"), views);  
        String connectorId = "exampleConnectorId";
        String servletUrl = "http://openengsb.org/registryServlet.html";
        int expiresInDays = 3;
        List<XLinkLocalTool> registeredTools = null;        
        return XLinkUtils.prepareXLinkTemplate(servletUrl, connectorId, modelsToViews, expiresInDays, registeredTools);  
    }
    
    private static HashMap<String, List<XLinkToolView>> getModelViewCombinationsFromConnector(ConnectorId connectorId){
        //todo fetch model/views for connectorId
        return new HashMap<String, List<XLinkToolView>>();
    }
    
    public static  boolean isTransformationPossible(String srcModelClass, String srcModelVersion, String destModelClass, String destModelVersion){
        //todo implement check here
        return true;
    }
   
}
