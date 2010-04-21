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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.common.DefaultComponent;
import org.openengsb.core.endpoints.OpenEngSBEndpoint;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

public class OpenEngSBComponent extends DefaultComponent {
    private Log log = LogFactory.getLog(getClass());

    private HashMap<String, HashMap<String, String>> contextProperties;
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

    public HashMap<String, HashMap<String, String>> getContextProperties() {
        return contextProperties;
    }

    public boolean hasNoEndpoints() {
        return endpoints.size() == 0;
    }

    public boolean hasContextProperties() {
        return contextProperties != null && contextProperties.size() != 0;
    }

    public void setContextProperties(HashMap<String, HashMap<String, String>> contextProperties) {
        this.contextProperties = contextProperties;
    }

    @Override
    protected void doInit() throws Exception {
        loadConfiguration();
        super.doInit();
    }

    @SuppressWarnings("unchecked")
    private void loadConfiguration() {
        try {
            ClassPathResource res = new ClassPathResource("contextProperties.xml");
            XmlBeanFactory factory = new XmlBeanFactory(res);
            contextProperties = (HashMap<String, HashMap<String, String>>) factory.getBean("contextProperties");
        } catch (BeansException e) {
            log.info("No configuration file found (or it is corrupted)");
        }
    }
}
