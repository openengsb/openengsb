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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.drools.event.AfterActivationFiredEvent;
import org.drools.event.DefaultAgendaEventListener;
import org.drools.rule.Package;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.model.Event;
import org.openengsb.drools.RuleBaseSource.RuleBaseElement;

public class DirectorySourceTest {

    private static class RuleListener2 extends DefaultAgendaEventListener {
        protected int numFired = 0;

        @Override
        public void afterActivationFired(AfterActivationFiredEvent event, WorkingMemory workingMemory) {
            numFired++;
            super.afterActivationFired(event, workingMemory);
        }
    }

    private RuleBaseSource source;
    private RuleBase rb;
    private StatefulSession session;
    private RuleListener2 listener;

    @Before
    public void setUp() throws Exception {
        source = new DirectoryRuleSource("./src/test/resources/rulebase");
        rb = source.getRulebase();
        session = rb.newStatefulSession();
        listener = new RuleListener2();
        session.addEventListener(listener);
    }

    @After
    public void tearDown() throws Exception {
        File newRuleFile = new File("src/test/resources/rulebase/test3.rule");
        if (newRuleFile.exists()) {
            newRuleFile.delete();
        }
        session.dispose();
    }

    @Test
    public void testGetRuleBase() throws Exception {
        Assert.assertNotNull(rb);
        Package p = rb.getPackage("org.openengsb");
        assertNotNull(p);
        System.err.println(p.getRules().length);
    }

    @Test
    public void testGetRules() throws Exception {
        Event testEvent = new Event("nomatter_domain", "hello");
        session.insert(testEvent);
        session.fireAllRules();
    }

    @Test
    public void testFireRules() throws Exception {
        testGetRules();
        Assert.assertEquals(listener.numFired, 1);
    }

    @Test
    public void testAddRule() throws Exception {
        session.dispose();
        source.add(RuleBaseElement.Rule, "test3", "when\n" + "  e : Event( name == \"hello\")\n" + "then\n"
                + "  System.out.println(\"this rule was added by the addrule-function\");\n");

        session = rb.newStatefulSession();
        listener = new RuleListener2();
        session.addEventListener(listener);
        Event event = new Event("", "hello");
        session.insert(event);
        session.fireAllRules();
        assertEquals(2, listener.numFired);
    }
}
