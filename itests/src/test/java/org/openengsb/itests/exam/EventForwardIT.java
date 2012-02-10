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

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.workflow.RuleManager;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.ExampleDomainEvents;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.domain.example.event.LogEvent.LogLevel;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.domain.example.model.ExampleResponseModel;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
// This one will run each test in it's own container (slower speed)
// @ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class EventForwardIT extends AbstractPreConfiguredExamTestHelper {

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
    public void testSendEvent() throws Exception {
        authenticateAsAdmin();
        addHelloWorldRule();
        ContextCurrentService contextService = getOsgiService(ContextCurrentService.class);
        contextService.createContext("42");
        ContextHolder.get().setCurrentContextId("42");

        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        String[] clazzes = new String[]{ Domain.class.getName(), ExampleDomain.class.getName() };
        properties.put("id", "dummyLog");
        properties.put("location.42", new String[]{ "example" });

        DummyLogDomain logService = new DummyLogDomain();
        getBundleContext().registerService(clazzes, logService, properties);

        LogEvent e = ModelUtils.createEmptyModelObject(LogEvent.class);
        e.setName("42");
        e.setLevel(LogLevel.INFO);

        ExampleDomainEvents exampleEvents = getOsgiService(ExampleDomainEvents.class);
        // this should be routed through the domain, which forwards it to the workflow service
        exampleEvents.raiseEvent(e);

        assertThat(logService.isWasCalled(), is(true));
    }

    private void addHelloWorldRule() throws Exception {
        RuleManager ruleManager = getOsgiService(RuleManager.class);
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
