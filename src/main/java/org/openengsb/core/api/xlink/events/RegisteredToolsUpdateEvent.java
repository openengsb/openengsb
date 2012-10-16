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

package org.openengsb.core.api.xlink.events;


import org.openengsb.core.api.Event;
import org.openengsb.core.api.xlink.model.RemoteTool;

/**
 * Event, indicating that the list of tools registered for XLinking, at the host,
 * has changed.
 */
public class RegisteredToolsUpdateEvent extends Event {
    
    /**
     * List of all other currently registered tools from the same host. The list
     * has changed. This information is used to support local switching between tools.
     */
    RemoteTool[] registeredTools;

    public RegisteredToolsUpdateEvent() { 
        
    }
    
    /**
     * List of all other currently registered tools from the same host. The list
     * has changed. This information is used to support local switching between tools.
     */    
    public RemoteTool[] getRegisteredTools() {
        return registeredTools;
    }

    public void setRegisteredTools(RemoteTool[] registeredTools) {
        this.registeredTools = registeredTools;
    }
    
}
