/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.drools.model;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.DeliveryChannel;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.common.EndpointSupport;
import org.apache.servicemix.common.JbiConstants;
import org.apache.servicemix.common.util.MessageUtil;
import org.apache.servicemix.common.util.URIResolver;
import org.apache.servicemix.drools.DroolsComponent;
import org.apache.servicemix.drools.DroolsEndpoint;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.drools.FactHandle;
import org.drools.WorkingMemory;

/**
 * A helper class for use inside a rule to forward a message to an endpoint
 * 
 * @version $Revision: 426415 $
 */
public class JbiHelper {

    private DroolsEndpoint endpoint;
    private Exchange exchange;
    private WorkingMemory memory;
    private FactHandle exchangeFactHandle;
    private boolean exchangeHandled = false;

    public JbiHelper(DroolsEndpoint endpoint, MessageExchange exchange, WorkingMemory memory) {
        this.endpoint = endpoint;
        this.exchange = new Exchange(exchange, endpoint.getNamespaceContext());
        this.memory = memory;
        this.exchangeFactHandle = this.memory.insert(this.exchange);
    }

    public DroolsEndpoint getEndpoint() {
        return endpoint;
    }

    public ComponentContext getContext() {
        return endpoint.getContext();
    }

    public DeliveryChannel getChannel() throws MessagingException {
        return getContext().getDeliveryChannel();
    }

    public Exchange getExchange() {
        return exchange;
    }

    public Log getLogger() {
        return LogFactory.getLog(memory.getRuleBase().getPackages()[0].getName());
    }

    /**
     * Forwards the inbound message to the given target
     * 
     * @param uri
     */
    public void route(String uri) throws MessagingException {
        Source src = null;
        routeTo(src, uri);
    }

    /**
     * @see #routeTo(Source, String)
     */
    public void routeTo(String content, String uri) throws MessagingException {
        if (content == null) {
            routeTo(this.exchange.getInternalExchange().getMessage("in").getContent(), uri);
        } else {
            routeTo(new StringSource(content), uri);
        }
    }

    /**
     * Send a message to the uri
     *  
     * @param content the message content
     * @param uri the target endpoint's uri
     * @throws MessagingException
     */
    public void routeTo(Source content, String uri) throws MessagingException {
        MessageExchange me = this.exchange.getInternalExchange();

        NormalizedMessage in = null;
        if (content == null) {
            in = me.getMessage("in");
        } else {
            in = me.createMessage();
            in.setContent(content);
        }
        MessageExchange newMe = getChannel().createExchangeFactory().createExchange(me.getPattern());
        URIResolver.configureExchange(newMe, getContext(), uri);
        MessageUtil.transferToIn(in, newMe);
        // Set the sender endpoint property
        String key = EndpointSupport.getKey(endpoint);
        newMe.setProperty(JbiConstants.SENDER_ENDPOINT, key);
        newMe.setProperty(JbiConstants.CORRELATION_ID, DroolsEndpoint.getCorrelationId(this.exchange.getInternalExchange()));
        newMe.setProperty(DroolsComponent.DROOLS_CORRELATION_ID, me.getExchangeId());
        getChannel().send(newMe);
    }

    /**
     * @see #routeToDefault(Source)
     */
    public void routeToDefault(String content) throws MessagingException {
        routeTo(content, endpoint.getDefaultRouteURI());
    }

    /**
     * Send this content to the default routing URI ({@link DroolsEndpoint#getDefaultRouteURI()} specified on the endpoint
     * 
     * @param content the message body
     * @throws MessagingException
     */
    public void routeToDefault(Source content) throws MessagingException {
        routeTo(content, endpoint.getDefaultRouteURI());
    }

    /**
     * @see #fault(Source)
     */
    public void fault(String content) throws Exception {
        MessageExchange me = this.exchange.getInternalExchange();
        if (me instanceof InOnly) {
            me.setError(new Exception(content));
            getChannel().send(me);
        } else {
            Fault fault = me.createFault();
            fault.setContent(new StringSource(content));
            me.setFault(fault);
            getChannel().send(me);
        }
        exchangeHandled = true;
    }

    /**
     * Send a JBI Error message (for InOnly) or JBI Fault message (for the other MEPs)
     * 
     * @param content the error content
     * @throws Exception
     */
    public void fault(Source content) throws Exception {
        MessageExchange me = this.exchange.getInternalExchange();
        if (me instanceof InOnly) {
            me.setError(new Exception(new SourceTransformer().toString(content)));
            getChannel().send(me);
        } else {
            Fault fault = me.createFault();
            fault.setContent(content);
            me.setFault(fault);
            getChannel().send(me);
        }
        exchangeHandled = true;
    }

    /**
     * @see #answer(Source)
     */
    public void answer(String content) throws Exception {
        answer(new StringSource(content));
    }

    /**
     * Answer the exchange with the given response content
     * 
     * @param content the response
     * @throws Exception
     */    
    public void answer(Source content) throws Exception {
        MessageExchange me = this.exchange.getInternalExchange();
        NormalizedMessage out = me.createMessage();
        out.setContent(content);
        me.setMessage(out, "out");
        getChannel().send(me);
        exchangeHandled = true;
        update();
    }

    /**
     * Update the {@link MessageExchange} information in the rule engine's {@link WorkingMemory}
     */
    public void update() {
        this.memory.update(this.exchangeFactHandle, this.exchange);
    }
    
    /**
     * Has the MessageExchange been handled by the drools endpoint?
     * 
     * @return
     */
    public boolean isExchangeHandled() {
        return exchangeHandled;
    }

}
