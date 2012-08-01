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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openengsb.core.api.LinkableDomain;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.services.internal.xlink.XLinkUtils;
import org.openengsb.domain.DomainModelOOSource.model.OOClassModel;
import org.openengsb.domain.DomainModelSQL.model.SQLCreateModel;
import org.openengsb.ui.admin.xlink.exceptions.OpenXLinkException;

/**
 */
public final class XLinkMock {
    
    private XLinkMock() {
        
    }
    
    public static final String sqlModel = SQLCreateModel.class.getName();
    public static final String ooModel = OOClassModel.class.getName();
    public static final String connectorNotFound = "Connectorservice was not found.";
    public static final String noTransformationPossible = "Transformation is not possible.";
    
    public static void transformAndOpenMatch(
            String sourceModelClass, 
            String sourceModelVersion, 
            Object soureObject, 
            String destinationModelClass, 
            String destinationModelVersion, 
            String connectorToCall, 
            String viewToCall,
            OsgiUtilsService osgiService) throws ClassNotFoundException, 
            OsgiServiceNotAvailableException, ClassCastException, OpenXLinkException {
        
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
            //Object modelObjectSource = queryEngine(sourceModelClass, sourceModelVersion, sourceModelIdentifierMap, osgiService);
            Object modelObjectsDestination = transformModelObject(sourceModelClass, 
                    sourceModelVersion, destinationModelClass, destinationModelVersion, soureObject, osgiService);
            String cId = connectorToCall;
            openPotentialMatches(modelObjectsDestination, cId, viewToCall, osgiService);
        }else{
            throw new OpenXLinkException(noTransformationPossible);
        }
    }
    
    /*private static Object queryEngine(
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
    }*/
    private static Object transformModelObject(
            String sourceModelClass, 
            String sourceModelVersion, 
            String destinationModelClass, 
            String destinationModelVersion, 
            Object modelObjectSource,
            OsgiUtilsService osgiService) throws ClassNotFoundException {
        if(modelObjectSource == null)return null;
        Object resultObject = null;
        
        Class destinationClass = XLinkUtils.getClassOfOpenEngSBModel(destinationModelClass, destinationModelVersion, osgiService);
        
        if(sourceModelClass.equals(sqlModel) && destinationModelClass.equals(ooModel)){
            SQLCreateModel sqlSource = (SQLCreateModel) modelObjectSource;
            OOClassModel ooclass = (OOClassModel) XLinkUtils.createEmptyInstanceOfModelClass(destinationClass);
            ooclass.setClassName(sqlSource.getTableName());
            resultObject = ooclass;
        } else if(sourceModelClass.equals(ooModel) && destinationModelClass.equals(sqlModel)){
            OOClassModel ooSource = (OOClassModel) modelObjectSource;
            SQLCreateModel sqlcreate = (SQLCreateModel) XLinkUtils.createEmptyInstanceOfModelClass(destinationClass);
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
            OsgiUtilsService osgiService) throws OsgiServiceNotAvailableException, 
            ClassCastException, OpenXLinkException{
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "openPotentialMatches was called:");
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "modelObjectsDestination - " + modelObjectsDestination);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "connectorToCall - " + connectorToCall);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.INFO, "viewToCall - " + viewToCall);
        List<Object> matches = new ArrayList<Object>();
        matches.add(modelObjectsDestination);
        
        Object serviceObject = osgiService.getService("service.pid="+connectorToCall, 100L);
        if(serviceObject == null) throw new OpenXLinkException(connectorNotFound);
        LinkableDomain service = (LinkableDomain) serviceObject;

        service.openXLinks(matches, viewToCall);
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
        //todo implement real check here
        return true;
    }    

    /*    
     *  String toolName_SQLCreateModel = "Tool SQLCreateModel";
        String connectorId_SQLCreateModel = "test3+test3+test3";
        
        String toolName_OOClassModel = "Tool OOClassModel";
        String connectorId_OOClassModel = "test4+test4+test4";
     * 
     * private void registerTool_SQLCreateModel(String hostId, String toolName, String connectorId) {
        
        ConnectorDescription connectorDescription =
            new ConnectorDescription("domainmodelsql", "virtual-test-connector", new HashMap<String, String>(),
                new HashMap<String, Object>());
        
        serviceManager.createWithId(connectorId, connectorDescription); 
        
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
        
        serviceManager.connectToXLink(connectorId, hostId, toolName, modelsToViews_SQLCreateModel);           
        
    }
    
    private void registerTool_OOClassModel(String hostId, String toolName, String connectorId) {
        
        ConnectorDescription connectorDescription =
            new ConnectorDescription("domainmodeloosource", "virtual-test-connector", new HashMap<String, String>(),
                new HashMap<String, Object>());
        
        serviceManager.createWithId(connectorId, connectorDescription); 
        
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
        
        serviceManager.connectToXLink(connectorId, hostId, toolName, modelsToViews_OOClassModel);           
        
    }      */
    
}
