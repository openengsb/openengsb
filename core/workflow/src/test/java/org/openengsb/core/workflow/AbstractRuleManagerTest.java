/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.workflow;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.mock;

import java.util.Collection;
import java.util.Random;

import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.workflow.RuleBaseException;
import org.openengsb.core.common.workflow.RuleManager;
import org.openengsb.core.common.workflow.model.RuleBaseElementId;
import org.openengsb.core.common.workflow.model.RuleBaseElementType;

public abstract class AbstractRuleManagerTest<SourceType extends RuleManager> {

    protected RuleManager source;
    protected KnowledgeBase rulebase;
    protected StatefulKnowledgeSession session;
    protected RuleListener listener;

    @Before
    public void setUp() throws Exception {
        source = getRuleBaseSource();
        source.addImport(DummyExampleDomain.class.getName());
        source.addImport(Event.class.getName());
        source.addGlobal(DummyExampleDomain.class.getName(), "example");
        rulebase = source.getRulebase();
    }

    @After
    public void tearDown() throws Exception {
        if (session != null) {
            session.dispose();
        }
    }

    protected abstract RuleManager getRuleBaseSource() throws Exception;

    /**
     * create new stateful session from the rulebase and attach a listener to validate testresults
     */
    protected void createSession() {
        if (session != null) {
            session.dispose();
            session = null;
        }
        session = rulebase.newStatefulKnowledgeSession();
        listener = new RuleListener();
        session.addEventListener(listener);
        session.setGlobal("example", mock(DummyExampleDomain.class));
    }

    /**
     * inserts an Event into the existing session and fires All rules
     */
    protected void executeTestSession() {
        Event event = new Event();
        session.insert(event);
        session.fireAllRules();
    }

    @Test
    public void testGetRuleBase() throws Exception {
        assertThat(rulebase, notNullValue());
    }

    @Test
    public void testGetRules() throws Exception {
        source.addImport("java.util.HashSet");
        createSession();
        Event testEvent = new Event();
        session.insert(testEvent);
        session.fireAllRules();
    }

    @Test
    public void testListImports() throws Exception {
        source.addImport("java.util.Map");
        Collection<String> listImports = source.listImports();
        assertThat(listImports, hasItem("java.util.Map"));
    }

    @Test
    public void testAddRule() throws Exception {
        source.addImport(Event.class.getName());
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "test3");
        source.add(id, "when\n" + "  e : Event()\n" + "then\n"
                + "  example.doSomething(\"this rule was added by the addrule-function\");\n");

        createSession();
        executeTestSession();
        assertTrue(listener.haveRulesFired("test3"));
    }

    @Test
    public void testAddImport() throws Exception {
        assertThat(source.listImports(), not(hasItem("java.util.Currency")));
        source.addImport("java.util.Currency");
        assertThat(source.listImports(), hasItem("java.util.Currency"));
    }

    @Test
    public void testRemoveImport() throws Exception {
        source.addImport("java.util.Currency");
        source.removeImport("java.util.Currency");
        assertThat(source.listImports(), not(hasItem("java.util.Currency")));
    }

    @Test
    public void testRuleCallingFunctionUsingImport() throws Exception {
        RuleBaseElementId testFunctionId = new RuleBaseElementId(RuleBaseElementType.Function, "org.openengsb", "test");
        source.add(testFunctionId, "function void test(Object message) {\n"
                + "System.out.println(\"notify: \" + message);\n}");
        source.addImport("java.util.Random");
        source.addImport(Event.class.getName());
        RuleBaseElementId testRuleId = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "test");
        source.add(testRuleId, "when\n" + "  e : Event()\n" + "then\n" + "  test(new Random());\n");
        createSession();

        session.insert(new Event());
        session.fireAllRules();
        assertTrue(listener.haveRulesFired("org.openengsb.test"));
    }

    @Test
    public void testAddGlobal() throws Exception {
        source.addGlobal("java.util.Random", "bla");
        source.add(new RuleBaseElementId(RuleBaseElementType.Rule, "bla"),
            "when\n then example.doSomething(\"\" + bla.nextInt());");
        createSession();
        session.setGlobal("bla", new Random());
        session.insert(new Event());
        session.fireAllRules();
        assertTrue(listener.haveRulesFired("bla"));
    }

    @Test
    public void testInvalidAddRule() throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "test");
        try {
            source.add(id, "this_makes_no_sense_at_all");
            fail("add successful");
        } catch (RuleBaseException e) {
            // expected
        }
        Collection<RuleBaseElementId> list = source.list(RuleBaseElementType.Rule);
        assertThat(list, not(hasItem(id)));
    }

    @Test(expected = RuleBaseException.class)
    public void testAddExistingRule() throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "hello1");
        source.add(id, "when\nthen\nexample.doSomething(\"bla\");");
        source.add(id, "when\nthen\nexample.doSomething(\"bla\");");
    }

    @Test
    public void testAddOtherPackages() throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, "at.ac.tuwien", "hello42");
        source.add(id, "when\nthen\nexample.doSomething(\"bla\");");
        createSession();
        executeTestSession();
        assertTrue(listener.haveRulesFired("at.ac.tuwien.hello42"));
    }

    @Test
    public void testRulesInDifferentPackages() throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, "at.ac.tuwien", "hello42");
        source.add(id, "when\nthen\nexample.doSomething(\"bla\");");
        id = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "hello42");
        source.add(id, "when\nthen\nexample.doSomething(\"bla\");");
        createSession();
        executeTestSession();
        assertTrue(listener.haveRulesFired("org.openengsb.hello42", "at.ac.tuwien.hello42"));
    }

}
