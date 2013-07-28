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
import java.util.Map.Entry;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.LinkingSupport;
import org.openengsb.core.api.OsgiServiceNotAvailableException;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.api.xlink.exceptions.DomainNotLinkableException;
import org.openengsb.core.api.xlink.exceptions.OpenXLinkException;
import org.openengsb.core.api.xlink.service.ui.XLinkMock;
import org.openengsb.core.services.xlink.XLinkUtils;
import org.openengsb.core.util.ModelUtils;
import org.openengsb.domain.OOSourceCode.model.OOClass;
import org.openengsb.domain.OOSourceCode.model.OOVariable;
import org.openengsb.domain.SQLCode.model.SQLCreate;
import org.openengsb.domain.SQLCode.model.SQLCreateField;

public class XLinkMockImpl implements XLinkMock {
    
    private OsgiUtilsService osgiService;
    private AuthenticationContext authenticationContext;

    public XLinkMockImpl(OsgiUtilsService osgiService, 
        AuthenticationContext authenticationContext) {
        this.osgiService = osgiService;
        this.authenticationContext = authenticationContext;
    }
    
    public static final String SQLMODEL = SQLCreate.class.getName();
    public static final String OOMODEL = OOClass.class.getName();
 
    /**
     * TODO [OPENENGSB-2776] replace with real transformation    
     * 
     * After fix, move function to ToolChooserLogicImpl.class
     * @see org.openengsb.ui.admin.xlink.ToolChooserLogicImpl
     */
    @Override
    public List<Object> transformModelObject(String sourceModelClass, String sourceModelVersion,             
            String destinationModelClass, String destinationModelVersion, 
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
        
        if (!destinationClass.getName().equals(destinationModelClass)) {
            throw new OpenXLinkException();
        }
       
        //########### MOCK start ###########      
        
        if (sourceModelClass.equals(SQLMODEL) && destinationModelClass.equals(OOMODEL)) {
            SQLCreate sqlSource = (SQLCreate) modelObjectSource;
            resultObject = hardwiredTransformationToOOClass(sqlSource, destinationClass);
        } else if (sourceModelClass.equals(OOMODEL) && destinationModelClass.equals(SQLMODEL)) {
            OOClass ooSource = (OOClass) modelObjectSource;
            resultObject = hardwiredTransformationToSQLCreate(ooSource, destinationClass);
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
     * Utility map for hardwired transformation
     * TODO [OPENENGSB-2776] remove this map when real transformation is available
     */    
    Map<String, String> nameMappingSQLToOOClass = new HashMap<String, String>() { 
        { 
            put("ClientContract", "Business_Order");
            put("Client", "Customer");
            put("ProducedProduct", "Order_Position");
            put("ProductionMachine", "Facility_Machine");
            put("ProductionFacility", "Production");
            put("ProductionChain", "Production");
            put("ProductionPlan", "Production");
        } 
    };
    
    /**
     * Utility map for hardwired transformation
     * TODO [OPENENGSB-2776] remove this map when real transformation is available
     */            
    Map<String, String> nameMappingOOClassToSQL = new HashMap<String, String>() { 
        { 
            put("Business_Order", "ClientContract");
            put("Customer", "Client");
            put("Order_Position", "ProducedProduct");
            put("Facility_Machine", "ProductionMachine");
            put("Production_Facility", "Production");
            put("Production_Chain", "Production");
            put("Production_Plan", "Production");
        } 
    };    
    
    /**
     * Utility map for hardwired transformation
     * TODO [OPENENGSB-2776] remove this map when real transformation is available
     */        
    Map<String, String> attributeTypeMappingSQLtoOOClass = new HashMap<String, String>() { 
        { 
            put("VARCHAR", "string");
            put("INTEGER", "int");
            put("FLOAT", "double");
            put("BIT", "bool");
            put("DATE", "DateTime");
            put("BIGINT", "long");
        } 
    };

    /**
     * Utility map for hardwired transformation
     * TODO [OPENENGSB-2776] remove this map when real transformation is available
     */        
    Map<String, String> attributeTypeMappingOOClassToSQL = invertMap(attributeTypeMappingSQLtoOOClass);
 
    /**
     * Utility map for hardwired transformation
     * TODO [OPENENGSB-2776] remove this map when real transformation is available
     */        
    Map<String, String> attributeMappingNameSQLtoOOClass = new HashMap<String, String>() { 
        { 
            put("cc_Id", "bo_Id");
            put("client", "customer_id");
            put("dateOfCreation", "creationDate");
            put("company", "organisation");
            put("pc_Id", "cm_Id");
            put("chainName", "nameOfChain");
            put("duration", "executionTime");
            put("pp_Id", "pm_Id");
            put("sp_Id", "fo_Id");
            put("productLabel", "itemName");
            put("count", "outputAmount");
            put("productionDate", "outputDate");  
            put("productionMachine", "productionFacility_Id");   
        } 
    };    

    /**
     * Utility map for hardwired transformation
     * TODO [OPENENGSB-2776] remove this map when real transformation is available
     */        
    Map<String, String> attributeNameMappingOOClassToSQL = invertMap(attributeMappingNameSQLtoOOClass);        
        
    /**
     * Utility method for hardwired transformation
     * TODO [OPENENGSB-2776] remove this method when real transformation is available
     */
    private OOClass hardwiredTransformationToOOClass(SQLCreate sqlSource, Class destinationClass) {
        OOClass ooclass = (OOClass) XLinkUtils.createEmptyInstanceOfModelClass(destinationClass);
        
        //transform name
        if (nameMappingSQLToOOClass.get(sqlSource.getTableName()) != null) {
            ooclass.setClassName(nameMappingSQLToOOClass.get(sqlSource.getTableName()));
        } else {
            ooclass.setClassName(sqlSource.getTableName());
        }
        
        //transform attribute names & types
        OOVariable[] transformedVariables = new OOVariable[sqlSource.getFields().length];
        for (int i = 0; i < transformedVariables.length ; i++) {
            transformedVariables[i] = new OOVariable();
            if (attributeTypeMappingSQLtoOOClass.get(sqlSource.getFields()[i].getFieldType()) != null) {
                transformedVariables[i].setType(
                        attributeTypeMappingSQLtoOOClass.get(sqlSource.getFields()[i].getFieldType()));
            } else if (sqlSource.getFields()[i].getFieldType().contains("VARCHAR")) {
                transformedVariables[i].setType("string");
            } else {
                transformedVariables[i].setType(sqlSource.getFields()[i].getFieldType());
            }
            
            if (attributeMappingNameSQLtoOOClass.get(sqlSource.getFields()[i].getFieldName()) != null) {
                transformedVariables[i].setName(
                        attributeMappingNameSQLtoOOClass.get(sqlSource.getFields()[i].getFieldName()));
            } else {
                transformedVariables[i].setName(sqlSource.getFields()[i].getFieldName());
            }            
            
            //dummy values since this field is not used in the demonstration
            transformedVariables[i].setIsFinal(false); 
            transformedVariables[i].setIsStatic(false);
        }
        ooclass.setAttributes(transformedVariables);
        
        //dummy values since information for this field is not available
        ooclass.setPackageName("");
        ooclass.setMethods(new StringBuffer(""));
        return ooclass;
    }
    
    /**
     * Utility method for hardwired transformation
     * TODO [OPENENGSB-2776] remove this method when real transformation is available
     */    
    private SQLCreate hardwiredTransformationToSQLCreate(OOClass ooSource, Class destinationClass) {
        SQLCreate sqlcreate = (SQLCreate) XLinkUtils.createEmptyInstanceOfModelClass(destinationClass);
        
        //transform name
        if (nameMappingOOClassToSQL.get(ooSource.getClassName()) != null) {
            sqlcreate.setTableName(nameMappingOOClassToSQL.get(ooSource.getClassName()));
        } else {
            sqlcreate.setTableName(ooSource.getClassName());
        }
        
        //transform attribute names & types
        SQLCreateField[] transformedFields = new SQLCreateField[ooSource.getAttributes().length];
        for (int i = 0; i < transformedFields.length ; i++) {
            transformedFields[i] = new SQLCreateField();
            
            if (attributeTypeMappingOOClassToSQL.get(ooSource.getAttributes()[i].getType()) != null) {
                transformedFields[i].setFieldType(
                        attributeTypeMappingOOClassToSQL.get(ooSource.getAttributes()[i].getType()));
            } else {
                transformedFields[i].setFieldType(ooSource.getAttributes()[i].getType());
            }
            
            if (attributeNameMappingOOClassToSQL.get(ooSource.getAttributes()[i].getName()) != null) {
                transformedFields[i].setFieldName(
                        attributeNameMappingOOClassToSQL.get(ooSource.getAttributes()[i].getName()));
            } else {
                transformedFields[i].setFieldName(ooSource.getAttributes()[i].getName());
            }            
            
            //dummy values since information for this field is not available
            transformedFields[i].setConstraints(new String[0]);
        }
        sqlcreate.setFields(transformedFields);      
        return sqlcreate;
    }
    
    /**
     * Utility method for hardwired transformation
     * TODO [OPENENGSB-2776] remove this method when real transformation is available
     */
    private <V, K> Map<V, K> invertMap(Map<K, V> map) {
        Map<V, K> inv = new HashMap<V, K>();
        for (Entry<K, V> entry : map.entrySet()) {
            inv.put(entry.getValue(), entry.getKey());
        }
        return inv;
    }
    
    /**
     * TODO [OPENENGSB-3267] remove dummy login after implementation of filter on wicketpage
     * 
     * After fix, move function to ToolChooserLogicImpl.class
     * @see org.openengsb.ui.admin.xlink.ToolChooserLogicImpl
     */
    @Override
    public void openPotentialMatches(List<Object> modelObjectsDestination, 
            String connectorToCall, String viewToCall) throws OsgiServiceNotAvailableException, 
            ClassCastException, OpenXLinkException, DomainNotLinkableException {
        Object serviceObject = osgiService.getService("(service.pid=" + connectorToCall + ")", 100L);
        if (serviceObject == null) {
            throw new OpenXLinkException();
        }
        LinkingSupport service;
        try {
            service = (LinkingSupport) serviceObject;
        } catch (Exception e) {
            throw new DomainNotLinkableException();
        } 
        authenticationContext.login("admin", new Password("password"));
        service.openXLinks(modelObjectsDestination.toArray(), viewToCall);
    }
    
    /**
     * TODO [OPENENGSB-2776] implement real check here
     * 
     * After fix, move function to ToolChooserLogicImpl.class
     * @see org.openengsb.ui.admin.xlink.ToolChooserLogicImpl
     */
    @Override
    public boolean isTransformationPossible(String srcModelClass, String srcModelVersion, 
            String destModelClass, String destModelVersion) {
        //########### MOCK start ###########
        if (!srcModelClass.equals(ExampleObjectOrientedModel.class.getName()) 
            && destModelClass.equals(ExampleObjectOrientedModel.class.getName())) {
            return false;
        }
        //########### MOCK end ###########
        return true;
    }   
    
    /**
     * TODO [OPENENGSB-3266] fetch real xlink identifiers instead all of them
     * 
     * After fix, move function to ToolChooserLogicImpl.class
     * @see org.openengsb.ui.admin.xlink.ToolChooserLogicImpl
     */
    @Override
    public List<String> getModelIdentifierToModelDescription(
            String modelId, String versionId) throws ClassNotFoundException {
        Class clazz = XLinkUtils.getClassOfOpenEngSBModel(modelId, versionId, osgiService);
        Object model = XLinkUtils.createEmptyInstanceOfModelClass(clazz);
        List<OpenEngSBModelEntry> entries = ModelUtils.toOpenEngSBModelEntries(model);
        List<String> identifierKeyNames = new ArrayList<String>();
        //########### MOCK start ###########
        
        if (modelId.equals(XLinkMockImpl.OOMODEL)) {
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
