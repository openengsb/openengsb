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
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.ekb.ModelDescription;
import org.openengsb.core.api.xlink.model.ModelToViewsTupel;
import org.openengsb.core.api.xlink.model.XLinkToolView;
import org.openengsb.core.common.xlink.ExampleObjectOrientedModel;
import org.openengsb.core.common.xlink.XLinkUtils;
import org.openengsb.domain.DomainModelOOSource.model.OOClassModel;
import org.openengsb.domain.DomainModelSQL.model.SQLCreateModel;

/**
 */
public final class XLinkMock {
    
    private XLinkMock() {
        
    }
    
    public static final String sqlModel = SQLCreateModel.class.getName();
    public static final String ooModel = OOClassModel.class.getName();
    
    public static void transformAndOpenMatch(
            String sourceModelClass, 
            String sourceModelVersion, 
            Object soureObject, 
            String destinationModelClass, 
            String destinationModelVersion, 
            String connectorToCall, 
            String viewToCall,
            OsgiUtilsService osgiService) {
        
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "transformAndOpenMatch was called:");
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "sourceModelClass - " + sourceModelClass);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "sourceModelVersion - " + sourceModelVersion);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "soureObject - " + soureObject);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "destinationModelClass - " + destinationModelClass);
        Logger.getLogger(XLinkMock.class.getName()).log(
                Level.INFO, "destinationModelVersion - " +  destinationModelVersion);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "connectorToCall - " + connectorToCall);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "viewToCall - " + viewToCall);

        
        if (isTransformationPossible(sourceModelClass, 
                sourceModelVersion, destinationModelClass, destinationModelVersion)) {
            try{
                //Object modelObjectSource = queryEngine(sourceModelClass, sourceModelVersion, sourceModelIdentifierMap, osgiService);
                Object modelObjectsDestination = transformModelObject(sourceModelClass, 
                        sourceModelVersion, destinationModelClass, destinationModelVersion, soureObject, osgiService);
                String cId = connectorToCall;
                openPotentialMatches(modelObjectsDestination, cId, viewToCall, osgiService);
            }catch(Exception e){
                Logger.getLogger(XLinkMock.class.getName()).log(Level.SEVERE, null, e);
            }
        }else{
            //this should not happen since the preselection of the user was only done with transformable classes
        }
    }
    
    private static Object queryEngine(
            String sourceModelClass, 
            String sourceModelVersion, 
            Map<String, String> sourceModelIdentifierMap,
            OsgiUtilsService osgiService) throws ClassNotFoundException {
        Object toReturn = null;
        if(sourceModelClass.equals(sqlModel)){
            SQLCreateModel sqlcreate = (SQLCreateModel) XLinkUtils.createInstanceOfModelClass(sourceModelClass, sourceModelVersion, osgiService);
            sqlcreate.setTableName(sourceModelIdentifierMap.get("tableName"));
            toReturn = sqlcreate;
        }else if(sourceModelClass.equals(ooModel)){
            OOClassModel ooclass = (OOClassModel) XLinkUtils.createInstanceOfModelClass(sourceModelClass, sourceModelVersion, osgiService);
            ooclass.setClassName(sourceModelIdentifierMap.get("className"));
            toReturn = ooclass;
        }
        return toReturn;
    }
    private static Object transformModelObject(
            String sourceModelClass, 
            String sourceModelVersion, 
            String destinationModelClass, 
            String destinationModelVersion, 
            Object modelObjectSource,
            OsgiUtilsService osgiService) throws ClassNotFoundException {
        if(modelObjectSource == null)return null;
        Object resultObject = null;
        
        if(sourceModelClass.equals(sqlModel) && destinationModelClass.equals(ooModel)){
            SQLCreateModel sqlSource = (SQLCreateModel) modelObjectSource;
            OOClassModel ooclass = (OOClassModel) XLinkUtils.createInstanceOfModelClass(destinationModelClass, destinationModelVersion, osgiService);
            ooclass.setClassName(sqlSource.getTableName());
            resultObject = ooclass;
        } else if(sourceModelClass.equals(ooModel) && destinationModelClass.equals(sqlModel)){
            OOClassModel ooSource = (OOClassModel) modelObjectSource;
            SQLCreateModel sqlcreate = (SQLCreateModel) XLinkUtils.createInstanceOfModelClass(destinationModelClass, destinationModelVersion, osgiService);
            sqlcreate.setTableName(ooSource.getClassName());
            resultObject = sqlcreate;
        } else if(sourceModelClass.equals(ooModel) && destinationModelClass.equals(ooModel)){
            OOClassModel ooSource = (OOClassModel) modelObjectSource;
            resultObject = ooSource;
        }else if(sourceModelClass.equals(sqlModel) && destinationModelClass.equals(sqlModel)){
            SQLCreateModel sqlSource = (SQLCreateModel) modelObjectSource;
            resultObject = sqlSource;
        }
        return resultObject;
    }
    private static void openPotentialMatches(
            Object modelObjectsDestination, 
            String connectorToCall, 
            String viewToCall,
            OsgiUtilsService osgiService) {
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "openPotentialMatches was called:");
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "modelObjectsDestination - " + modelObjectsDestination);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "connectorToCall - " + connectorToCall);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "viewToCall - " + viewToCall);

       //osgiService.get
    }
    
    public static  boolean isTransformationPossible(
            String srcModelClass, 
            String srcModelVersion, 
            String destModelClass, 
            String destModelVersion) {
        if(!srcModelClass.equals(ExampleObjectOrientedModel.class.getName()) 
            && destModelClass.equals(ExampleObjectOrientedModel.class.getName())){
            return false;
        }
        //todo implement check here
        return true;
    }    
    
    public static void dummyRegistrationOfTools(ConnectorManager serviceManager) {
        List<ModelToViewsTupel> modelsToViews 
            = new ArrayList<ModelToViewsTupel>();  
        String viewId_ExampleObjectOrientedModel_1 = "viewId_ExampleObjectOrientedModel_1";
        String viewId_ExampleObjectOrientedModel_2 = "viewId_ExampleObjectOrientedModel_2";
        
        HashMap<String, String> descriptions  = new HashMap<String, String>();
        descriptions.put("en", "This is an ExampleObjectOrientedModel view.");
        descriptions.put("de", "Das ist eine ExampleObjectOrientedModel view.");
        
        List<XLinkToolView> views = new ArrayList<XLinkToolView>();
        views.add(new XLinkToolView(viewId_ExampleObjectOrientedModel_1, "View 1", descriptions));
        views.add(new XLinkToolView(viewId_ExampleObjectOrientedModel_2, "View 2", descriptions));          
        
        modelsToViews.add(
                new ModelToViewsTupel(
                        new ModelDescription(
                                ExampleObjectOrientedModel.class.getName(),
                                "3.0.0.SNAPSHOT")
                        , views));
        
        String toolName1 = "Tool A";
        String toolName2 = "Tool B";
        String hostId = "localhost";
        String cId1 = "test1+test1+test1";
        String cId2 = "test2+test2+test2";
        
        //test2+test2+test2
        //test2%2Btest2%2Btest2
        serviceManager.connectToXLink(cId1, hostId, toolName1, modelsToViews);
        serviceManager.connectToXLink(cId2, hostId, toolName2, modelsToViews);
        
         List<ModelToViewsTupel> modelsToViews_SQLCreateModel 
            = new ArrayList<ModelToViewsTupel>();  
        String viewId_SQLCreateModel_1 = "viewId_SQLCreateModel_1";
        String viewId_SQLCreateModel_2 = "viewId_SQLCreateModel_2";
        
        HashMap<String, String> descriptions_SQLCreateModel  = new HashMap<String, String>();
        descriptions_SQLCreateModel.put("en", "This is an SQLCreateModel view.");
        descriptions_SQLCreateModel.put("de", "Das ist eine SQLCreateModel view.");
        
        List<XLinkToolView> views_SQLCreateModel = new ArrayList<XLinkToolView>();
        views_SQLCreateModel.add(new XLinkToolView(viewId_SQLCreateModel_1, "View 1", descriptions_SQLCreateModel));
        views_SQLCreateModel.add(new XLinkToolView(viewId_SQLCreateModel_2, "View 2", descriptions_SQLCreateModel));          
        
        modelsToViews_SQLCreateModel.add(
                new ModelToViewsTupel(
                        new ModelDescription(
                                SQLCreateModel.class.getName(),
                                "3.0.0.SNAPSHOT")
                        , views_SQLCreateModel));  
        
         List<ModelToViewsTupel> modelsToViews_OOClassModel 
            = new ArrayList<ModelToViewsTupel>();  
        String viewId_OOClassModel_1 = "viewId_OOClassModel_1";
        String viewId_OOClassModel_2 = "viewId_OOClassModel_2";
        
        HashMap<String, String> descriptions_OOClassModel  = new HashMap<String, String>();
        descriptions_OOClassModel.put("en", "This is an OOClassModel view.");
        descriptions_OOClassModel.put("de", "Das ist eine OOClassModel view.");
        
        List<XLinkToolView> views_OOClassModel = new ArrayList<XLinkToolView>();
        views_OOClassModel.add(new XLinkToolView(viewId_OOClassModel_1, "View 1", descriptions_OOClassModel));
        views_OOClassModel.add(new XLinkToolView(viewId_OOClassModel_2, "View 2", descriptions_OOClassModel));  
        
        modelsToViews_OOClassModel.add(
                new ModelToViewsTupel(
                        new ModelDescription(
                                OOClassModel.class.getName(),
                                "3.0.0.SNAPSHOT")
                        , views_OOClassModel));        
        
        String toolName3 = "Tool SQLCreateModel";
        String toolName4 = "Tool OOClassModel";
        String cId3 = "test3+test3+test3";
        String cId4 = "test4+test4+test4";  
        
        serviceManager.connectToXLink(cId3, hostId, toolName3, modelsToViews_SQLCreateModel);
        serviceManager.connectToXLink(cId4, hostId, toolName4, modelsToViews_OOClassModel);
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
        HashMap<ModelDescription, List<XLinkToolView>> modelsToViews 
            = new HashMap<ModelDescription, List<XLinkToolView>>();  
        String viewId1 = "exampleViewId_1";
        String viewId2 = "exampleViewId_2";
        
        HashMap<String, String> descriptions  = new HashMap<String, String>();
        descriptions.put("en", "This is a demo view.");
        descriptions.put("de", "Das ist eine demonstration view.");
        
        List<XLinkToolView> views = new ArrayList<XLinkToolView>();
        views.add(new XLinkToolView(viewId1, "View 1", descriptions));
        views.add(new XLinkToolView(viewId2, "View 2", descriptions));          
        
        modelsToViews.put(new ModelDescription(ExampleObjectOrientedModel.class.getName(), "1.0"), views);  
        String connectorId = "exampleConnectorId";
        String servletUrl = "http://openengsb.org/registryServlet.html";
        int expiresInDays = 3;
        List<XLinkLocalTool> registeredTools = null;        
        return XLinkUtils.prepareXLinkTemplate(
                servletUrl, connectorId, modelsToViews, expiresInDays, registeredTools);  
    }*/
   
}
