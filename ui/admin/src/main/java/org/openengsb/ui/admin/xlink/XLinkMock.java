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
import org.openengsb.core.api.xlink.XLinkRegisteredTools;

/**
 */
public final class XLinkMock {
    
    private XLinkMock(){
        
    }
    
    public static ConnectorId getConnectorIdInstance(String connectorId){
        return null;
    }
    
    public static String getModelClassNameFromModelId(String modelId, String versionId){
        return null;
    }
    
    public static String getModelClassNameFromConnectorId(ConnectorId connectorId){
        return null;
    }
    
    public static List<String> getModelIdentifierToModelId(String modelId, String versionId){
        List<String> identifierKeyNames = Arrays.asList("methodName", "className", "packageName");
        return identifierKeyNames;
    }
    
    public static void callMatcher(String sourceModelClassName, Map<String,String> sourceModelIdentifierMap, String destinationModelClassName, ConnectorId connectorToCall, String viewToCall){
        
    }
    
    public static List<XLinkRegisteredTools> getRegisteredToolsFromUser(String hostId){
        //List<ConnectorId> connectors = getConnectorIdsFromHost(hostId);
        //return getToolsFromConnectors(connectors);
        return createMockToolList();
    }
    
    private static List<XLinkRegisteredTools> createMockToolList(){
        List<XLinkRegisteredTools> tools = new ArrayList<XLinkRegisteredTools>();
        XLinkRegisteredTools dummyTool1 = new XLinkRegisteredTools();
        dummyTool1.setId(new ConnectorId());
        HashMap<String, String> views = new HashMap<String, String>();
        views.put("view1","View1");
        views.put("view1","View2");
        views.put("view1","View3");
        dummyTool1.setAvailableViews(views);
        dummyTool1.setToolName("Tool A");     
        tools.add(dummyTool1);
        XLinkRegisteredTools dummyTool2 = new XLinkRegisteredTools();
        dummyTool2.setId(new ConnectorId());
        dummyTool2.setAvailableViews(views);
        dummyTool2.setToolName("Tool B");     
        tools.add(dummyTool2);
        return tools;
    }
    
    private static List<ConnectorId> getConnectorIdsFromHost(String hostId){
        return null;
    }
    
    private static List<XLinkRegisteredTools> getToolsFromConnectors(List<ConnectorId> connectors){
        List<XLinkRegisteredTools> tools = new ArrayList();
        for(ConnectorId connectorId : connectors){
            XLinkRegisteredTools newTool = new XLinkRegisteredTools();
            newTool.setId(connectorId);
            newTool.setAvailableViews(getAvailableViewsForConnectorId(connectorId));
            newTool.setToolName(getToolNameForConnectorId(connectorId));
            tools.add(newTool);
        }
        return tools;
    }
    
    private static HashMap<String, String> getAvailableViewsForConnectorId(ConnectorId connectorId){
        return null;
    }
    
    private static String getToolNameForConnectorId(ConnectorId connectorId){
        return null;
    }
    
}
