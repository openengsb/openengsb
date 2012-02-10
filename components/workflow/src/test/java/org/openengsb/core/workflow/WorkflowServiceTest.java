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

package org.openengsb.core.workflow;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.Event;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.workflow.RuleBaseException;
import org.openengsb.core.api.workflow.model.InternalWorkflowEvent;
import org.openengsb.core.api.workflow.model.ProcessBag;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.core.test.NullDomain;
import org.openengsb.core.test.NullEvent3;
import org.openengsb.core.workflow.model.TestEvent;

public class WorkflowServiceTest extends AbstractWorkflowServiceTest {

    private DummyExampleDomain logService;
    private DummyNotificationDomain notification;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        logService = (DummyExampleDomain) domains.get("example");
        notification = (DummyNotificationDomain) domains.get("notification");
    }

    @Test
    public void testProcessEvent() throws Exception {
        Event event = ModelUtils.createEmptyModelObject(Event.class);
        service.processEvent(event);
    }

    @Test
    public void testProcessInternalWorkflowEvent_shouldNotFail() throws Exception {
        InternalWorkflowEvent event = ModelUtils.createEmptyModelObject(InternalWorkflowEvent.class);
        event.setProcessBag(new ProcessBag());
        event.getProcessBag().setProcessId("0");
        service.processEvent(event);
    }

    @Test
    public void testProcessEventTriggersHelloWorld() throws Exception {
        Event event = ModelUtils.createEmptyModelObject(Event.class);
        service.processEvent(event);
        verify(notification, atLeast(1)).notify("Hello");
        verify((DummyExampleDomain) domains.get("example"), atLeast(1)).doSomething("Hello World");
        verify(myservice, atLeast(1)).call();
    }

    @Test
    public void testUseLog() throws Exception {
        Event event = ModelUtils.createEmptyModelObject(Event.class);
        event.setName("test-context");
        service.processEvent(event);
        verify(logService).doSomething("42");
    }

    @Test
    public void testUpdateRule() throws Exception {
        manager.update(new RuleBaseElementId(RuleBaseElementType.Rule, "hello1"),
            "when\n Event ( name == \"test-context\")\n then \n example.doSomething(\"21\");");
        Event event = ModelUtils.createEmptyModelObject(Event.class);
        event.setName("test-context");
        service.processEvent(event);
        verify(logService).doSomething("21");
    }

    @Test
    public void testUseLogContent() throws Exception {
        Event event = ModelUtils.createEmptyModelObject(Event.class);
        event.setName("test-context");
        service.processEvent(event);
        Mockito.verify(logService, Mockito.times(2)).doSomething(Mockito.anyString());
    }

    @Test
    public void addInvalidRule_shouldNotModifyRulebase() throws Exception {
        try {
            manager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "hello"), "this*is_invalid");
            fail("expected Exception");
        } catch (RuleBaseException e) {
            // expected
        }
        Event event = ModelUtils.createEmptyModelObject(Event.class);
        event.setName("test-context");
        service.processEvent(event);
        Mockito.verify(logService, Mockito.times(2)).doSomething(Mockito.anyString());
    }

    @Test
    public void invalidMofidyRule_shouldNotModifyRulebase() throws Exception {
        try {
            manager.update(new RuleBaseElementId(RuleBaseElementType.Rule, "hello1"), "this*is_invalid");
            fail("expected Exception");
        } catch (RuleBaseException e) {
            assertThat(e.getCause(), nullValue());
        }
        Event event = ModelUtils.createEmptyModelObject(Event.class);
        event.setName("test-context");
        service.processEvent(event);
        Mockito.verify(logService, Mockito.times(2)).doSomething(Mockito.anyString());
    }

    @Test
    public void testStartProcess_shouldRunScriptNodes() throws Exception {
        long id = service.startFlow("flowtest");
        service.waitForFlowToFinish(id);
        verify(logService).doSomething("context: " + ContextHolder.get().getCurrentContextId());
    }

    @Test
    public void testStartMultipleProcesses_shouldRunInCorrectContext() throws Exception {
        int tryThreads = 2;
        List<DummyExampleDomain> services = new ArrayList<DummyExampleDomain>();
        for (int i = 0; i < tryThreads; i++) {
            ContextHolder.get().setCurrentContextId(Integer.toString(i));
            services.add(registerDummyConnector(DummyExampleDomain.class, "example"));
        }
        for (int i = 0; i < tryThreads; i++) {
            ContextHolder.get().setCurrentContextId(Integer.toString(i));
            long id = service.startFlow("flowtest");
            service.waitForFlowToFinish(id);
            verify(services.get(i)).doSomething("context: " + ContextHolder.get().getCurrentContextId());
        }
    }

    @Test(timeout = 3000)
    public void testStartProcessWithEvents_shouldRunScriptNodes() throws Exception {
        Event event = ModelUtils.createEmptyModelObject(Event.class);
        event.setType(Event.class.getSimpleName());
        TestEvent testEvent = ModelUtils.createEmptyModelObject(TestEvent.class);
        testEvent.setType(Event.class.getSimpleName());

        long id = service.startFlow("floweventtest");
        service.processEvent(event);
        service.processEvent(testEvent);
        service.waitForFlowToFinish(id);
        InOrder inOrder2 = inOrder(logService);
        inOrder2.verify(logService).doSomething("start testflow");
        inOrder2.verify(logService).doSomething("first event received");
    }

    @Test(timeout = 3000)
    public void testStart2Processes_shouldOnlyTriggerSpecificEvents() throws Exception {
        long id1 = service.startFlow("floweventtest");
        long id2 = service.startFlow("floweventtest");

        Event event = ModelUtils.createEmptyModelObject(Event.class);
        event.setName("event");
        event.setProcessId(id1);
        event.setType(Event.class.getSimpleName());

        TestEvent testEvent = ModelUtils.createEmptyModelObject(TestEvent.class);
        testEvent.setProcessId(id1);
        testEvent.setType(Event.class.getSimpleName());
        
        service.processEvent(event);
        service.processEvent(testEvent);
        service.waitForFlowToFinish(id1);

        assertThat(service.getRunningFlows(), hasItem(id2));
        assertThat(service.getRunningFlows(), not(hasItem(id1)));
    }

    @Test(timeout = 3000)
    public void testCiWorkflow() throws Exception {
        long id = service.startFlow("ci");
        Event buildEvent = ModelUtils.createEmptyModelObject(Event.class);
        buildEvent.setType("BuildSuccess");
        service.processEvent(buildEvent);
        Event testEvent = ModelUtils.createEmptyModelObject(Event.class);
        testEvent.setType("TestSuccess");
        service.processEvent(testEvent);
        service.waitForFlowToFinish(id);
        verify((DummyReport) domains.get("report"), times(1)).collectData();
        verify(notification, atLeast(1)).notify(anyString());
        verify((DummyDeploy) domains.get("deploy"), times(1)).deployProject();
    }

    @Test
    public void testStartInBackground() throws Exception {
        Object lock = new Object();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("lock", lock);
        Future<Long> processIdFuture = service.startFlowInBackground("blockingFlowtest", params);
        Thread.sleep(200);
        assertThat(processIdFuture.isDone(), is(false));
        synchronized (lock) {
            lock.notify();
        }
        processIdFuture.get(2, TimeUnit.SECONDS);
        assertThat(processIdFuture.isDone(), is(true));
    }

    @Test
    public void testStartInBackgroundWithoutParams() throws Exception {
        Future<Long> processIdFuture = service.startFlowInBackground("flowtest");
        Thread.sleep(200);
        processIdFuture.get(2, TimeUnit.SECONDS);
        assertThat(processIdFuture.isDone(), is(true));
    }

    @Test
    public void testStartWorkflowTriggeredByEvent() throws Exception {
        manager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "test42"), "when\n" + "  Event()\n" + "then\n"
                + "  kcontext.getKnowledgeRuntime().startProcess(\"ci\");\n");
        service.processEvent(ModelUtils.createEmptyModelObject(Event.class));
        assertThat(service.getRunningFlows().isEmpty(), is(false));
    }

    @Test
    public void testRegisterWorkflowTrigger() throws Exception {
        Event triggerEvent = ModelUtils.createEmptyModelObject(Event.class);
        triggerEvent.setName("triggerEvent");
        service.registerFlowTriggerEvent(triggerEvent, "ci");
        service.processEvent(ModelUtils.createEmptyModelObject(Event.class));
        service.processEvent(triggerEvent);
        assertThat(service.getRunningFlows().size(), is(1));
    }

    @Test
    public void testRegisterWorkflowTriggerWithSubclass() throws Exception {
        NullEvent3 testEvent = ModelUtils.createEmptyModelObject(NullEvent3.class);
        testEvent.setName("triggerEvent");
        testEvent.setTestProperty("foo");
        testEvent.setTestStringProp("bar");
        testEvent.setTestBoolProp(true);
        testEvent.setTestIntProp(42);
        service.registerFlowTriggerEvent(testEvent, "ci");
        service.processEvent(ModelUtils.createEmptyModelObject(Event.class));
        service.processEvent(testEvent);
        assertThat(service.getRunningFlows().size(), is(1));
    }

    @Test
    public void testRegisterWorkflowTriggerIgnoreNullFields() throws Exception {
        NullEvent3 testEvent = ModelUtils.createEmptyModelObject(NullEvent3.class);
        testEvent.setName("triggerEvent");
        service.registerFlowTriggerEvent(testEvent, "ci");
        service.processEvent(ModelUtils.createEmptyModelObject(Event.class));
        service.processEvent(testEvent);
        assertThat(service.getRunningFlows().size(), is(1));
    }

    @Test
    public void testRegisterWorkflowTriggerIgnoreNullFieldsMixed() throws Exception {
        NullEvent3 testEvent = ModelUtils.createEmptyModelObject(NullEvent3.class);
        testEvent.setName("triggerEvent");
        testEvent.setTestStringProp("bar");
        testEvent.setTestIntProp(42);
        service.registerFlowTriggerEvent(testEvent, "ci");
        service.processEvent(ModelUtils.createEmptyModelObject(Event.class));
        service.processEvent(testEvent);
        assertThat(service.getRunningFlows().size(), is(1));
    }

    @Test(timeout = 3000)
    public void testRegisterWorkflowTriggerWithFlowStartedEvent() throws Exception {
        Event triggerEvent = ModelUtils.createEmptyModelObject(Event.class);
        triggerEvent.setName("triggerEvent");
        service.registerFlowTriggerEvent(triggerEvent, "flowStartedEvent");
        service.processEvent(triggerEvent);
        for (Long id : service.getRunningFlows()) {
            service.waitForFlowToFinish(id);
        }
    }

    @Test
    public void testIfEventIsRetracted() throws Exception {
        Event event = ModelUtils.createEmptyModelObject(Event.class);
        service.processEvent(event);
        event = ModelUtils.createEmptyModelObject(Event.class);
        event.setName("test-context");
        service.processEvent(event);
        verify(logService, times(2)).doSomething("Hello World");
    }

    @Test
    public void testStartProcessWithProperyBag_ChangePropertyByScriptNode_shouldChangeProperty() throws Exception {
        ProcessBag processBag = new ProcessBag();
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("processBag", processBag);

        long id = service.startFlow("propertybagtest", parameterMap);
        service.waitForFlowToFinish(id);

        assertThat((String) processBag.getProperty("test"), is(String.valueOf(id)));
    }

    @Test
    public void processEventsConcurrently_shouldProcessBothEvents() throws Exception {
        manager.addImport(TestEvent.class.getName());
        manager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "concurrent test"), "when\n"
                + "TestEvent(value == \"0\")\n"
                + "then\n"
                + "example.doSomething(\"concurrent\");");
        manager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "concurrent test1"), "when\n"
                + "TestEvent(value == \"1\")\n"
                + "then\n"
                + "Thread.sleep(1000);");

        TestEvent event1 = ModelUtils.createEmptyModelObject(TestEvent.class);
        event1.setValue("1");
        TestEvent event0 = ModelUtils.createEmptyModelObject(TestEvent.class);
        event0.setValue("0");
        Callable<Void> task = makeProcessEventTask(event1);
        Callable<Void> task2 = makeProcessEventTask(event0);
        ExecutorService executor = Executors.newCachedThreadPool();
        Future<Void> future1 = executor.submit(task);
        Thread.sleep(300);
        Future<Void> future2 = executor.submit(task2);
        future1.get();
        future2.get();
        verify(logService).doSomething("concurrent");
    }

    private Callable<Void> makeProcessEventTask(final Event event) {
        Callable<Void> task = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                service.processEvent(event);
                return null;
            }
        };
        return task;
    }

    @Test
    public void testExecuteWorkflow() throws Exception {
        Collection<RuleBaseElementId> list = manager.list(RuleBaseElementType.Process);
        System.out.println(list);
        ProcessBag result = service.executeWorkflow("simpleFlow", new ProcessBag());
        assertThat((Integer) result.getProperty("test"), is(42));
        assertThat((String) result.getProperty("alternativeName"),
            is("The answer to life the universe and everything"));
    }

    @Test
    public void testCancelWorkflow() throws Exception {
        long pid = service.startFlow("ci");
        service.cancelFlow(pid);
        service.waitForFlowToFinish(pid, 5000);
    }

    @Test
    public void testCancelWorkflowWithOpenTasks() throws Exception {
        long pid = service.startFlow("ci");
        ProcessBag bag = new ProcessBag();
        bag.setProcessId(Long.toString(pid));
        taskboxInternal.createNewTask(bag);
        service.cancelFlow(pid);
        service.waitForFlowToFinish(pid, 5000);
        assertThat("Tasks were not cancelled properly", taskbox.getOpenTasks().isEmpty(), is(true));
    }

    public void testWaitForFlow_shouldReturnTrue() throws Exception {
        Future<Long> pid = service.startFlowInBackground("flowtest");
        boolean finished = service.waitForFlowToFinish(pid.get(), 400);
        assertThat(finished, is(true));
    }

    @Test
    public void testWaitForFlowThatCannotFinish_shouldReturnFalse() throws Exception {
        Long pid = service.startFlow("floweventtest");
        Event event = ModelUtils.createEmptyModelObject(Event.class);
        event.setName("FirstEvent");
        service.processEvent(event);
        service.startFlowInBackground("flowtest");
        boolean finished = service.waitForFlowToFinish(pid, 400);
        assertThat(finished, is(false));
    }

    @Test
    public void testAddElementToDifferentPackage_shouldNotAddOpenEngSBFlows() throws Exception {
        manager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "org.test", "testrule"), "when\nthen\n");
        // Drools throws Exceptions when a flow does not belong to the package
    }

    @Test
    public void testResponseRule() throws Exception {
        NullDomain nullDomainImpl = mock(NullDomain.class);
        registerServiceViaId(nullDomainImpl, "test-connector", NullDomain.class, Domain.class);

        manager.addImport(NullDomain.class.getName());
        manager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "response-test"), ""
                + "when\n"
                + "   e : Event()\n"
                + "then\n"
                + "   NullDomain origin = (NullDomain) OsgiHelper.getResponseProxy(e, NullDomain.class);"
                + "   origin.nullMethod(42);");

        Event event = ModelUtils.createEmptyModelObject(Event.class);
        event.setOrigin("test-connector");
        service.processEvent(event);
        verify(nullDomainImpl).nullMethod(42);
    }

    @Test
    public void testTriggerExceptionInEventProcessing_shouldNotKeepLocked() throws Exception {
        manager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "response-test"), ""
                + "when\n"
                + "   e : Event(name==\"evil\")\n"
                + "then\n"
                + "   String testxx = null;"
                + "   testxx.toString();"); // provoke NPE
        try {
            Event event = ModelUtils.createEmptyModelObject(Event.class);
            event.setName("evil");
            service.processEvent(event);
            fail("evil Event should trigger Exception");
        } catch (Exception e) {
            // expected
        }
        final AtomicReference<Exception> exceptionOccured = new AtomicReference<Exception>();
        final AtomicBoolean completed = new AtomicBoolean(false);
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    // should work because the evil Event should have been removed
                    service.processEvent(ModelUtils.createEmptyModelObject(Event.class));
                    completed.set(true);
                } catch (Exception e) {
                    exceptionOccured.set(e);
                }
            };
        };
        t.start();
        t.join(10000);
        assertThat("processEvent did not complete in time. Seems the workflow-engine is locked",
            completed.get(), is(true));
        if (exceptionOccured.get() != null) {
            throw exceptionOccured.get();
        }
    }

    @Test
    public void serializeConsequenceException_shouldReturnString() throws Exception {
        manager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "response-test"), ""
                + "when\n"
                + "   e : Event(name==\"evil\")\n"
                + "then\n"
                + "   String testxx = null;"
                + "   testxx.toString();"); // provoke NPE
        try {
            Event event = ModelUtils.createEmptyModelObject(Event.class);
            event.setName("evil");
            service.processEvent(event);
            fail("evil Event should trigger Exception");
        } catch (Exception e) {
            String exceptionString = new ObjectMapper().writeValueAsString(e);
            assertThat(exceptionString, not(nullValue()));
        }
    }

}
