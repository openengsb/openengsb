package org.openengsb.drools;

import java.util.Properties;

import org.drools.RuleBase;
import org.drools.RuleBaseConfiguration;
import org.drools.WorkingMemory;
import org.drools.agent.RuleAgent;
import org.junit.Test;
import org.openengsb.drools.model.Event;

public class GuvnorInterface {
	/* drl */
	public static final String URL = "http://localhost:8080/drools-guvnor/org.drools.guvnor.Guvnor/package/org.openengsb/LATEST"; 
	
	@Test
	public void testInit() {
		Properties config = new Properties();
		config.put("url", URL);
		RuleAgent agent = RuleAgent.newRuleAgent(config);
		RuleBase ruleBase = agent.getRuleBase();

		WorkingMemory workingMemory = ruleBase.newStatefulSession();
		workingMemory.setGlobal("helper", new MessageHelper() {
			
			@Override
			public boolean triggerAction(String name, String arg2) {
				System.out.println("triggered action: " + name);
				System.out.println("arg was " + arg2);
				return true;
			}
		});
		Event e = new Event("hello");
		workingMemory.insert(e);

		workingMemory.fireAllRules();
		
	}

}
