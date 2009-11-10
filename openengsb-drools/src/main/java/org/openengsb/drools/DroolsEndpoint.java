/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jbi.JBIException;
import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.MessageExchange.Role;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.JbiConstants;
import org.apache.servicemix.common.ServiceUnit;
import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.drools.RuleBase;
import org.drools.compiler.RuleBaseLoader;
import org.openengsb.drools.model.Event;
import org.springframework.core.io.Resource;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * 
 * @org.apache.xbean.XBean element="endpoint"
 */

public class DroolsEndpoint extends ProviderEndpoint {

    private RuleBase ruleBase;
    private Resource ruleBaseResource;
    private URL ruleBaseURL;
    private Map<String, Object> globals;

    @SuppressWarnings("serial")
    private ConcurrentMap<String, DroolsExecutionContext> pending = new ConcurrentHashMap<String, DroolsExecutionContext>() {
        public DroolsExecutionContext remove(Object key) {
            DroolsExecutionContext context = super.remove(key);
            if (context != null) {
                // stop the execution context -- updating and disposing of any
                // working memory
                // context.update();
                context.stop();
            }
            return context;
        };
    };

    public DroolsEndpoint() {
        super();
    }

    public DroolsEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
        super(component, endpoint);
    }

    public DroolsEndpoint(ServiceUnit su, QName service, String endpoint) {
        super(su, service, endpoint);
    }

    /**
     * @return the ruleBase
     */
    public RuleBase getRuleBase() {
        return ruleBase;
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
        return ruleBaseResource;
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
        return ruleBaseURL;
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
        return globals;
    }

    /**
     * @param variables the variables to set
     */
    public void setGlobals(Map<String, Object> variables) {
        this.globals = variables;
    }

    public void validate() throws DeploymentException {
        super.validate();
        if (ruleBase == null && ruleBaseResource == null && ruleBaseURL == null) {
            throw new DeploymentException("Property ruleBase, ruleBaseResource or ruleBaseURL must be set");
        }
    }

    public void start() throws Exception {
        super.start();
        if (ruleBase == null) {
            InputStream is = null;
            try {
                if (ruleBaseResource != null) {
                    is = ruleBaseResource.getInputStream();
                } else if (ruleBaseURL != null) {
                    is = ruleBaseURL.openStream();
                } else {
                    throw new IllegalArgumentException("Property ruleBase, ruleBaseResource "
                            + "or ruleBaseURL must be set");
                }
                RuleBaseLoader loader = RuleBaseLoader.getInstance();
                ruleBase = loader.loadFromReader(new InputStreamReader(is));
            } catch (Exception e) {
                throw new JBIException(e);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.servicemix.common.endpoints.ProviderEndpoint#process(
     * javax.jbi.messaging.MessageExchange,
     * javax.jbi.messaging.NormalizedMessage)
     */
    public void process(MessageExchange exchange) throws Exception {
        if (exchange.getRole() == Role.PROVIDER) {
            handleProviderExchange(exchange);
        }
    }

    private void handleProviderExchange(MessageExchange exchange) throws Exception {
        if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
            drools(exchange);
        } else {
            // must be a DONE/ERROR so removing any pending contexts
            pending.remove(exchange.getExchangeId());
        }
    }

    public static String getCorrelationId(MessageExchange exchange) {
        Object correlation = exchange.getProperty(JbiConstants.CORRELATION_ID);
        if (correlation == null) {
            return exchange.getExchangeId();
        } else {
            return correlation.toString();
        }
    }

    protected void drools(MessageExchange exchange) throws Exception {
        Event e = XmlHelper.parseEvent(exchange.getMessage("in"));
        @SuppressWarnings("unchecked")
        Collection<Object> objects = Arrays.asList(new Object[] { e });
        startDroolsExecutionContext(objects);
    }

    private DroolsExecutionContext startDroolsExecutionContext(Collection<Object> objects) {
        DroolsExecutionContext drools = new DroolsExecutionContext(this, objects);
        // pending.put(exchange.getExchangeId(), drools);
        drools.start();
        return drools;
    }

    @Override
    protected void send(MessageExchange me) throws MessagingException {
        if (me.getStatus() != ExchangeStatus.ACTIVE) {
            // must be a DONE/ERROR so removing any pending contexts
            pending.remove(me.getExchangeId());
        }
        super.send(me);
    }
}
