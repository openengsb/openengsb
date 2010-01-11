/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE\-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.drools;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.ServiceUnit;
import org.drools.RuleBase;
import org.drools.agent.RuleAgent;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.endpoints.EventEndpoint;
import org.openengsb.core.model.Event;
import org.springframework.core.io.Resource;

/**
 * @org.apache.xbean.XBean element="droolsEndpoint"
 *                         description="Drools Component"
 */
public class DroolsEndpoint extends EventEndpoint {

    /**
     * Pointer to the rulebase.
     */
    private RuleBase ruleBase;
    /**
     * Some Resource to read the rulebase from.
     */
    private Resource ruleBaseResource;
    /**
     * URL to remote guvnor-rulebase.
     */
    private URL ruleBaseURL;
    /**
     * List of global variables for rules to use.
     */
    private Map<String, Object> globals = new HashMap<String, Object>();

    /**
     * default constructor.
     */
    public DroolsEndpoint() {
    }

    public DroolsEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
        super(component, endpoint);
    }

    public DroolsEndpoint(ServiceUnit su, QName service, String endpoint) {
        super(su, service, endpoint);
    }

    @Override
    protected void handleEvent(MessageExchange exchange, NormalizedMessage in, ContextHelper contextHelper)
            throws MessagingException {
        if (ruleBase == null) {
            init();
        }
        drools(exchange);
    }

    private void init() {
        Properties config = new Properties();
        config.put("url", "http://localhost:8081/drools-guvnor/org.drools.guvnor.Guvnor/package/org.openengsb/LATEST");
        RuleAgent agent = RuleAgent.newRuleAgent(config);
        RuleBase ruleBase = agent.getRuleBase();
        setRuleBase(ruleBase);
    }

    /**
     * @return the ruleBase
     */
    public RuleBase getRuleBase() {
        return this.ruleBase;
    }

    /**
     * @param ruleBase the ruleBase to set
     */
    public void setRuleBase(RuleBase ruleBase) {
        this.ruleBase = ruleBase;
    }

    /**
     * @return the ruleBaseResource
     */
    public Resource getRuleBaseResource() {
        return this.ruleBaseResource;
    }

    /**
     * @param ruleBaseResource the ruleBaseResource to set
     */
    public void setRuleBaseResource(Resource ruleBaseResource) {
        this.ruleBaseResource = ruleBaseResource;
    }

    /**
     * @return the ruleBaseURL
     */
    public URL getRuleBaseURL() {
        return this.ruleBaseURL;
    }

    /**
     * @param ruleBaseURL the ruleBaseURL to set
     */
    public void setRuleBaseURL(URL ruleBaseURL) {
        this.ruleBaseURL = ruleBaseURL;
    }

    /**
     * @return the variables
     */
    public Map<String, Object> getGlobals() {
        return this.globals;
    }

    /**
     * @param variables the variables to set
     */
    public void setGlobals(Map<String, Object> variables) {
        this.globals = variables;
    }

    /**
     * handle the MessageExchange with drools.
     * 
     * @param exchange exchange to handle
     */
    protected void drools(MessageExchange exchange) {
        NormalizedMessage inMessage = exchange.getMessage("in");
        String contextId = (String) inMessage.getProperty("contextId");

        Event e = XmlHelper.parseEvent(inMessage);
        Collection<Object> objects = Arrays.asList(new Object[] { e });
        DroolsExecutionContext drools = new DroolsExecutionContext(this, objects, contextId);
        drools.start();
    }

}
