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
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.api.workflow.EventRegistrationService;
import org.openengsb.core.api.workflow.model.RemoteEvent;
import org.openengsb.core.api.workflow.model.RuleBaseElementId;
import org.openengsb.core.api.workflow.model.RuleBaseElementType;
import org.openengsb.core.common.util.ModelUtils;
import org.openengsb.core.services.internal.RequestHandlerImpl;
import org.openengsb.core.workflow.internal.RegistrationServiceImpl;
import org.openengsb.core.workflow.model.TestEvent;

public class RegistrationServiceTest extends AbstractWorkflowServiceTest {

    private EventRegistrationService regService;
    private RequestHandler requestHandler;
    private OutgoingPort outgoingPort;

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        requestHandler = getRequestHandler();

        regService = getRegistrationService();
        registerServiceViaId(requestHandler, "requestHandler", RequestHandler.class);
        outgoingPort = mockService(OutgoingPort.class, "testPort");

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        requestHandler.handleCall(((MethodCallRequest) invocation.getArguments()[0]).getMethodCall());
                    };
                };
                executorService.execute(runnable);
                return null;
            }
        }).when(outgoingPort).send(any(MethodCallRequest.class));
    }

    private RequestHandler getRequestHandler() {
        RequestHandlerImpl requestHandlerImpl = new RequestHandlerImpl();
        return requestHandlerImpl;
    }

    private RegistrationServiceImpl getRegistrationService() {
        RegistrationServiceImpl registrationServiceImpl = new RegistrationServiceImpl();
        registrationServiceImpl.setRuleManager(manager);
        return registrationServiceImpl;
    }

    @Test
    public void testWrapRemoteEvent() throws Exception {
        TestEvent event = ModelUtils.createEmptyModelObject(TestEvent.class);
        event.setProcessId(3L);
        event.setValue("bla");
        RemoteEvent wrapEvent = RemoteEventUtil.wrapEvent(event);
        Map<String, String> properties = wrapEvent.getNestedEventProperties();
        assertThat(wrapEvent.getClassName(), is(TestEvent.class.getName()));
        assertThat(properties.get("processId"), is("3"));
    }

    @Test
    public void testRegisterEvent() throws Exception {
        RemoteEvent reg = ModelUtils.createEmptyModelObject(RemoteEvent.class);
        reg.setContextValues(new HashMap<String, String>());
        reg.setNestedEventProperties(new HashMap<String, String>());
        reg.setClassName(TestEvent.class.getName());
        reg.setProcessId(3L);
        TestEvent test = ModelUtils.createEmptyModelObject(TestEvent.class);
        regService.registerEvent(reg, "testPort", "test://localhost");
        service.processEvent(test);
        verify(outgoingPort, timeout(5000)).send(any(MethodCallRequest.class));
    }

    @Test
    public void testRegisterEvent_shouldCreateRule() throws Exception {
        RemoteEvent reg = ModelUtils.createEmptyModelObject(RemoteEvent.class);
        reg.setContextValues(new HashMap<String, String>());
        reg.setNestedEventProperties(new HashMap<String, String>());
        reg.setClassName(TestEvent.class.getName());
        int oldCount = manager.list(RuleBaseElementType.Rule).size();
        regService.registerEvent(reg, "testPort", "test://localhost");
        assertThat(manager.list(RuleBaseElementType.Rule).size(), is(oldCount + 1));
    }

    @Test
    public void testRegisterEvent_shouldProcessRemoteEvent() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        trackInvocations((DummyExampleDomain) domains.get("example"), latch).doSomething("it works");

        RemoteEvent reg = ModelUtils.createEmptyModelObject(RemoteEvent.class);
        reg.setContextValues(new HashMap<String, String>());
        reg.setNestedEventProperties(new HashMap<String, String>());
        reg.setClassName(TestEvent.class.getName());
        regService.registerEvent(reg, "testPort", "test://localhost", "workflowService");
        String ruleCode = "when RemoteEvent() then example.doSomething(\"it works\");";
        manager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "react to remote-event"), ruleCode);
        service.processEvent(ModelUtils.createEmptyModelObject(TestEvent.class));
        assertThat(latch.await(5, TimeUnit.SECONDS), is(true));

        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);
    }

    @Test
    public void testRegisterMultipleEvents_shouldOnlyProcessOneEvent() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        trackInvocations((DummyExampleDomain) domains.get("example"), latch).doSomething("it works");

        RemoteEvent reg = ModelUtils.createEmptyModelObject(RemoteEvent.class);
        reg.setContextValues(new HashMap<String, String>());
        reg.setNestedEventProperties(new HashMap<String, String>());
        reg.setClassName(TestEvent.class.getName());
        regService.registerEvent(reg, "testPort", "test://localhost", "workflowService");
        RemoteEvent reg2 = ModelUtils.createEmptyModelObject(RemoteEvent.class);
        reg.setClassName(TestEvent.class.getName());
        Map<String, String> nestedEventProperties = new HashMap<String, String>();
        nestedEventProperties.put("value", "testValue");
        reg2.setNestedEventProperties(nestedEventProperties);
        regService.registerEvent(reg2, "testPort", "test://localhost", "workflowService");
        String ruleCode = "when RemoteEvent() then example.doSomething(\"it works\");";
        manager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "react to remote-event"), ruleCode);
        service.processEvent(ModelUtils.createEmptyModelObject(TestEvent.class));

        assertThat(latch.await(5, TimeUnit.SECONDS), is(true));

        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);
    }

    private <T> T trackInvocations(T mock, final CountDownLatch latch) {
        return doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                latch.countDown();
                return null;
            }
        }).when(mock);
    }

}
