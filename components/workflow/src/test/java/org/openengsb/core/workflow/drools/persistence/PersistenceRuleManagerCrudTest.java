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

package org.openengsb.core.workflow.drools.persistence;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openengsb.core.test.AbstractOpenEngSBTest;
import org.openengsb.core.workflow.api.RuleBaseException;
import org.openengsb.core.workflow.api.RuleManager;
import org.openengsb.core.workflow.api.model.RuleBaseElementId;
import org.openengsb.core.workflow.api.model.RuleBaseElementType;
import org.openengsb.core.workflow.drools.util.RuleUtil;

@RunWith(Parameterized.class)
public class PersistenceRuleManagerCrudTest extends AbstractOpenEngSBTest {

    private RuleManager ruleManager;
    private String[] code = new String[4];
    private RuleBaseElementId[] id = new RuleBaseElementId[4];

    @Before
    public void setUp() throws Exception {
        ruleManager = RuleUtil.getRuleManager();
    }

    @Parameters
    public static List<Object[]> data() {
        List<Object[]> data = new ArrayList<Object[]>();

        List<TestElement> testData = new ArrayList<TestElement>();
        // functions:
        RuleBaseElementId funcId1 = new RuleBaseElementId(RuleBaseElementType.Function, "org.openengsb", "test42");
        RuleBaseElementId funcId2 = new RuleBaseElementId(RuleBaseElementType.Function, "at.ac.tuwien", "test");
        String func1 = "function void test42(){ System.out.println(\"sample-code\");}";
        String func2 = "function void test(){ System.out.println(\"bla42\");}";

        testData.add(new TestElement(funcId1, func1));
        testData.add(new TestElement(funcId1, func1));
        testData.add(new TestElement(funcId2, func2));
        testData.add(new TestElement(funcId2, func2));
        data.add(new Object[]{ testData });

        testData = new ArrayList<TestElement>();

        RuleBaseElementId ruleId1 = new RuleBaseElementId(RuleBaseElementType.Rule, "org.openengsb", "test42");
        RuleBaseElementId ruleId2 = new RuleBaseElementId(RuleBaseElementType.Rule, "at.ac.tuwien", "test");
        String rule1 = "when\nthen\nSystem.out.println(\"sample-code\");";
        String rule2 = "when\nthen\nSystem.out.println(\"\");";

        testData.add(new TestElement(ruleId1, rule1));
        testData.add(new TestElement(ruleId1, rule2));
        testData.add(new TestElement(ruleId2, rule1));
        testData.add(new TestElement(ruleId2, rule2));
        data.add(new Object[]{ testData });

        testData = new ArrayList<TestElement>();
        String sampleFlow = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<process xmlns=\"http://drools.org/drools-5.0/process\""
                + "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\""
                + "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\""
                + "         type=\"RuleFlow\" name=\"flowname\" id=\"flowId\" "
                + "package-name=\"org.openengsb\" >" + "" + "  <header>" + "  </header>" + "  <nodes>"
                + "    <start id=\"1\" name=\"Start\" x=\"100\" y=\"100\" width=\"48\" height=\"48\" />"
                + "    <end id=\"2\" name=\"End\" x=\"245\" y=\"105\" width=\"48\" height=\"48\" />" + "  </nodes>"
                + "  <connections>" + "    <connection from=\"1\" to=\"2\" />" + "  </connections>" + "</process>";

        RuleBaseElementId flowId1 = new RuleBaseElementId(RuleBaseElementType.Process, "org.openengsb", "flowId");
        RuleBaseElementId flowId2 = new RuleBaseElementId(RuleBaseElementType.Process, "at.ac.tuwien", "flowId2");

        testData.add(new TestElement(flowId1, sampleFlow));
        testData.add(new TestElement(flowId1, sampleFlow));
        sampleFlow = sampleFlow.replace("org.openengsb", "at.ac.tuwien").replace("flowId", "flowId2");
        testData.add(new TestElement(flowId2, sampleFlow));
        testData.add(new TestElement(flowId2, sampleFlow));

        data.add(new Object[]{ testData });
        return data;
    }

    public PersistenceRuleManagerCrudTest(List<TestElement> testelements) {
        for (int i = 0; i < 3; i++) {
            TestElement el = testelements.get(i);
            code[i] = el.code;
            id[i] = el.id;
        }
    }

    @Test
    public void testCreate() throws RuleBaseException {
        ruleManager.add(id[0], code[0]);
        assertEquals(code[0], ruleManager.get(id[0]));
    }

    @Test
    public void testUpdate() throws RuleBaseException {
        ruleManager.add(id[0], code[0]);
        ruleManager.update(id[1], code[1]);
        assertThat(ruleManager.get(id[0]), equalTo(code[1]));
    }

    @Test
    public void testList() throws RuleBaseException {
        ruleManager.add(id[0], code[0]);
        ruleManager.add(id[2], code[2]);
        Collection<RuleBaseElementId> result = ruleManager.list(id[0].getType(), id[0].getPackageName());
        assertThat(result, hasItem(id[0]));
        assertThat(result, not(hasItem(id[2])));
    }

    @Test
    public void testListAll() throws RuleBaseException {
        ruleManager.add(id[0], code[0]);
        ruleManager.add(id[2], code[2]);
        Collection<RuleBaseElementId> result = ruleManager.list(id[0].getType());
        assertThat(result, hasItem(id[0]));
        assertThat(result, hasItem(id[2]));
    }

    @Test
    public void testDelete() throws RuleBaseException {
        ruleManager.add(id[0], code[0]);
        ruleManager.delete(id[0]);
    }

    @Test
    public void testAddOrUpdateOnNewRule() throws Exception {
        ruleManager.addOrUpdate(id[0], code[0]);
        assertThat(ruleManager.get(id[0]), is(code[0]));
    }

    @Test
    public void testAddOrUpdateOnExistingRule() throws Exception {
        ruleManager.add(id[0], code[0]);
        ruleManager.addOrUpdate(id[1], code[1]);
        assertThat(ruleManager.get(id[0]), is(code[1]));
    }

    public static final class TestElement {
        private final RuleBaseElementId id;
        private final String code;

        public TestElement(RuleBaseElementId id, String code) {
            this.id = id;
            this.code = code;
        }
    }

}
