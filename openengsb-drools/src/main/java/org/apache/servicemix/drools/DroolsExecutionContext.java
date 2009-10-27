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
package org.apache.servicemix.drools;

import java.util.Map;

import javax.jbi.messaging.MessageExchange;

import org.apache.servicemix.drools.model.JbiHelper;
import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.drools.event.ActivationCreatedEvent;
import org.drools.event.DefaultAgendaEventListener;

/**
 * Represents the execution context of the Drools rules for a single {@link MessageExchange}
 */
public class DroolsExecutionContext extends DefaultAgendaEventListener {
    
    private final StatefulSession memory;
    private final JbiHelper helper;
    private int rulesFired;
    private MessageExchange exchange;
    
    public static final String JBI_HELPER_KEY = "jbi";
    
    /**
     * Start a new execution context for the specified exchange.
     * 
     * This will create and fill {@link WorkingMemory} and register listeners on it to keep track of things.
     * 
     * @param endpoint
     * @param exchange
     */
    public DroolsExecutionContext(DroolsEndpoint endpoint, MessageExchange exchange) {
        super();
        this.memory = endpoint.getRuleBase().newStatefulSession();
        this.memory.addEventListener(this);
        this.exchange = exchange;
        this.helper = new JbiHelper(endpoint, exchange, memory);
        populateWorkingMemory(endpoint);
    }

    private void populateWorkingMemory(DroolsEndpoint endpoint) {
        memory.setGlobal(JBI_HELPER_KEY, helper);
        if (endpoint.getAssertedObjects() != null) {
            for (Object o : endpoint.getAssertedObjects()) {
                memory.insert(o);
            }
        }
        if (endpoint.getGlobals() != null) {
            for (Map.Entry<String, Object> e : endpoint.getGlobals().entrySet()) {
                memory.setGlobal(e.getKey(), e.getValue());
            }
        }
    }
    
    /**
     * Start the execution context.
     * This will fire all rules in the rule base.
     */
    public void start() {
        memory.fireAllRules();
    }
    
    /**
     * Update the working memory, potentially triggering additional rules
     */
    public void update() {
        helper.update();
    }
    
    /**
     * Stop the context, disposing of all event listeners and working memory contents
     */
    public void stop() {
        memory.removeEventListener(this);
        memory.dispose();
    }
    
    /**
     * Get the number of rules that were fired
     */
    public int getRulesFired() {
        return rulesFired;
    }

    /**
     * Returns <code>true</code> if the {@link MessageExchange} was handled by the rules themselves 
     * (e.g. by answering or faulting the exchange}
     */
    public boolean isExchangeHandled() {
        return helper.isExchangeHandled();
    }
    
    /**
     * Return the {@link MessageExchange} we are evaluating rules on
     */
    public MessageExchange getExchange() {
        return exchange;
    }
    
    // event handler callbacks
    @Override
    public void activationCreated(ActivationCreatedEvent event, WorkingMemory workingMemory) {
        rulesFired++;
    }
    
    /**
     * Access the JbiHelper object that is being exposed to the .drl file
     */
    public JbiHelper getHelper() {
        return helper;
    }
}
