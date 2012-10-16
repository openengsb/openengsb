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

import org.openengsb.core.api.model.ModelDescription;


/**
 * Modelclass of a XLinkTemplate. <br/>
 * Created and transfered to each remote Client that participates in XLinking. Contains the
 * necessary Information to create valid XLink-URLs. Defines the BaseURL to the XLink 
 * HTTP-Servlet, required keynames, view/model associations and a List of all other currently 
 * registered tools from the same host.
 * <br/><br/>
 * The list of registered tools can be used to support 'local-switching' between local tools. 
 */
public class XLinkTemplate {
    
    // @extract-start XLinkTemplate
    /**
     * URL to the Registry´s HTTP-Servlet without necessary GET-Parameters. 
     * Already contains the expirationDate of the Link as GET-Parameter. 
     * <br/><br/>
     * XLink-URLs expire after a certain amount of days.
     */
    private String baseUrl;
    
    /**
     * Map with the available viewId as key and the assigned modelclass as value.
     * This Map defines which Model is to be used for which view.  
     */
    private Map<String, ModelDescription> viewToModels;
     
    /**
     * List of all other currently registered tools from the same host. 
     * <br/><br/>
     * This list of registered tools can be used to support 
     * 'local-switching' between local tools.
     */
    private List<RemoteTool> registeredTools; 
    
    /**
     * Key/value combination of the connectorId in HTTP GET paramater syntax.
     * Must be concatenated to the baseUrl, when the generated XLink should 
     * to be used for 'local-switching'.
     */
    private String connectorId;
    
    /**
     * Contains a set of Keynames, which are to be used for
     * constructing valid XLinkURLs.
     */
    private XLinkTemplateKeyNames keyNames;
    
    // @extract-end

    public XLinkTemplate() {
    }

    public XLinkTemplate(String baseUrl, Map<String, ModelDescription> viewToModels, 
            List<RemoteTool> registeredTools, String connectorId, 
            XLinkTemplateKeyNames keyNames) {
        this.baseUrl = baseUrl;
        this.viewToModels = viewToModels;
        this.registeredTools = registeredTools;
        this.connectorId = connectorId;
        this.keyNames = keyNames;
    }

    /**
     * URL to the Registry´s HTTP-Servlet without necessary GET-Parameters. 
     * Already contains the expirationDate of the Link as GET-Parameter. 
     * <br/><br/>
     * XLink-URLs expire after a certain amount of days.
     */ 
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    /**
     * Key/value combination of the connectorId in HTTP GET paramater syntax.
     * Must be concatenated to the baseUrl, when the generated XLink should 
     * to be used for 'local-switching'.
     */
    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    /**
     * List of all other currently registered tools from the same host. 
     * <br/><br/>
     * This list of registered tools can be used to support 
     * 'local-switching' between local tools.
     */  
    public List<RemoteTool> getRegisteredTools() {
        return registeredTools;
    }

    public void setRegisteredTools(List<RemoteTool> registeredTools) {
        this.registeredTools = registeredTools;
    }

    /**
     * Map with the available viewId as key and the assigned modelclass as value.
     * This Map defines which Model is to be used for which view.  
     */   
    public Map<String, ModelDescription> getViewToModels() {
        return viewToModels;
    }

    public void setViewToModels(Map<String, ModelDescription> viewToModels) {
        this.viewToModels = viewToModels;
    }
    
    /**
     * Contains a set of Keynames, which are to be used for
     * constructing valid XLinkURLs.
     */
    public XLinkTemplateKeyNames getKeyNames() {
        return keyNames;
    }

    public void setKeyNames(XLinkTemplateKeyNames keyNames) {
        this.keyNames = keyNames;
    }

}

