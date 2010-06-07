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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.io.FileUtils;
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
import org.openengsb.drools.message.RuleBaseElement;
import org.openengsb.drools.source.DirectoryRuleSource;
import org.openengsb.drools.source.RuleBaseSource;
import org.openengsb.util.IO;

public class DirectorySourceTest {

    private static class RuleListener2 extends DefaultAgendaEventListener {
        protected int numFired = 0;
        protected Set<String> rulesFired = new HashSet<String>();

        @Override
        public void afterActivationFired(AfterActivationFiredEvent event, WorkingMemory workingMemory) {
            rulesFired.add(event.getActivation().getRule().getName());
            numFired++;
            super.afterActivationFired(event, workingMemory);
        }
    }

    private RuleBaseSource source;
    private RuleBase rulebase;
    private StatefulSession session;
    private RuleListener2 listener;

    @Before
    public void setUp() throws Exception {
        File rulebaseReferenceDirectory = new File("src/test/resources/rulebase");
        File rulebaseTestDirectory = new File("data/rulebase");
        FileUtils.copyDirectory(rulebaseReferenceDirectory, rulebaseTestDirectory);

        source = new DirectoryRuleSource("data/rulebase");
        rulebase = source.getRulebase();
    }

    private void createSession() {
        if (session != null) {
            session.dispose();
            session = null;
        }
        session = rulebase.newStatefulSession();
        listener = new RuleListener2();
        session.addEventListener(listener);
    }

    @After
    public void tearDown() throws Exception {
        IO.deleteStructure(new File("data"));
        if (session != null) {
            session.dispose();
        }
    }

    @Test
    public void testGetRuleBase() throws Exception {
        Assert.assertNotNull(rulebase);
        Package p = rulebase.getPackage("org.openengsb");
        assertNotNull(p);
        System.err.println(p.getRules().length);
    }

    @Test
    public void testGetRules() throws Exception {
        createSession();
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
        source.add(RuleBaseElement.Rule, "test3", "when\n" + "  e : Event( name == \"hello\")\n" + "then\n"
                + "  System.out.println(\"this rule was added by the addrule-function\");\n");
        createSession();
        Event event = new Event("", "hello");
        session.insert(event);
        session.fireAllRules();
        assertEquals(2, listener.numFired);
    }

    @Test
    public void testAddImport() throws Exception {
        Package p = getPackage();
        assertNull(p.getImports().get("java.util.Currency"));
        source.add(RuleBaseElement.Import, "java.util.Currency", "ignored");
        p = getPackage();
        assertNotNull(p.getImports().get("java.util.Currency"));
    }

    private Package getPackage() throws RuleBaseException {
        Package p = source.getRulebase().getPackage("org.openengsb");
        return p;
    }

    @Test
    public void testRemoveImport() throws Exception {
        Package p = getPackage();
        source.add(RuleBaseElement.Import, "java.util.Currency", "ignored");
        source.delete(RuleBaseElement.Import, "java.util.Currency");
        p = getPackage();
        assertNull(p.getImports().get("java.util.Currency"));
    }

    @Test
    public void testAddFunction() throws Exception {
        source.add(RuleBaseElement.Function, "notify", "function void notify(String message) {\n"
                + "System.out.println(\"notify: \" + message);\n}");
        Package p = getPackage();
        assertFalse(p.getFunctions().isEmpty());
        assertNotNull(p.getFunctions().get("notify"));
    }

    @Test
    public void testRemoveFunction() throws Exception {
        source.add(RuleBaseElement.Function, "notify", "function void notify(String message) {\n"
                + "System.out.println(\"notify: \" + message);\n}");
        source.delete(RuleBaseElement.Function, "notify");
        Package p = getPackage();
        assertNull(p.getFunctions().get("notify"));
    }

    @Test
    public void testRuleCallingFunctionUsingImport() throws Exception {
        source.add(RuleBaseElement.Function, "test", "function void test(Object message) {\n"
                + "System.out.println(\"notify: \" + message);\n}");
        source.add(RuleBaseElement.Import, "java.util.Random", "ignored");
        source.add(RuleBaseElement.Rule, "test", "when\n" + "  e : Event( name == \"testevent\")\n" + "then\n"
                + "  test(new Random());\n");
        createSession();

        session.insert(new Event("", "testevent"));
        session.fireAllRules();
        assertEquals(1, listener.numFired);
    }

    @Test
    public void testGlobalLoaded() throws Exception {
        String global = getPackage().getGlobals().get("test");
        assertNotNull(global);
    }

    @Test
    public void testGlobalPresent() throws Exception {
        String global = source.get(RuleBaseElement.Global, "test");
        assertNotNull(global);
    }

    @Test
    public void testAddGlobal() throws Exception {
        source.add(RuleBaseElement.Global, "bla", "java.util.Random");
        source.add(RuleBaseElement.Rule, "bla", "when\n then System.out.println(bla.nextInt());");
        createSession();
        session.setGlobal("bla", new Random());
        session.insert(new Event("", "asd"));
        session.fireAllRules();

        assertTrue(listener.rulesFired.contains("bla"));
    }

    @Test
    public void testInvalidAddRule() throws Exception {
        try {
            source.add(RuleBaseElement.Rule, "test", "this_makes_no_sense_at_all");
            fail("add successful");
        } catch (RuleBaseException e) {
            // expected
        }
        String code = source.get(RuleBaseElement.Rule, "test");
        assertNull(code);
    }

    @Test(expected = RuleBaseException.class)
    public void testAddExistingRule() throws Exception {
        source.add(RuleBaseElement.Rule, "hello1", "when\nthen\nSystem.out.println(\"bla\");");
    }
}
