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

import java.util.List;
import java.util.Map;


/**
 * Modelclass for the XLinkTemplate definitions. Transfered to each Tool that participates in XLinking. Defines the
 * baseURL to the XLink HTTP-Servlet, view/model associations and a List of all other 
 * currently registered tools from the same host. This last information is used to support local switching between
 * tools. To create valid XLink-URLs, the identifing fields of a model must be concatenated to the baseURL as 
 * GET-Parameters.
 */
public class XLinkTemplate {
    
    // @extract-start XLinkTemplate
    /**
     * URL to the Registry´s HTTP-Servlet without the Identifier´s fields. Already contains the modelId of the
     * sourceModel and the expirationDate of the Link as GET-Parameters. XLink-URLs expire after a certain amount of
     * days.
     */
    private String baseUrl;
    
    /**
     * Map with the available viewId as key and the assigned modelclass as value.
     * In this way the OpenEngSB defines which Model is to be used for which 
     * view. When in use, the identifing fields of the model must be concatenated 
     * with their values to the baseUrl as GET-Parameters. 
     */
    private Map<String, XLinkModelInformation> viewToModels;
    
    /**
     * Keyname of the modelClass, which must be concatenated to the baseUrl as 
     * GET Paramter
     */
    private String modelClassKey;
    
    /**
     * Keyname of the version of the model, which must be concatenated to the baseUrl as 
     * GET Paramter
     */    
    private String modelVersionKey;
    
    /**
     * List of all other currently registered tools from the same host. This information is used to support 
     * local switching between tools.
     */
    private List<XLinkLocalTool> registeredTools;
    
    /**
     * Keyname of the contextId, must be set by the tool to determine the OpenEngSB context of the XLink.
     * To select the root context, ad this key with no value.
     */
    private String contextIdKeyName;    
    
    /**
     * Key/value combination of the connectorId in HTTP GET paramater syntax.
     * Simply concatenate to the baseUrl, when the XLink is 
     * to be used for local switching.
     */
    private String connectorId;
    
    /**
     * Keyname of the viewId, which is to be used for local switching.
     * Must be added, with corresponding value, to the baseUrl as GET-Paramter.
     */    
    private String viewIdKeyName;
    
    // @extract-end

    public XLinkTemplate() {
    }

    public XLinkTemplate(String baseUrl, 
            Map<String, XLinkModelInformation> viewToModels, 
            String modelClassKey, 
            String modelVersionKey, 
            List<XLinkLocalTool> registeredTools, 
            String contextIdKeyName, 
            String connectorId, 
            String viewIdKeyName) {
        this.baseUrl = baseUrl;
        this.viewToModels = viewToModels;
        this.modelClassKey = modelClassKey;
        this.modelVersionKey = modelVersionKey;
        this.registeredTools = registeredTools;
        this.contextIdKeyName = contextIdKeyName;
        this.connectorId = connectorId;
        this.viewIdKeyName = viewIdKeyName;
    }

    /**
     * URL to the Registry´s HTTP-Servlet without the Identifier´s fields. Already contains the modelId of the
     * sourceModel and the expirationDate of the Link as GET-Parameters. XLink-URLs expire after a certain amount of
     * days.
     */    
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    /**
     * Key/value combination of the connectorId in HTTP GET paramater syntax.
     * Simply concatenate to the baseUrl, when the XLink is 
     * to be used for local switching.
     */
    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    /**
     * List of all other currently registered tools from the same host. This information is used to support 
     * local switching between tools.
     */    
    public List<XLinkLocalTool> getRegisteredTools() {
        return registeredTools;
    }

    public void setRegisteredTools(List<XLinkLocalTool> registeredTools) {
        this.registeredTools = registeredTools;
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
     * Map with the available viewId as key and the assigned modelclass as value.
     * In this way the OpenEngSB defines which Model is to be used for which 
     * view. When in use, the identifing fields of the model must be concatenated 
     * with their values to the baseUrl as GET-Parameters. 
     */    
    public Map<String, XLinkModelInformation> getViewToModels() {
        return viewToModels;
    }

    public void setViewToModels(Map<String, XLinkModelInformation> viewToModels) {
        this.viewToModels = viewToModels;
    }

    /**
     * Keyname of the modelClass, which must be concatenated to the baseUrl as 
     * GET Paramter
     */    
    public String getModelClassKey() {
        return modelClassKey;
    }

    public void setModelClassKey(String modelClassKey) {
        this.modelClassKey = modelClassKey;
    }
    
    /**
     * Keyname of the version of the model, which must be concatenated to the baseUrl as 
     * GET Paramter
     */  
    public String getModelVersionKey() {
        return modelVersionKey;
    }

    public void setModelVersionKey(String modelVersionKey) {
        this.modelVersionKey = modelVersionKey;
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
    
}

