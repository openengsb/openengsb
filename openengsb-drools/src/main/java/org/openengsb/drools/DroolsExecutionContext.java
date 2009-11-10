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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.drools.event.ActivationCreatedEvent;
import org.drools.event.DefaultAgendaEventListener;
import org.openengsb.drools.model.MessageHelperImpl;

/**
 * Represents the execution context of the Drools rules.
 */
public class DroolsExecutionContext extends DefaultAgendaEventListener {

    /**
     * the logger.
     */
    private static Log log = LogFactory.getLog(DroolsExecutionContext.class);

    /**
     * Memory of the session handling the event.
     */
    private final StatefulSession memory;

    /**
     * name of the helper-variable used in drl-rules.
     */
    public static final String HELPER_KEY = "helper";

    /**
     * Start a new execution context for the specified exchange.
     * 
     * This will create and fill {@link WorkingMemory} and register listeners on
     * it to keep track of things.
     * 
     * @param endpoint endpoint the context belongs to
     * @param objects objects to insert into the working memory
     */
    public DroolsExecutionContext(DroolsEndpoint endpoint, Collection<Object> objects) {
        this.memory = endpoint.getRuleBase().newStatefulSession();
        this.memory.addEventListener(this);

        populateWorkingMemory(objects);
    }

    /**
     * inserts objects into the kb.
     * 
     * @param objects the objects to insert.
     */
    private void populateWorkingMemory(Collection<Object> objects) {
        memory.setGlobal(HELPER_KEY, new MessageHelperImpl());
        if (objects != null) {
            for (Object o : objects) {
                memory.insert(o);
            }
        }
    }

    /**
     * Start the execution context. This will fire all rules in the rule base.
     */
    public void start() {
        memory.fireAllRules();
    }

    /**
     * Stop the context, disposing of all event listeners and working memory
     * contents.
     */
    public void stop() {
        memory.removeEventListener(this);
        memory.dispose();
    }

    @Override
    public void activationCreated(ActivationCreatedEvent event, WorkingMemory workingMemory) {
        /*
         * TODO add auditing features, for tracking fired rules.
         * event.getActivation().getRule();
         */
        log.debug("Event fired rule: " + event.getActivation().getRule().getName());
    }

}
