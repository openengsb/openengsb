package org.openengsb.drools.source;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Random;

import org.drools.RuleBase;
import org.drools.StatefulSession;
import org.drools.rule.Package;
import org.drools.rule.Rule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.model.Event;
import org.openengsb.drools.RuleBaseException;
import org.openengsb.drools.RuleListener;
import org.openengsb.drools.message.RuleBaseElementId;
import org.openengsb.drools.message.RuleBaseElementType;

public abstract class RuleSourceTest<SourceType extends RuleBaseSource> {

    protected RuleBaseSource source;
    protected RuleBase rulebase;
    protected StatefulSession session;
    protected RuleListener listener;

    @Before
    public void setUp() throws Exception {
        source = getRuleBaseSource();
        rulebase = source.getRulebase();
    }

    @After
    public void tearDown() throws Exception {
        if (session != null) {
            session.dispose();
        }
    }

    protected abstract RuleBaseSource getRuleBaseSource();

    /**
     * create new stateful session from the rulebase and attach a listener to
     * validate testresults
     */
    protected void createSession() {
        if (session != null) {
            session.dispose();
            session = null;
        }
        session = rulebase.newStatefulSession();
        listener = new RuleListener();
        session.addEventListener(listener);
    }

    /**
     * inserts an Event into the existing session and fires All rules
     */
    protected void executeTestSession() {
        Event event = new Event("", "hello");
        session.insert(event);
        session.fireAllRules();
    }

    @Test
    public void testGetRuleBase() throws Exception {
        assertNotNull(rulebase);
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
    public void testAddRule() throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "test3");
        source.add(id, "when\n" + "  e : Event( name == \"hello\")\n" + "then\n"
                + "  System.out.println(\"this rule was added by the addrule-function\");\n");
        createSession();
        executeTestSession();
        assertTrue(listener.haveRulesFired("test3"));
    }

    @Test
    public void testAddImport() throws Exception {
        Package p = getPackage();
        assertNull(p.getImports().get("java.util.Currency"));
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Import, "java.util.Currency");
        source.add(id, "java.util.Currency");
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
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Import, "java.util.Currency");
        source.add(id, "ignored");
        source.delete(id);
        p = getPackage();
        assertNull(p.getImports().get("java.util.Currency"));
    }

    @Test
    public void testAddFunction() throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Function, "org.openengsb", "notify");
        source.add(id, "function void notify(String message) {\n" + "System.out.println(\"notify: \" + message);\n}");
        Package p = getPackage();
        assertFalse(p.getFunctions().isEmpty());
        assertNotNull(p.getFunctions().get("notify"));
    }

    @Test
    public void testRemoveFunction() throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Function, "org.openengsb", "notify");
        source.add(id, "function void notify(String message) {\n" + "System.out.println(\"notify: \" + message);\n}");
        source.delete(id);
        Package p = getPackage();
        assertNull(p.getFunctions().get("notify"));
    }

    @Test
    public void testRuleCallingFunctionUsingImport() throws Exception {
        RuleBaseElementId testFunctionId = new RuleBaseElementId(RuleBaseElementType.Function, "org.openengsb", "test");
        source.add(testFunctionId, "function void test(Object message) {\n"
                + "System.out.println(\"notify: \" + message);\n}");
        RuleBaseElementId testImportId = new RuleBaseElementId(RuleBaseElementType.Import, "org.openengsb",
                "java.util.Random");
        source.add(testImportId, "ignored");
        RuleBaseElementId testRuleId = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "test");
        source.add(testRuleId, "when\n" + "  e : Event( name == \"testevent\")\n" + "then\n"
                + "  test(new Random());\n");
        createSession();

        session.insert(new Event("", "testevent"));
        session.fireAllRules();
        assertTrue(listener.haveRulesFired("org.openengsb.test"));
    }

    @Test
    public void testGlobalLoaded() throws Exception {
        String global = getPackage().getGlobals().get("test");
        assertNotNull(global);
    }

    @Test
    public void testGlobalPresent() throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Global, "test");
        String global = source.get(id);
        assertNotNull(global);
    }

    @Test
    public void testAddGlobal() throws Exception {
        source.add(new RuleBaseElementId(RuleBaseElementType.Global, "bla"), "java.util.Random");
        source.add(new RuleBaseElementId(RuleBaseElementType.Rule, "bla"),
                "when\n then System.out.println(bla.nextInt());");
        createSession();
        session.setGlobal("bla", new Random());
        session.insert(new Event("", "asd"));
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
        Rule rule = source.getRulebase().getPackage("org.openengsb").getRule("test");
        assertNull(rule);
    }

    @Test(expected = RuleBaseException.class)
    public void testAddExistingRule() throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "hello1");
        source.add(id, "when\nthen\nSystem.out.println(\"bla\");");
    }

    @Test
    public void testAddOtherPackages() throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, "at.ac.tuwien", "hello42");
        source.add(id, "when\nthen\nSystem.out.println(\"bla\");");
        createSession();
        executeTestSession();
        assertTrue(listener.haveRulesFired("at.ac.tuwien.hello42"));
    }

    @Test
    public void testRulesInDifferentPackages() throws Exception {
        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, "at.ac.tuwien", "hello42");
        source.add(id, "when\nthen\nSystem.out.println(\"bla\");");
        id = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "hello42");
        source.add(id, "when\nthen\nSystem.out.println(\"bla\");");
        createSession();
        executeTestSession();
        assertTrue(listener.haveRulesFired("org.openengsb.hello42", "at.ac.tuwien.hello42"));
    }

}
