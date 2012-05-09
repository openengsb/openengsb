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
import java.util.List;
import org.openengsb.core.api.model.ConnectorId;

/**
 * Modelclass to transfere information about a tool that is registered for XLink.
 * Carries the ConnectorId of the tool and the views it offers for XLink, represented
 * as a HashMap of keyNames and short descriptions. The HostId of the tool is not 
 * needed, since this information is only sent to tools of the same host.
 */
public class XLinkLocalTool implements Serializable{
    
    // @extract-start XLinkRegisteredTool
    /**
     * Id of the connector, identifying the tool
     */
    private ConnectorId id;
    
    /**
     * Human readable name of the tool
     */
    private String toolName;
    
    /**
     * Views the tool offers for XLink, represented as keyNames and short descriptions
     */
    private List<XLinkToolView> availableViews;
    
    // @extract-end

    public XLinkLocalTool() {
    }

    public XLinkLocalTool(ConnectorId id, String toolName, List<XLinkToolView> availableViews) {
        this.id = id;
        this.toolName = toolName;
        this.availableViews = availableViews;
    }

    /**
     * Views the tool offers for XLink, represented as keyNames and short descriptions
     */    
    public List<XLinkToolView> getAvailableViews() {
        return availableViews;
    }

    public void setAvailableViews(List<XLinkToolView> availableViews) {
        this.availableViews = availableViews;
    }

    /**
     * Id of the connector, identifying the tool
     */    
    public ConnectorId getId() {
        return id;
    }

    public void setId(ConnectorId id) {
        this.id = id;
    }

    /**
     * Name of the tool, may be null
     */    
    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }
    
}
