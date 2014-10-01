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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.apache.karaf.features.FeaturesService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.EventSupport;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.model.ConnectorDescription;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.workflow.api.RuleManager;
import org.openengsb.core.workflow.api.WorkflowService;
import org.openengsb.core.workflow.api.model.RuleBaseElementId;
import org.openengsb.core.workflow.api.model.RuleBaseElementType;
import org.openengsb.domain.example.ExampleDomain;
import org.openengsb.domain.example.event.LogEvent;
import org.openengsb.domain.example.model.ExampleRequestModel;
import org.openengsb.domain.example.model.ExampleResponseModel;
import org.openengsb.itests.remoteclient.ExampleConnector;
import org.openengsb.itests.remoteclient.SecureSampleConnector;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

@RunWith(JUnit4TestRunner.class)
// This one will run each test in it's own container (slower speed)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class WorkflowIT extends AbstractPreConfiguredExamTestHelper {

    private DummyLogDomain exampleMock;

    @Inject
    private FeaturesService featuresService;

    public static class DummyLogDomain extends AbstractOpenEngSBService implements ExampleDomain, EventSupport {
        private boolean wasCalled = false;
        private Event lastEvent;

        @Override
        public String doSomethingWithMessage(String message) {
            wasCalled = true;
            return "something";
        }

        @Override
        public AliveState getAliveState() {
            return AliveState.OFFLINE;
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
        public ExampleResponseModel doSomethingWithModel(ExampleRequestModel model) {
            wasCalled = true;
            return new ExampleResponseModel();
        }

        @Override
        public void onEvent(Event event) {
            lastEvent = event;
        }
    }

    @Before
    public void setUp() throws Exception {
        exampleMock = new DummyLogDomain();
        Dictionary<String, Object> properties = new Hashtable<String, Object>();
        properties.put("domain", "example");
        properties.put("connector", "example");
        properties.put("location.foo", "example2");
        properties.put(org.osgi.framework.Constants.SERVICE_PID, "example2");
        getBundleContext().registerService(new String[]{ ExampleDomain.class.getName(), EventSupport.class.getName() },
                exampleMock, properties);
    }

    @Test
    public void testCreateRuleAndTriggerDomain_shouldTriggerDomain() throws Exception {
        RuleManager ruleManager = getOsgiService(RuleManager.class);

        ruleManager.addImport(ExampleDomain.class.getName());
        ruleManager.addImport(LogEvent.class.getName());

        ruleManager.addGlobal(ExampleDomain.class.getName(), "example2");

        ruleManager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "example-trigger"), ""
                + "when\n"
                + "    l : LogEvent()\n"
                + "then\n"
                + "    example2.doSomethingWithMessage(\"42\");\n"
        );

        ContextHolder.get().setCurrentContextId("foo");
        WorkflowService workflowService = getOsgiService(WorkflowService.class);

        authenticate("admin", "password");
        workflowService.processEvent(new LogEvent());

        assertThat(exampleMock.wasCalled, is(true));
    }

    @Test
    public void testCreateAndTriggerResponseRule_shouldCallOrigin() throws Exception {
        RuleManager ruleManager = getOsgiService(RuleManager.class);

        ruleManager.addImport(ExampleDomain.class.getName());
        ruleManager.addImport(LogEvent.class.getName());

        ruleManager.addGlobal(ExampleDomain.class.getName(), "example2");

        ruleManager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "example-response"), ""
                + "when\n"
                + "    l : LogEvent()\n"
                + "then\n"
                + "   ExampleDomain origin = (ExampleDomain) OsgiHelper.getResponseProxy(l, ExampleDomain.class);"
                + "   origin.doSomethingWithMessage(\"42\");"
        );

        ContextHolder.get().setCurrentContextId("foo");
        WorkflowService workflowService = getOsgiService(WorkflowService.class);
        LogEvent event = new LogEvent();
        event.setOrigin("example2");
        authenticateAsAdmin();
        workflowService.processEvent(event);

        assertThat(exampleMock.wasCalled, is(true));
    }

    @Test
    @Ignore
    public void testRaiseEvent_shouldForwardToConnector() throws Exception {
        ContextHolder.get().setCurrentContextId("foo");
        WorkflowService workflowService = getOsgiService(WorkflowService.class);
        authenticateAsAdmin();
        Event event = new Event();
        workflowService.processEvent(event);
        assertThat(exampleMock.lastEvent, equalTo(event));
    }

    @Test
    public void testRaiseEvent_shouldForwardToRemoteConnector() throws Exception {
        featuresService.installFeature("openengsb-ports-jms");
        String openwirePort = getConfigProperty("org.openengsb.infrastructure.jms", "openwire");
        SecureSampleConnector remoteConnector = new SecureSampleConnector(openwirePort);
        final AtomicReference<Event> eventRef = new AtomicReference<Event>();
        Map<String, String> attributes = new HashMap<String, String>();
        Map<String, Object> properties = new HashMap<String, Object>();
        attributes.put("mixin.1", EventSupport.class.getName());
        remoteConnector.start(new MyExampleConnector(eventRef),
                new ConnectorDescription("example", "external-connector-proxy", attributes, properties));
        WorkflowService workflowService = getOsgiService(WorkflowService.class);
        Event event = new Event("test");
        ContextHolder.get().setCurrentContextId("foo");
        authenticateAsAdmin();
        workflowService.processEvent(event);
        assertThat(eventRef.get().getName(), equalTo("test"));
    }

    public static class MyExampleConnector extends ExampleConnector {
        private final AtomicReference<Event> eventRef;

        public MyExampleConnector(AtomicReference<Event> eventRef) {
            this.eventRef = eventRef;
        }

        @Override
        public void onEvent(Event event) {
            eventRef.set(event);
        }
    }
}
