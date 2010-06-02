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

import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.drools.WorkingMemory;
import org.drools.event.AfterActivationFiredEvent;
import org.drools.event.DefaultAgendaEventListener;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.model.Event;

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
        source = new DirectoryRuleSource("./src/test/resources/");
        rb = source.getRulebase();
        session = rb.newStatefulSession();
        listener = new RuleListener2();
        session.addEventListener(listener);
    }

    @After
    public void tearDown() throws Exception {
        session.dispose();
    }

    @Test
    public void testGetRuleBase() throws Exception {
        Assert.assertNotNull(rb);
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
        Assert.assertEquals(listener.numFired, 2);
    }
}
