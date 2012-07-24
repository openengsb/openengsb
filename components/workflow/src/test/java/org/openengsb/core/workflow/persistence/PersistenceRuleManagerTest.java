/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.workflow.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.Collection;
import java.util.Random;

import org.drools.KnowledgeBase;
import org.drools.runtime.StatefulKnowledgeSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.test.AbstractOpenEngSBTest;
import org.openengsb.core.workflow.util.RuleUtil;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.domain.example.model.ExampleResponseModel;

public class PersistenceRuleManagerTest extends AbstractOpenEngSBTest {

    private RuleManager ruleManager;
    private KnowledgeBase rulebase;
    private StatefulKnowledgeSession session;
    private RuleListener listener;

    @Before
    public void setUp() throws Exception {
        ruleManager = RuleUtil.getRuleManager();
        RuleUtil.addImportsAndGlobals(ruleManager);
        rulebase = ruleManager.getRulebase();
    }

    @After
    public void tearDown() throws Exception {
        if (session != null) {
            session.dispose();
        }
    }

    @Test
    public void testGetRuleBase() throws Exception {
        assertThat(rulebase, notNullValue());
    }

    @Test
    public void testListImports() throws Exception {
        ruleManager.addImport("java.util.Map");
        Collection<String> listImports = ruleManager.listImports();
        assertThat(listImports, hasItem("java.util.Map"));
    }

    @Test
    public void testAddRule() throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "test3");
        ruleManager.add(id, "when\n" + "  e : Event()\n" + "then\n"
                + "  example2.doSomethingWithMessage(\"this rule was added by the addrule-function\");\n");
        executeTestSession();
        assertTrue(listener.haveRulesFired("test3"));
    }

    @Test
    public void testAddImport() throws Exception {
        assertThat(ruleManager.listImports(), not(hasItem("java.util.Currency")));
        ruleManager.addImport("java.util.Currency");
        assertThat(ruleManager.listImports(), hasItem("java.util.Currency"));
    }

    @Test
    public void testRemoveImport() throws Exception {
        ruleManager.addImport("java.util.Currency");
        ruleManager.removeImport("java.util.Currency");
        assertThat(ruleManager.listImports(), not(hasItem("java.util.Currency")));
    }

    @Test
    public void testRuleCallingFunctionUsingImport() throws Exception {
        RuleBaseElementId testFunctionId = new RuleBaseElementId(RuleBaseElementType.Function, "org.openengsb", "test");
        ruleManager.add(testFunctionId, "function void test(Object message) {\n"
                + "System.out.println(\"notify: \" + message);\n}");
        ruleManager.addImport("java.util.Random");
        RuleBaseElementId testRuleId = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "test");
        ruleManager.add(testRuleId, "when\n" + "  e : Event()\n" + "then\n" + "  test(new Random());\n");
        executeTestSession();
        assertTrue(listener.haveRulesFired("org.openengsb.test"));
    }

    @Test
    public void testAddGlobal() throws Exception {
        ruleManager.addGlobal("java.util.Random", "bla");
        ruleManager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "bla"),
            "when\n then example2.doSomethingWithMessage(\"\" + bla.nextInt());");
        createSession();
        session.setGlobal("bla", new Random());
        sendEventToSession();
        assertTrue(listener.haveRulesFired("bla"));
    }

    @Test
    public void testAddGlobalIfNotPresentWhenIsPresent() throws Exception {
        ruleManager.addGlobal("java.util.Random", "bla");
        ruleManager.addGlobalIfNotPresent("java.util.Random", "bla");
        assertThat(ruleManager.listGlobals().get("bla"), is("java.util.Random"));
    }

    @Test
    public void testAddGlobalIfNotPresentWhenNotPresent() throws Exception {
        ruleManager.addGlobalIfNotPresent("java.util.Random", "bla");
        assertThat(ruleManager.listGlobals().get("bla"), is("java.util.Random"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddGlobalIfNotPresentWithWrongType() throws Exception {
        ruleManager.addGlobalIfNotPresent("java.util.Random", "bla");
        ruleManager.addGlobalIfNotPresent("java.util.List", "bla");
    }

    @Test
    public void testGetAllGlobalsOfType() throws Exception {
        ruleManager.addGlobal("java.util.Random", "bla1");
        ruleManager.addGlobal("java.util.Random", "bla2");
        assertThat(ruleManager.getAllGlobalsOfType("java.util.Random"), hasItems("bla1", "bla2"));
    }

    @Test
    public void testGetGlobalType() throws Exception {
        ruleManager.addGlobal("java.util.Random", "bla1");
        String result = ruleManager.getGlobalType("bla1");
        assertThat(result, is("java.util.Random"));
    }

    @Test
    public void testInvalidAddRule() throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "test");
        try {
            ruleManager.add(id, "this_makes_no_sense_at_all");
            fail("add successful");
        } catch (RuleBaseException e) {
            // expected
        }
        Collection<RuleBaseElementId> list = ruleManager.list(RuleBaseElementType.Rule);
        assertThat(list, not(hasItem(id)));
    }

    @Test(expected = RuleBaseException.class)
    public void testAddExistingRule() throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "hello1");
        ruleManager.add(id, "when\nthen\nexample.doSomething(\"bla\");");
        ruleManager.add(id, "when\nthen\nexample.doSomething(\"bla\");");
    }

    @Test
    public void testAddOtherPackages() throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, "at.ac.tuwien", "hello42");
        ruleManager.add(id, "when\nthen\nexample2.doSomethingWithMessage(\"bla\");");
        executeTestSession();
        assertTrue(listener.haveRulesFired("at.ac.tuwien.hello42"));
    }

    @Test
    public void testRulesInDifferentPackages() throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, "at.ac.tuwien", "hello42");
        ruleManager.add(id, "when\nthen\nexample2.doSomethingWithMessage(\"bla\");");
        id = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "hello42");
        ruleManager.add(id, "when\nthen\nexample2.doSomethingWithMessage(\"bla\");");
        executeTestSession();
        assertTrue(listener.haveRulesFired("org.openengsb.hello42", "at.ac.tuwien.hello42"));
    }

    /**
     * create new stateful session from the rulebase and attach a listener to validate testresults
     */
    private void createSession() {
        session = rulebase.newStatefulKnowledgeSession();
        listener = new RuleListener();
        session.addEventListener(listener);
        ExampleDomain exampleService = new ExampleDomain() {

            @Override
            public String getInstanceId() {
                return null;
            }

            @Override
            public AliveState getAliveState() {
                return null;
            }

            @Override
            public String doSomethingWithLogEvent(LogEvent event) {
                return null;
            }

            @Override
            public String doSomethingWithMessage(String message) {
                return null;
            }

            @Override
            public ExampleResponseModel doSomethingWithModel(ExampleRequestModel model) {
                return null;
            }
        };
        session.setGlobal("example2", exampleService);
    }

    /**
     * inserts an Event into the existing session and fires All rules
     */
    private void sendEventToSession() {
        session.insert(new Event());
        session.fireAllRules();
    }

    private void executeTestSession() {
        createSession();
        sendEventToSession();
    }

}
