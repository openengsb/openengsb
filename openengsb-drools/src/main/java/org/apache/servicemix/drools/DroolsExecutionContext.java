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

import java.io.IOException;
import java.util.Collection;

import javax.jbi.messaging.MessageExchange;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.drools.definition.rule.Rule;
import org.drools.event.ActivationCreatedEvent;
import org.drools.event.DefaultAgendaEventListener;
import org.openengsb.drools.MessageHelperImpl;
import org.xml.sax.SAXException;

/**
 * Represents the execution context of the Drools rules for a single
 * {@link MessageExchange}
 */
public class DroolsExecutionContext extends DefaultAgendaEventListener {

	private final StatefulSession memory;
//	private final JbiHelper helper;
	private int rulesFired;
	//private MessageExchange exchange;

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
	public DroolsExecutionContext(DroolsEndpoint endpoint,
			Collection<Object> objects) {
		this.memory = endpoint.getRuleBase().newStatefulSession();
		this.memory.addEventListener(this);
//		this.helper = new JbiHelper(endpoint, memory);

		populateWorkingMemory(objects);
	}

	private void populateWorkingMemory(Collection<Object> objects) {
		// memory.setGlobal(JBI_HELPER_KEY, helper);
		memory.setGlobal(HELPER_KEY, new MessageHelperImpl());
		if (objects != null) {
			System.out.println("inserting objects");
			for (Object o : objects) {
				System.out.println("insert");
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
		System.out.println("firing all rules");
		Rule[] rules = memory.getRuleBase().getPackages()[0].getRules();
		System.out.println(rules[0].getName());
		memory.fireAllRules();
		System.out.println("all rules fired - " + getRulesFired());
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
	public void activationCreated(ActivationCreatedEvent event,
			WorkingMemory workingMemory) {
		rulesFired++;
	}

}
