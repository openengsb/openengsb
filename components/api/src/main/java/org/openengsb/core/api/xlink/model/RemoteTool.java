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


/**
 * Modelclass to transfere information about remote tools, registered for XLink.
 * <br/><br/>
 * Carries the ToolName, the ConnectorId of a tool and the views it offers for XLink.
 * A remote Host should only receive information about it´s local tools.
 */
public class RemoteTool implements Serializable {
    
    // @extract-start XLinkRegisteredTool
    /**
     * Id of the connector, identifying the tool
     */
    private String id;
    
    /**
     * Human readable name of the tool, may be null
     */
    private String toolName;
    
    /**
     * Views the tool offers for XLink, represented as keyNames and short descriptions
     */
    private List<RemoteToolView> availableViews;
    
    // @extract-end

    public RemoteTool() {
    }

    public RemoteTool(String id, String toolName, List<RemoteToolView> availableViews) {
        this.id = id;
        this.toolName = toolName;
        this.availableViews = availableViews;
    }

    /**
     * Views the tool offers for XLink, represented as keyNames and short descriptions
     */    
    public List<RemoteToolView> getAvailableViews() {
        return availableViews;
    }

    public void setAvailableViews(List<RemoteToolView> availableViews) {
        this.availableViews = availableViews;
    }

    /**
     * Id of the connector, identifying the tool
     */    
    public String getId() {
        return id;
    }

    public void setId(String id) {
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
