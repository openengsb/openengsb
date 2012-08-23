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
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.api.xlink.exceptions.DomainNotLinkableException;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.core.services.xlink.XLinkUtils;
import org.openengsb.domain.OOSourceCode.model.OOClass;
import org.openengsb.domain.SQLCode.model.SQLCreate;
import org.openengsb.ui.admin.xlink.exceptions.OpenXLinkException;

/**
 * This class mocks the unfinished xlink-functionality
 */
public class XLinkMock {
    
    private OsgiUtilsService osgiService;
    private AuthenticationContext authenticationContext;

    public XLinkMock(OsgiUtilsService osgiService, 
        AuthenticationContext authenticationContext) {
        this.osgiService = osgiService;
        this.authenticationContext = authenticationContext;
    }
    
    public static final String SQLMODEL = SQLCreate.class.getName();
    public static final String OOMODEL = OOClass.class.getName();
 
    /**
     * Transforms the given ModelObject from it´s SourceClass to the defined DestinationModel.
     * TODO [OPENENGSB-2776] replace with real transformation    
     */
    public List<Object> transformModelObject(
            String sourceModelClass,  
            String sourceModelVersion,             
            String destinationModelClass, 
            String destinationModelVersion, 
            Object modelObjectSource) throws ClassNotFoundException, OpenXLinkException {
        if (!isTransformationPossible(sourceModelClass, 
            sourceModelVersion, destinationModelClass, destinationModelVersion)) {
            throw new OpenXLinkException();      
        }
        if (modelObjectSource == null) {
            return null;
        }
        Object resultObject = null;
        Class destinationClass 
            = XLinkUtils.getClassOfOpenEngSBModel(
                destinationModelClass, destinationModelVersion, 
                osgiService);
       
        //########### MOCK start ###########      
        
        if (sourceModelClass.equals(SQLMODEL) && destinationModelClass.equals(OOMODEL)) {
            SQLCreate sqlSource = (SQLCreate) modelObjectSource;
            OOClass ooclass = (OOClass) XLinkUtils.createEmptyInstanceOfModelClass(destinationClass);
            ooclass.setClassName(sqlSource.getTableName());
            resultObject = ooclass;
        } else if (sourceModelClass.equals(OOMODEL) && destinationModelClass.equals(SQLMODEL)) {
            OOClass ooSource = (OOClass) modelObjectSource;
            SQLCreate sqlcreate = (SQLCreate) XLinkUtils.createEmptyInstanceOfModelClass(destinationClass);
            sqlcreate.setTableName(ooSource.getClassName());
            resultObject = sqlcreate;
        } else if (sourceModelClass.equals(OOMODEL) && destinationModelClass.equals(OOMODEL)) {
            OOClass ooSource = (OOClass) modelObjectSource;
            resultObject = ooSource;
        } else if (sourceModelClass.equals(SQLMODEL) && destinationModelClass.equals(SQLMODEL)) {
            SQLCreate sqlSource = (SQLCreate) modelObjectSource;
            resultObject = sqlSource;
        }
        
        //########### MOCK end ########### 
        
        List<Object> matches = new ArrayList<Object>();
        matches.add(resultObject);
        return matches;
    }
    
    /**
     * Calls the given connector to process the list of transformed Objects as 
     * potential XLink matches.
     * TODO [OPENENGSB-3267] remove dummy login after implementation of filter on wicketpage
     */
    public void openPotentialMatches(
            List<Object> modelObjectsDestination, 
            String connectorToCall, 
            String viewToCall) throws OsgiServiceNotAvailableException, 
            ClassCastException, OpenXLinkException, DomainNotLinkableException {
        Object serviceObject = osgiService.getService("(service.pid=" + connectorToCall + ")", 100L);
        if (serviceObject == null) {
            throw new OpenXLinkException();
        }
        LinkableDomain service;
        try {
            service = (LinkableDomain) serviceObject;
        } catch (Exception e) {
            throw new DomainNotLinkableException();
        } 
        authenticationContext.login("admin", new Password("password"));
        service.openXLinks(modelObjectsDestination.toArray(), viewToCall);
    }
    
    /**
     * Returns true, if the transformation between the two defined ModelClasses is possible.
     * TODO [OPENENGSB-2776] implement real check here
     */
    public boolean isTransformationPossible(
            String srcModelClass, 
            String srcModelVersion, 
            String destModelClass, 
            String destModelVersion) {
        //########### MOCK start ###########
        if (!srcModelClass.equals(ExampleObjectOrientedModel.class.getName()) 
            && destModelClass.equals(ExampleObjectOrientedModel.class.getName())) {
            return false;
        }
        //########### MOCK end ###########
        return true;
    }   
    
    /**
     * Returns the FieldNames of the given ModelDescription (e.g. ModelClass and ModelVersion) which
     * are marked as identifying Fields for XLink.
     * TODO [OPENENGSB-3266] fetch real xlink identifiers instead all of them
     */
    public List<String> getModelIdentifierToModelDescription(
            String modelId, 
            String versionId) throws ClassNotFoundException {
        Class clazz = XLinkUtils.getClassOfOpenEngSBModel(modelId, versionId, osgiService);
        Object model = XLinkUtils.createEmptyInstanceOfModelClass(clazz);
        List<OpenEngSBModelEntry> entries = ModelUtils.getOpenEngSBModelEntries(model);
        List<String> identifierKeyNames = new ArrayList<String>();
        
        //########### MOCK start ###########
        
        if (modelId.equals(XLinkMock.OOMODEL)) {
            identifierKeyNames.add("className");
            identifierKeyNames.add("attributes");
            return identifierKeyNames;
        }        
        
        //########### MOCK end ###########
        
        for (OpenEngSBModelEntry entry : entries) {
            identifierKeyNames.add(entry.getKey());
        }
        return identifierKeyNames;
    }        
    
}
