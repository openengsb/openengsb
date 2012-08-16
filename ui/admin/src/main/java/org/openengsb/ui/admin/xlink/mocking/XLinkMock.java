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

import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.LinkableDomain;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.security.SecurityContext;
import org.openengsb.core.services.xlink.XLinkUtils;
import org.openengsb.domain.OOSourceCode.model.OOClass;
import org.openengsb.domain.SQLCode.model.SQLCreate;
import org.openengsb.ui.admin.xlink.exceptions.OpenXLinkException;

/**
 * This class mocks the unfinished xlink-functionality
 */
public final class XLinkMock {
    
    private XLinkMock() {
        
    }
    
    public static final String sqlModel = SQLCreate.class.getName();
    public static final String ooModel = OOClass.class.getName();
    
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
            List<Object> modelObjectsDestination = transformModelObject(sourceModelClass, sourceModelVersion,
                    destinationModelClass, destinationModelVersion, soureObject, osgiService);
            if(!modelObjectsDestination.isEmpty()) {
                openPotentialMatches(modelObjectsDestination, connectorToCall, viewToCall, osgiService);
            }
        }else{
            
        }
    }
 
    /**
     * Transforms the given ModelObject from it´s SourceClass to the defined DestinationModel.
     */
    public static List<Object> transformModelObject(
            String sourceModelClass,  
            String sourceModelVersion,             
            String destinationModelClass, 
            String destinationModelVersion, 
            Object modelObjectSource,
            OsgiUtilsService osgiService) throws ClassNotFoundException, OpenXLinkException {
        if (!isTransformationPossible(sourceModelClass, 
            sourceModelVersion, destinationModelClass, destinationModelVersion)) {
            throw new OpenXLinkException();      
        }
        if(modelObjectSource == null)return null;
        Object resultObject = null;
        Class destinationClass = XLinkUtils.getClassOfOpenEngSBModel(destinationModelClass, destinationModelVersion, osgiService);
       
        //########### MOCK !!! Todo replace with real transformation        
        
        if(sourceModelClass.equals(sqlModel) && destinationModelClass.equals(ooModel)){
            SQLCreate sqlSource = (SQLCreate) modelObjectSource;
            OOClass ooclass = (OOClass) XLinkUtils.createEmptyInstanceOfModelClass(destinationClass);
            ooclass.setClassName(sqlSource.getTableName());
            resultObject = ooclass;
        } else if(sourceModelClass.equals(ooModel) && destinationModelClass.equals(sqlModel)){
            OOClass ooSource = (OOClass) modelObjectSource;
            SQLCreate sqlcreate = (SQLCreate) XLinkUtils.createEmptyInstanceOfModelClass(destinationClass);
            sqlcreate.setTableName(ooSource.getClassName());
            resultObject = sqlcreate;
        } else if(sourceModelClass.equals(ooModel) && destinationModelClass.equals(ooModel)){
            OOClass ooSource = (OOClass) modelObjectSource;
            resultObject = ooSource;
        }else if(sourceModelClass.equals(sqlModel) && destinationModelClass.equals(sqlModel)){
            SQLCreate sqlSource = (SQLCreate) modelObjectSource;
            resultObject = sqlSource;
        }
        
        //########### MOCK !!! Todo replace with real transformation    
        
        List<Object> matches = new ArrayList<Object>();
        matches.add(resultObject);
        return matches;
    }
    
    /**
     * Calls the given connector to process the list of transformed Objects as 
     * potential XLink matches.
     */
    public static void openPotentialMatches(
            List<Object> modelObjectsDestination, 
            String connectorToCall, 
            String viewToCall,
            OsgiUtilsService osgiService) throws OsgiServiceNotAvailableException, 
            ClassCastException, OpenXLinkException{
        Object serviceObject = osgiService.getService("(service.pid="+connectorToCall+")", 100L);
        if(serviceObject == null) throw new OpenXLinkException();
        LinkableDomain service = (LinkableDomain) serviceObject;
        /*TODO remove after implementation of filter on wicketpage*/
        SecurityContext.login("admin", new Password("password"));
        service.openXLinks(modelObjectsDestination, viewToCall);
    }
    
    /**
     * Returns true, if the transformation between the two defined ModelClasses is possible.
     */
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
