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

import org.apache.log4j.Logger;
import org.apache.servicemix.common.DefaultComponent;

public class OpenEngSBComponent<T> extends DefaultComponent {
    private Logger log = Logger.getLogger(getClass());

    private HashMap<String, String> contextProperties = new HashMap<String, String>();
    private List<T> endpoints = new LinkedList<T>();

    @SuppressWarnings("unchecked")
    public T[] getEndpoints() {
        return (T[]) endpoints.toArray();
    }

    public void setEndpoints(T[] endpoints) {
        this.endpoints = new LinkedList<T>();
        for (T t : endpoints) {
            this.endpoints.add(t);
        }
    }

    public void addEndpoint(T endpoint) {
        endpoints.add(endpoint);
    }
    
    public void removeEndpoint(T endpoint) {
        endpoints.remove(endpoint);
    }

    @Override
    protected List<?> getConfiguredEndpoints() {
        return endpoints;
    }

    public HashMap<String, String> getContextProperties() {
        return contextProperties;
    }

    public void setContextProperties(HashMap<String, String> contextProperties) {
        this.contextProperties = contextProperties;
    }

    public boolean hasNoEndpoints() {
        return endpoints.size() == 0;
    }

    @Override
    protected void doInit() throws Exception {
        log.info("Loading SE");
        // TODO: read Properties from file and store them in contextProperties

        super.doInit();
    }

    @Override
    protected void doShutDown() throws Exception {
        log.info("Unloading SE");
        contextProperties = null;

        super.doShutDown();
    }
}
