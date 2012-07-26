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

import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.ekb.ModelDescription;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.xlink.model.XLinkLocalTool;
import org.openengsb.core.api.xlink.model.XLinkTemplate;
import org.openengsb.core.api.xlink.model.XLinkToolRegistration;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.core.common.xlink.XLinkUtils;
import org.openengsb.ui.admin.xlink.mocking.XLinkMock;

public class ToolChooserLogic {
    
    private ConnectorManager serviceManager;
    private OsgiUtilsService osgiService;

    public ToolChooserLogic(ConnectorManager serviceManager, OsgiUtilsService osgiService) {
        this.serviceManager = serviceManager;
        this.osgiService = osgiService;
    }
    
    public List<XLinkLocalTool> getRegisteredToolsFromHost(String hostId) {
        return XLinkUtils.getLocalToolFromRegistrations(
                serviceManager.getXLinkRegistration(hostId));
    } 
    
    public ModelDescription getModelClassOfView(String hostId, String cId, String viewId) {
        String connectorId = cId;
        if (getRegistration(hostId, connectorId) != null) {
            XLinkTemplate template = getRegistration(hostId, connectorId).getxLinkTemplate();
            return template.getViewToModels().get(viewId);            
        }
        return null;
    }    
    
    private XLinkToolRegistration getRegistration(String hostId, String connectorId) {
        for (XLinkToolRegistration registration : serviceManager.getXLinkRegistration(hostId)) {
            if (registration.getConnectorId().equals(connectorId)) {
                return registration;
            }
        }
        return null;
    }
 
    public List<String> getModelIdentifierToModelId(
            String modelId, 
            String versionId) throws ClassNotFoundException {
        //Todo fetch real identifiers
        Class clazz = XLinkUtils.getClassOfOpenEngSBModel(modelId, versionId, osgiService);
        Object model = XLinkUtils.createEmptyInstanceOfModelClass(clazz);
        List<OpenEngSBModelEntry> entries = ModelUtils.getOpenEngSBModelEntries(model);
        List<String> identifierKeyNames = new ArrayList<String>();
        
        //########### MOCK !!!
        
        if(modelId.equals(XLinkMock.ooModel)){
            identifierKeyNames.add("className");
            identifierKeyNames.add("attributes");
            return identifierKeyNames;
        }        
        
        //########### MOCK !!!
        
        for (OpenEngSBModelEntry entry : entries) {
            identifierKeyNames.add(entry.getKey());
        }
        return identifierKeyNames;
    }    
    
    public boolean isConnectorRegistrated(String hostId, String connectorId){
        for (XLinkToolRegistration registration : serviceManager.getXLinkRegistration(hostId)) {
            if (registration.getConnectorId().equals(connectorId)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isViewExisting(String hostId, String connectorId, String viewId){
        for (XLinkToolRegistration registration : serviceManager.getXLinkRegistration(hostId)) {
            if (registration.getConnectorId().equals(connectorId)) {
                return registration.getxLinkTemplate().getViewToModels().containsKey(viewId);
            }
        }
        return false;
    }    
    
}
