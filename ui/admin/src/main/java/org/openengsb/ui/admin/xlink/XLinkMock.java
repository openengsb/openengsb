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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openengsb.core.api.model.ConnectorId;
import org.openengsb.core.api.xlink.XLinkRegisteredTool;
import org.openengsb.core.api.xlink.XLinkToolView;

/**
 */
public final class XLinkMock {
    
    private XLinkMock(){
        
    }
    
    private static ConnectorId getConnectorIdClassForStringId(String id){
        return null;
    }
    
    public static String getDestinationModelClassName(String cId, String viewId){
        ConnectorId connectorId = getConnectorIdClassForStringId(cId);
        List<XLinkRegisteredTool> tools = getToolsFromConnectorId(connectorId);
        return null;
    }
    
    public static List<String> getModelIdentifierToModelId(String modelId, String versionId){
        List<String> identifierKeyNames = Arrays.asList("methodName", "className", "packageName");
        return identifierKeyNames;
    }
    
    public static void callMatcher(String sourceModelClassName, Map<String,String> sourceModelIdentifierMap, String destinationModelClassName, ConnectorId connectorToCall, String viewToCall){
        
    }
    
    public static List<XLinkRegisteredTool> getRegisteredToolsFromUser(String hostId){
        List<ConnectorId> connectors = getConnectorIdsFromHost(hostId);
        return getToolsFromConnectors(connectors);
    }
    
    private static List<XLinkRegisteredTool> createMockToolList(){
        List<XLinkRegisteredTool> tools = new ArrayList<XLinkRegisteredTool>();
        XLinkRegisteredTool dummyTool1 = new XLinkRegisteredTool();
        dummyTool1.setId(new ConnectorId());
        
        String viewId_1 = "exampleViewId_1";
        String viewId_2 = "exampleViewId_2";
        
        HashMap<String, String> descriptions  = new HashMap<String, String>();
        descriptions.put("en","This is a demo view.");
        descriptions.put("de","Das ist eine demonstration view.");
        
        List<XLinkToolView> views = new ArrayList<XLinkToolView>();
        views.add(new XLinkToolView(viewId_1, "View 1", descriptions));
        views.add(new XLinkToolView(viewId_2, "View 2", descriptions));
        
        HashMap<String, List<XLinkToolView>> modelsToViews = new HashMap<String, List<XLinkToolView>>(); 
        modelsToViews.put(ExampleObjectOrientedDomain.class.getName(), views);
        
        dummyTool1.setAvailableViews(views);
        dummyTool1.setToolName("Tool A");     
        tools.add(dummyTool1);
        XLinkRegisteredTool dummyTool2 = new XLinkRegisteredTool();
        dummyTool2.setId(new ConnectorId());
        dummyTool2.setAvailableViews(views);
        dummyTool2.setToolName("Tool B");     
        tools.add(dummyTool2);
        return tools;
    }
    
    private static List<ConnectorId> getConnectorIdsFromHost(String hostId){
        return null;
    }
    
    private static List<XLinkRegisteredTool> getToolsFromConnectors(List<ConnectorId> connectors){
        List<XLinkRegisteredTool> tools = new ArrayList();
        for(ConnectorId connectorId : connectors){
            tools.addAll(getToolsFromConnectorId(connectorId));
        }
        //return tools;
        return createMockToolList();
    }
    
    private static List<XLinkRegisteredTool> getToolsFromConnectorId(ConnectorId connectorId){
        return createMockToolList();
    }

    
}
