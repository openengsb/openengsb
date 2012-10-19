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

import java.io.Serializable;
import java.util.Map;

import org.openengsb.core.api.model.ModelDescription;

/**
 * Modelclass to store a XLink registration, of a connector, to XLink.
 */
public class XLinkConnectorRegistration implements Serializable {
    
    /**
     * HostId of the client, provided during registration.
     * This is currently the IP of the host.
     */
    private String hostId;
    
    /**
     * Id of the connector, identifying the tool
     */    
    private String connectorId;
    
    /**
     * Human readable name of the tool, may be null.
     */    
    private String toolName;
    
    /**
     * Model/View associations, provided by the client during registration
     */
    private Map<ModelDescription, XLinkConnectorView[]> modelsToViews;
    
    /**
     * BluePrint that was generated and returned to the client during registration
     */
    private XLinkUrlBlueprint xLinkTemplate;

    public XLinkConnectorRegistration(String hostId, String connectorId, String toolName, 
            Map<ModelDescription, XLinkConnectorView[]> modelsToViews, XLinkUrlBlueprint xLinkTemplate) {
        this.hostId = hostId;
        this.connectorId = connectorId;
        this.toolName = toolName;
        this.modelsToViews = modelsToViews;
        this.xLinkTemplate = xLinkTemplate;
    }
    
    /**
     * Id of the connector, identifying the tool
     */   
    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }
    
    /**
     * HostId of the client, provided during registration.
     * This is currently the IP of the host.
     */
    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }
    
    /**
     * Model/View associations, provided by the client during registration
     */
    public Map<ModelDescription, XLinkConnectorView[]> getModelsToViews() {
        return modelsToViews;
    }

    public void setModelsToViews(Map<ModelDescription, XLinkConnectorView[]> modelsToViews) {
        this.modelsToViews = modelsToViews;
    }
    
    /**
     * Human readable name of the tool, may be null.
     */  
    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }
    
    /**
     * BluePrint that was generated and returned to the client during registration
     */
    public XLinkUrlBlueprint getxLinkTemplate() {
        return xLinkTemplate;
    }

    public void setxLinkTemplate(XLinkUrlBlueprint xLinkTemplate) {
        this.xLinkTemplate = xLinkTemplate;
    }
    
}
