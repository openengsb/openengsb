/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.config.jbi;

import java.util.HashMap;
import java.util.Map;

import org.openengsb.config.jbi.types.ComponentType;
import org.openengsb.config.jbi.types.EndpointType;

public class ServiceUnitInfo {
    private final ComponentType component;
    private final EndpointType endpoint;
    private final Map<String, String> map;

    public ServiceUnitInfo(ComponentType component, EndpointType endpoint) {
        this.component = component;
        this.endpoint = endpoint;
        this.map = new HashMap<String, String>();
    }

    public ServiceUnitInfo(ComponentType component, EndpointType endpoint, Map<String, String> map) {
        this.component = component;
        this.endpoint = endpoint;
        this.map = map;
    }

    public ComponentType getComponent() {
        return component;
    }

    public EndpointType getEndpoint() {
        return endpoint;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public String getIdentifier() {
        return component.getName() + "-" + endpoint.getName() + "-" + map.get("service") + "-" + map.get("endpoint");
    }
}
