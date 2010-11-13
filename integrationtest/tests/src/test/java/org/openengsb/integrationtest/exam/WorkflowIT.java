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

package org.openengsb.integrationtest.exam;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.common.Domain;
import org.openengsb.core.common.Event;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.util.AliveState;
import org.openengsb.core.common.workflow.RuleManager;
import org.openengsb.core.common.workflow.WorkflowService;
import org.openengsb.core.common.workflow.model.RuleBaseElementId;
import org.openengsb.core.common.workflow.model.RuleBaseElementType;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.integrationtest.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
public class WorkflowIT extends AbstractExamTestHelper {

    public static class DummyLogDomain implements ExampleDomain {
        private boolean wasCalled = false;

        @Override
        public String doSomething(String message) {
            this.wasCalled = true;
            return "something";
        }

        @Override
        public AliveState getAliveState() {
            return AliveState.OFFLINE;
        }

        @Override
        public String doSomething(ExampleEnum exampleEnum) {
            this.wasCalled = true;
            return "something";
        }

        @Override
        public String doSomethingWithLogEvent(LogEvent event) {
            this.wasCalled = true;
            return "something";
        }

        public boolean isWasCalled() {
            return wasCalled;
        }
    }

    @Test
    public void testSendEvent() throws Exception {
        addHelloWorldRule();

        ContextCurrentService contextService = retrieveService(getBundleContext(), ContextCurrentService.class);
        contextService.createContext("42");
        contextService.setThreadLocalContext("42");
        contextService.putValue("domain/ExampleDomain/defaultConnector/id", "dummyLog");

        /*
         * This is kind of a workaround. But for some reason when the workflow-service waits for these services for 30
         * seconds, they don't show up. But when provoking an AssertionError using the line below, the services show up,
         * and the test runs just fine - ChristophGr
         */
        retrieveService(getBundleContext(), ExampleDomain.class);

        Dictionary<String, String> properties = new Hashtable<String, String>();
        String[] clazzes = new String[]{ Domain.class.getName(), ExampleDomain.class.getName() };
        properties.put("id", "dummyLog");

        DummyLogDomain logService = new DummyLogDomain();
        getBundleContext().registerService(clazzes, logService, properties);

        WorkflowService workflowService = retrieveService(getBundleContext(), WorkflowService.class);
        Event e = new Event("42");
        workflowService.processEvent(e);

        assertThat(logService.isWasCalled(), is(true));
    }

    private void addHelloWorldRule() throws Exception {
        RuleManager ruleManager = retrieveService(getBundleContext(), RuleManager.class);
        ruleManager.addImport("org.openengsb.domain.example.ExampleDomain");
        ruleManager.addGlobal("org.openengsb.domain.example.ExampleDomain", "example");

        RuleBaseElementId id = new RuleBaseElementId(RuleBaseElementType.Rule, "hello1");
        String rule = readRule();
        ruleManager.add(id, rule);
    }

    private String readRule() throws IOException {
        InputStream helloWorldRule = null;
        try {
            helloWorldRule = this.getClass().getClassLoader().getResourceAsStream("rulebase/org/openengsb/hello1.rule");
            return IOUtils.toString(helloWorldRule);
        } finally {
            IOUtils.closeQuietly(helloWorldRule);
        }
    }
}
