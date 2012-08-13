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

import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.LinkableDomain;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.security.SecurityContext;
import org.openengsb.core.services.xlink.XLinkUtils;
import org.openengsb.domain.DomainModelOOSource.model.OOClassModel;
import org.openengsb.domain.DomainModelSQL.model.SQLCreateModel;
import org.openengsb.ui.admin.xlink.exceptions.OpenXLinkException;

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
        if (isTransformationPossible(sourceModelClass, 
            sourceModelVersion, destinationModelClass, destinationModelVersion)) {
            List<Object> modelObjectsDestination = transformModelObject(sourceModelClass, 
                    destinationModelClass, destinationModelVersion, soureObject, osgiService);
            if(!modelObjectsDestination.isEmpty()) {
                openPotentialMatches(modelObjectsDestination, connectorToCall, viewToCall, osgiService);
            }
        }else{
            throw new OpenXLinkException(noTransformationPossible);
        }
    }
 
    private static List<Object> transformModelObject(
            String sourceModelClass,  
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
        List<Object> matches = new ArrayList<Object>();
        matches.add(resultObject);
        return matches;
    }
    private static void openPotentialMatches(
            List<Object> modelObjectsDestination, 
            String connectorToCall, 
            String viewToCall,
            OsgiUtilsService osgiService) throws OsgiServiceNotAvailableException, 
            ClassCastException, OpenXLinkException{
        Logger.getLogger(XLinkMock.class.getName()).log(Level.FINER, "openPotentialMatches was called:");
        Logger.getLogger(XLinkMock.class.getName()).log(Level.FINER, "modelObjectsDestination - " + modelObjectsDestination.get(0));
        Logger.getLogger(XLinkMock.class.getName()).log(Level.FINER, "connectorToCall - " + connectorToCall);
        Logger.getLogger(XLinkMock.class.getName()).log(Level.FINER, "viewToCall - " + viewToCall);

        Object serviceObject = osgiService.getService("(service.pid="+connectorToCall+")", 100L);
        if(serviceObject == null) throw new OpenXLinkException(connectorNotFound);
        LinkableDomain service = (LinkableDomain) serviceObject;
        SecurityContext.login("admin", new Password("password"));
        service.openXLinks(modelObjectsDestination, viewToCall);
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
    
}
