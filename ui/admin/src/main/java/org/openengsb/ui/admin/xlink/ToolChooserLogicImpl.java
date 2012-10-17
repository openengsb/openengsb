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

package org.openengsb.ui.admin.xlink;

import java.util.List;

import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.xlink.internal.XLinkConnectorManager;
import org.openengsb.core.api.xlink.internal.ui.ToolChooserLogic;
import org.openengsb.core.api.xlink.model.XLinkConnector;
import org.openengsb.core.api.xlink.model.XLinkConnectorRegistration;
import org.openengsb.core.api.xlink.model.XLinkUrlBlueprint;
import org.openengsb.core.services.xlink.XLinkUtils;

/**
 * This class supplies the logic to process a call from an XLink URL.
 */
public class ToolChooserLogicImpl implements ToolChooserLogic {
    
    private XLinkConnectorManager serviceManager;

    public ToolChooserLogicImpl(XLinkConnectorManager serviceManager) {
        this.serviceManager = serviceManager;
    }
    
    @Override
    public List<XLinkConnector> getRegisteredToolsFromHost(String hostId) {
        return XLinkUtils.getLocalToolFromRegistrations(
                serviceManager.getXLinkRegistration(hostId));
    } 
    
    @Override
    public ModelDescription getModelClassOfView(String hostId, String connectorId, String viewId) {
        if (getRegistration(hostId, connectorId) != null) {
            XLinkUrlBlueprint template = getRegistration(hostId, connectorId).getxLinkTemplate();
            return template.getViewToModels().get(viewId);            
        }
        return null;
    }    
    
    /**
     * Returns the Registrationdata to a given HostId and ConnectorId.
     * Returns null, if no XLinkRegistration was found.
     */
    private XLinkConnectorRegistration getRegistration(String hostId, String connectorId) {
        for (XLinkConnectorRegistration registration : serviceManager.getXLinkRegistration(hostId)) {
            if (registration.getConnectorId().equals(connectorId)) {
                return registration;
            }
        }
        return null;
    }
    
    @Override
    public boolean isConnectorRegistrated(String hostId, String connectorId) {
        for (XLinkConnectorRegistration registration : serviceManager.getXLinkRegistration(hostId)) {
            if (registration.getConnectorId().equals(connectorId)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean isViewExisting(String hostId, String connectorId, String viewId) {
        for (XLinkConnectorRegistration registration : serviceManager.getXLinkRegistration(hostId)) {
            if (registration.getConnectorId().equals(connectorId)) {
                return registration.getxLinkTemplate().getViewToModels().containsKey(viewId);
            }
        }
        return false;
    }    
    
}
