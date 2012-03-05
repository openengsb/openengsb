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

import java.util.HashMap;

import org.openengsb.core.api.model.ConnectorId;

/**
 * Modelclass to transfere information about a tool that is registered for XLink.
 * Carries the ConnectorId of the tool and the views it offers for XLink, represented
 * as a HashMap of keyNames and short descriptions. The HostId of the tool is not 
 * needed, since this information is only sent to tools of the same host.
 */
public class XLinkRegisteredTools {
    
    // @extract-start XLinkRegisteredTools
    /**
     * Id of the connector, identifying the tool
     */
    private ConnectorId id;
    
    /**
     * Name of the tool, may be null
     */
    private String toolName;
    
    /**
     * Views the tool offers for XLink, represented as keyNames and short descriptions
     */
    private HashMap<String, String> availableViews;
    
    // @extract-end

    /**
     * Views the tool offers for XLink, represented as keyNames and short descriptions
     */    
    public HashMap<String, String> getAvailableViews() {
        return availableViews;
    }

    public void setAvailableViews(HashMap<String, String> availableViews) {
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
