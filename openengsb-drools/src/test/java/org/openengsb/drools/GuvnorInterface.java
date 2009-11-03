package org.openengsb.drools;

import java.util.Iterator;
import java.util.Properties;

import org.drools.RuleBase;
import org.drools.WorkingMemory;
import org.drools.agent.RuleAgent;
import org.openengsb.drools.model.Event;

public class GuvnorInterface {

	public static final String URL = "http://localhost:8080/drools-guvnor/org.drools.guvnor.Guvnor/package/testPackage/LATEST"; 
	
	public static final void main(String[] args) {
		Properties config = new Properties();
		config.put("url", URL);
		RuleAgent agent = RuleAgent.newRuleAgent(config);
		RuleBase ruleBase = agent.getRuleBase();

		WorkingMemory workingMemory = ruleBase.newStatefulSession();
		Event e = new Event("greet");
		workingMemory.insert(e);

		workingMemory.fireAllRules();

		for (Iterator<?> i = workingMemory.iterateObjects(); i.hasNext();) {
			System.out.println(i.next().getClass().getCanonicalName());
		}
	}

}
