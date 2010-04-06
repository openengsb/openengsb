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
package org.openengsb.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.servicemix.common.DefaultComponent;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;

public class OpenEngSBComponent extends DefaultComponent {
    protected HashMap<String, String> contextProperties = new HashMap<String, String>();
    private List<OpenEngSBEndpoint> endpoints = new LinkedList<OpenEngSBEndpoint>();

    public OpenEngSBEndpoint[] getEndpoints() {
        return (OpenEngSBEndpoint[]) endpoints.toArray();
    }

    public void setEndpoints(OpenEngSBEndpoint[] endpoints) {
        this.endpoints = new LinkedList<OpenEngSBEndpoint>();
        for (OpenEngSBEndpoint t : endpoints) {
            this.endpoints.add(t);
        }
    }

    public void addCustomEndpoint(OpenEngSBEndpoint endpoint) {
        endpoints.add(endpoint);
    }
    
    public void removeCustomEndpoint(OpenEngSBEndpoint endpoint) {
        endpoints.remove(endpoint);
    }

    @Override
    protected List<?> getConfiguredEndpoints() {
        return endpoints;
    }

    public HashMap<String, String> getContextProperties() {
        return contextProperties;
    }

    public boolean hasNoEndpoints() {
        return endpoints.size() == 0;
    }
    
    public boolean hasContextProperties() {
        return contextProperties.size() != 0;
    }
}
