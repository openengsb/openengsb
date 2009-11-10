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

import java.util.Properties;

import org.drools.RuleBase;
import org.drools.WorkingMemory;
import org.drools.agent.RuleAgent;
import org.junit.Test;
import org.openengsb.drools.model.Event;
import org.openengsb.drools.model.MessageHelperImpl;

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
        workingMemory.setGlobal("helper", new MessageHelperImpl());
        Event e = new Event("hello");
        workingMemory.insert(e);

        workingMemory.fireAllRules();

    }

}
