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
 * Simple Modelclass to store a registration to XLink
 */
public class XLinkToolRegistration {
    
    /**
     * HostId of the client, provided during registration
     */
    private String hostId;
    /**
     * Id of the connector, identifying the tool
     */    
    private String connectorId;
    /**
     * Human readable name of the tool
     */    
    private String toolName;
    /**
     * Model/View associations, provided by the client during registration
     */
    private Map<XLinkModelInformation, List<XLinkToolView>> modelsToViews;
    /**
     * Template that was generated and returned to the client during registration
     */
    private XLinkTemplate xLinkTemplate;

    public XLinkToolRegistration(String hostId, String connectorId, String toolName, 
            Map<XLinkModelInformation, List<XLinkToolView>> modelsToViews, XLinkTemplate xLinkTemplate) {
        this.hostId = hostId;
        this.connectorId = connectorId;
        this.toolName = toolName;
        this.modelsToViews = modelsToViews;
        this.xLinkTemplate = xLinkTemplate;
    }

    public String getConnectorId() {
        return connectorId;
    }

    public void setConnectorId(String connectorId) {
        this.connectorId = connectorId;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public Map<XLinkModelInformation, List<XLinkToolView>> getModelsToViews() {
        return modelsToViews;
    }

    public void setModelsToViews(Map<XLinkModelInformation, List<XLinkToolView>> modelsToViews) {
        this.modelsToViews = modelsToViews;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public XLinkTemplate getxLinkTemplate() {
        return xLinkTemplate;
    }

    public void setxLinkTemplate(XLinkTemplate xLinkTemplate) {
        this.xLinkTemplate = xLinkTemplate;
    }
    
}
