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

package org.openengsb.itests.exam;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Constants;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.WorkflowService;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.domain.example.model.ExampleResponseModel;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

@RunWith(JUnit4TestRunner.class)
// This one will run each test in it's own container (slower speed)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class WorkflowIT extends AbstractPreConfiguredExamTestHelper {

    public static class DummyLogDomain extends AbstractOpenEngSBService implements ExampleDomain {
        private boolean wasCalled = false;

        @Override
        public String doSomething(String message) {
            wasCalled = true;
            return "something";
        }

        @Override
        public AliveState getAliveState() {
            return AliveState.OFFLINE;
        }

        @Override
        public String doSomething(ExampleEnum exampleEnum) {
            wasCalled = true;
            return "something";
        }

        @Override
        public String doSomethingWithLogEvent(LogEvent event) {
            wasCalled = true;
            return "something";
        }

        public boolean isWasCalled() {
            return wasCalled;
        }

        @Override
        public ExampleResponseModel doSomething(ExampleRequestModel model) {
            wasCalled = true;
            return ModelUtils.createEmptyModelObject(ExampleResponseModel.class);
        }
    }

    @Test
    public void testCreateRuleAndTriggerDomain() throws Exception {
        DummyLogDomain exampleMock = new DummyLogDomain();
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("domain", "example");
        properties.put("connector", "example");
        properties.put("location.foo", "example2");
        getBundleContext().registerService(ExampleDomain.class.getName(), exampleMock, properties);

        RuleManager ruleManager = getOsgiService(RuleManager.class);

        ruleManager.addImport(ExampleDomain.class.getName());
        ruleManager.addImport(LogEvent.class.getName());

        ruleManager.addGlobal(ExampleDomain.class.getName(), "example2");

        ruleManager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "example-trigger"), "" +
                "when\n" +
                "    l : LogEvent()\n" +
                "then\n" +
                "    example2.doSomething(\"42\");\n"
            );

        ContextHolder.get().setCurrentContextId("foo");
        WorkflowService workflowService = getOsgiService(WorkflowService.class);

        authenticate("admin", "password");
        workflowService.processEvent(new LogEvent());

        assertThat(exampleMock.wasCalled, is(true));
    }

    @Test
    public void testCreateAndTriggerResponseRule_shouldCallOrigin() throws Exception {
        DummyLogDomain exampleMock = new DummyLogDomain();
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("domain", "example");
        properties.put("connector", "example");
        properties.put("location.foo", "example2");
        properties.put(Constants.ID_KEY, "example2");
        getBundleContext().registerService(ExampleDomain.class.getName(), exampleMock, properties);

        RuleManager ruleManager = getOsgiService(RuleManager.class);

        ruleManager.addImport(ExampleDomain.class.getName());
        ruleManager.addImport(LogEvent.class.getName());

        ruleManager.addGlobal(ExampleDomain.class.getName(), "example2");

        ruleManager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "example-response"), ""
                + "when\n"
                + "    l : LogEvent()\n"
                + "then\n"
                + "   ExampleDomain origin = (ExampleDomain) OsgiHelper.getResponseProxy(l, ExampleDomain.class);"
                + "   origin.doSomething(\"42\");"
            );

        ContextHolder.get().setCurrentContextId("foo");
        WorkflowService workflowService = getOsgiService(WorkflowService.class);
        LogEvent event = new LogEvent();
        event.setOrigin("example2");
        authenticateAsAdmin();
        workflowService.processEvent(event);

        assertThat(exampleMock.wasCalled, is(true));
    }

    /**
     * Ignored because security manager is commented in the moment.
     */
    @Ignore
    @Test(expected = Exception.class)
    public void testUserAccessToRuleManager_shouldThrowException() throws Exception {
        authenticate("user", "password");
    }

}
