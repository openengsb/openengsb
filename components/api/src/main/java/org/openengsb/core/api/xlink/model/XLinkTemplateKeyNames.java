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

package org.openengsb.core.api.xlink.model;

/**
 * Modelclass defining keyNames which are to be used 
 * when creating a XLinkURL. Transfered via a XLinkTemplate.
 */
public class XLinkTemplateKeyNames {
    
    /**
     * Keyname of the modelClass, which must be concatenated to the baseUrl as 
     * GET Paramter
     */
    private String modelClassKeyName;
    
    /**
     * Keyname of the version of the model, which must be concatenated to the baseUrl as 
     * GET Paramter
     */    
    private String modelVersionKeyName;
    
    /**
     * Keyname of the identifierString of the model, which must be concatenated to the baseUrl as 
     * GET Paramter
     */        
    private String identifierKeyName;
    
    /**
     * Keyname of the contextId, must be set by the tool to determine the OpenEngSB context of the XLink.
     * To select the root context, ad this key with no value.
     */
    private String contextIdKeyName;   
    
    /**
     * Keyname of the viewId, which is to be used for local switching.
     * Must be added, with corresponding value, to the baseUrl as GET-Paramter.
     */    
    private String viewIdKeyName;

    public XLinkTemplateKeyNames(){
        
    }
    
    public XLinkTemplateKeyNames(String modelClassKeyName, 
            String modelVersionKeyName, String identifierKeyName, 
            String contextIdKeyName, String viewIdKeyName) {
        this.modelClassKeyName = modelClassKeyName;
        this.modelVersionKeyName = modelVersionKeyName;
        this.identifierKeyName = identifierKeyName;
        this.contextIdKeyName = contextIdKeyName;
        this.viewIdKeyName = viewIdKeyName;
    }
    
    /**
     * Keyname of the viewId, which is to be used for local switching.
     * Must be added, with corresponding value, to the baseUrl as GET-Paramter.
     */     
    public String getViewIdKeyName() {
        return viewIdKeyName;
    }

    public void setViewIdKeyName(String viewIdKeyName) {
        this.viewIdKeyName = viewIdKeyName;
    }
        
    /**
     * Keyname of the contextId, must be set by the tool to determine the OpenEngSB context of the XLink
     */    
    public String getContextIdKeyName() {
        return contextIdKeyName;
    }

    public void setContextIdKeyName(String contextIdKeyName) {
        this.contextIdKeyName = contextIdKeyName;
    }

    /**
     * Keyname of the identifierString of the model, which must be concatenated to the baseUrl as 
     * GET Paramter
     */   
    public String getIdentifierKeyName() {
        return identifierKeyName;
    }

    public void setIdentifierKeyName(String identifierKeyName) {
        this.identifierKeyName = identifierKeyName;
    }
    
    /**
     * Keyname of the modelClass, which must be concatenated to the baseUrl as 
     * GET Paramter
     */    
    public String getModelClassKeyName() {
        return modelClassKeyName;
    }

    public void setModelClassKeyName(String modelClassKeyName) {
        this.modelClassKeyName = modelClassKeyName;
    }
    
    /**
     * Keyname of the version of the model, which must be concatenated to the baseUrl as 
     * GET Paramter
     */  
    public String getModelVersionKeyName() {
        return modelVersionKeyName;
    }

    public void setModelVersionKeyName(String modelVersionKeyName) {
        this.modelVersionKeyName = modelVersionKeyName;
    }
   
}
