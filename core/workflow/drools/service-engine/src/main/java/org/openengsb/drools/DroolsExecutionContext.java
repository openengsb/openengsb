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

import java.util.Collection;

import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.openengsb.core.MessageProperties;

/**
 * Represents the execution context of the Drools rules.
 */
public class DroolsExecutionContext {

    /**
     * Memory of the session handling the event.
     */
    private final StatefulSession memory;

    /**
     * Start a new execution context for the specified exchange.
     * 
     * This will create and fill {@link WorkingMemory} and register listeners on
     * it to keep track of things.
     * 
     * @param endpoint endpoint the context belongs to
     * @param objects objects to insert into the working memory
     * @param contextId
     */
    public DroolsExecutionContext(DroolsEndpoint endpoint, Collection<Object> objects, MessageProperties msgProperties) {
        DroolsSession session = new DroolsSession(msgProperties, endpoint);
        this.memory = session.createSession(objects);
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
        memory.dispose();
    }

}
