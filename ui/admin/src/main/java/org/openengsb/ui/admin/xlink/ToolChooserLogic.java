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
import java.util.Map;
import org.openengsb.core.api.ConnectorManager;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.model.OpenEngSBModelEntry;
import org.openengsb.core.api.xlink.model.XLinkLocalTool;
import org.openengsb.core.api.xlink.model.XLinkModelInformation;
import org.openengsb.core.api.xlink.model.XLinkTemplate;
import org.openengsb.core.api.xlink.model.XLinkToolRegistration;
import org.openengsb.core.api.xlink.model.XLinkToolView;

public class ToolChooserLogic {

    private static OpenEngSBModel createInstanceOfModelClass(String modelId, String versionId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    private ConnectorManager serviceManager;

    public ToolChooserLogic(ConnectorManager serviceManager) {
        this.serviceManager = serviceManager;
    }
    
    public List<XLinkLocalTool> getRegisteredToolsFromHost(String hostId) {
        return getLocalToolFromRegistrations(serviceManager.getXLinkRegistration(hostId));
    } 
    
    public XLinkModelInformation getModelClassOfView(String hostId, String cId, String viewId) {
        ConnectorId connectorId = ConnectorId.fromFullId(cId);
        XLinkTemplate template = getRegistration(hostId, connectorId).getxLinkTemplate();
        return template.getViewToModels().get(viewId);
    }    
    
    private XLinkToolRegistration getRegistration(String hostId, ConnectorId connectorId) {
        for(XLinkToolRegistration registration : serviceManager.getXLinkRegistration(hostId)) {
            if (registration.getConnectorId().equals(connectorId)) {
                return registration;
            }
        }
        return null;
    }
    
    //todo testing
    public static List<String> getModelIdentifierToModelId(
            String modelId, 
            String versionId) throws ClassNotFoundException {
        //Todo fetch real identifiers
        OpenEngSBModel model = createInstanceOfModelClass(modelId, versionId);
        List<OpenEngSBModelEntry> entries = model.getOpenEngSBModelEntries();
        List<String> identifierKeyNames = new ArrayList<String>();
        for (OpenEngSBModelEntry entry : entries) {
            identifierKeyNames.add(entry.getKey());
        }
        return identifierKeyNames;
    }    
    
    private List<XLinkLocalTool> getLocalToolFromRegistrations(List<XLinkToolRegistration> registrations) {
        List<XLinkLocalTool> tools = new ArrayList<XLinkLocalTool>();
        for (XLinkToolRegistration registration : registrations) {
            XLinkLocalTool newLocalTools 
                = new XLinkLocalTool(
                        registration.getConnectorId(), 
                        registration.getToolName(), 
                        getViewsOfRegistration(registration));
            tools.add(newLocalTools);
        }
        return tools;
    }    

    private List<XLinkToolView> getViewsOfRegistration(XLinkToolRegistration registration) {
        List<XLinkToolView> viewsOfRegistration = new ArrayList<XLinkToolView>();
        Map<XLinkModelInformation, List<XLinkToolView>> modelsToViews = registration.getModelsToViews();
        for (List<XLinkToolView> views : modelsToViews.values()) {
            for (XLinkToolView view : views) {
                if (!viewsOfRegistration.contains(view)) {
                    viewsOfRegistration.add(view);
                }
            }
        }
        return viewsOfRegistration;
    }    
    
}
