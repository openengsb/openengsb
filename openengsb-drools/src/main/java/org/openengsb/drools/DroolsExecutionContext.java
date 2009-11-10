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

import java.util.Collection;

import javax.jbi.messaging.MessageExchange;

import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.drools.event.ActivationCreatedEvent;
import org.drools.event.DefaultAgendaEventListener;
import org.openengsb.drools.model.MessageHelperImpl;

/**
 * Represents the execution context of the Drools rules for a single
 * {@link MessageExchange}
 */
public class DroolsExecutionContext extends DefaultAgendaEventListener {

    private final StatefulSession memory;
    // private final JbiHelper helper;
    private int rulesFired;
    // private MessageExchange exchange;

    public static final String JBI_HELPER_KEY = "jbi";
    public static final String HELPER_KEY = "helper";

    /**
     * Start a new execution context for the specified exchange.
     * 
     * This will create and fill {@link WorkingMemory} and register listeners on
     * it to keep track of things.
     * 
     * @param endpoint
     */
    public DroolsExecutionContext(DroolsEndpoint endpoint, Collection<Object> objects) {
        this.memory = endpoint.getRuleBase().newStatefulSession();
        this.memory.addEventListener(this);
        // this.helper = new JbiHelper(endpoint, memory);

        populateWorkingMemory(objects);
    }

    private void populateWorkingMemory(Collection<Object> objects) {
        // memory.setGlobal(JBI_HELPER_KEY, helper);
        memory.setGlobal(HELPER_KEY, new MessageHelperImpl());
        if (objects != null) {
            for (Object o : objects) {
                memory.insert(o);
            }
        }
        // if (endpoint.getGlobals() != null) {
        // for (Map.Entry<String, Object> e : endpoint.getGlobals().entrySet())
        // {
        // memory.setGlobal(e.getKey(), e.getValue());
        // }
        // }
    }

    /**
     * Start the execution context. This will fire all rules in the rule base.
     */
    public void start() {
        memory.fireAllRules();
    }

    /**
     * Stop the context, disposing of all event listeners and working memory
     * contents
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

    // event handler callbacks
    @Override
    public void activationCreated(ActivationCreatedEvent event, WorkingMemory workingMemory) {
        rulesFired++;
    }

}
