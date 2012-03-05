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

package org.openengsb.core.api.xlink;

import java.util.List;


/**
 * Modelclass for the XLinkTemplate definitions. Transfered to each Tool that participates in XLinking. Defines the
 * baseURL to the XLink HTTP-Servlet, a list of Keynames used to identify modelObjects and a List of all other 
 * currently registered tools from the same host. This information is used to support local switching between
 * tools. To create valid XLink-URLs, the KeyNames with their values must be concatenated to the baseURL as 
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
     * List containing keynames that determine how single modelObjects are identified. Must be concatenated with 
     * their values to the baseUrl as GET-Parameters. The keyNames correspond to existing keys in the 
     * toolenviroment.
     */
    private List<String> keyNames;
    
    /**
     * List of all other currently registered tools from the same host. This information is used to support 
     * local switching between tools.
     */
    private List<XLinkRegisteredTools> registeredTools;
    
    /**
     * Keyname of the connectorId, which is to be used for local switching.
     * Must be added, with corresponding value, to the baseUrl as GET-Paramter.
     */
    private String connectorIdKeyName;
    
    /**
     * Keyname of the viewId, which is to be used for local switching.
     * Must be added, with corresponding value, to the baseUrl as GET-Paramter.
     */    
    private String viewIdKeyName;
    
    // @extract-end

    public XLinkTemplate() {
    }

    public XLinkTemplate(String baseUrl, List<String> keyNames, List<XLinkRegisteredTools> registeredTools, 
            String connectorIdKeyName, String viewIdKeyName) {
        this.baseUrl = baseUrl;
        this.keyNames = keyNames;
        this.registeredTools = registeredTools;
        this.connectorIdKeyName = connectorIdKeyName;
        this.viewIdKeyName = viewIdKeyName;
    }

    /**
     * Keyname of the connectorId, which is to be used for local switching.
     * Must be added, with corresponding value, to the baseUrl as GET-Paramter.
     */    
    public String getConnectorIdKeyName() {
        return connectorIdKeyName;
    }

    public void setConnectorIdKeyName(String connectorIdKeyName) {
        this.connectorIdKeyName = connectorIdKeyName;
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
     * URL to the Registry´s HTTP-Servlet without the Identifier Fields as Parameters
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * List containing keynames that determine how single modelObjects are identified. Must be concatenated to the
     * baseUrl as GET-Parameters.
     */
    public List<String> getKeyNames() {
        return keyNames;
    }

    public void setKeyNames(List<String> keyNames) {
        this.keyNames = keyNames;
    }

    /**
     * List of all other currently registered tools from the same host. This information can be used to support 
     * local switching between tools.
     */    
    public List<XLinkRegisteredTools> getRegisteredTools() {
        return registeredTools;
    }

    public void setRegisteredTools(List<XLinkRegisteredTools> registeredTools) {
        this.registeredTools = registeredTools;
    }

}

