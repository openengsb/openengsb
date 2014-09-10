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
import java.util.Objects;

/**
 * Modelclass to transfer information about local XLink registrations to other remote connectors. <br/>
 * <br/>
 * Carries the ToolName, the ConnectorId of a tool and the views it offers for XLink. A remote connector should only
 * receive information about it´s local tools.
 */
public class XLinkConnector implements Serializable {
    private static final long serialVersionUID = 1L;

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
    private XLinkConnectorView[] availableViews;

    public XLinkConnector() {
    }

    public XLinkConnector(String id, String toolName, XLinkConnectorView[] availableViews) {
        this.id = id;
        this.toolName = toolName;
        this.availableViews = availableViews;
    }

    /**
     * Views the tool offers for XLink, represented as keyNames and short descriptions
     */
    public XLinkConnectorView[] getAvailableViews() {
        return availableViews;
    }

    public void setAvailableViews(XLinkConnectorView[] availableViews) {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof XLinkConnector) {
            final XLinkConnector other = (XLinkConnector) obj;
            return Objects.equals(id, other.id)
                    && Objects.equals(toolName, other.toolName)
                    && Objects.equals(availableViews, other.availableViews);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, toolName, availableViews);
    }

}
