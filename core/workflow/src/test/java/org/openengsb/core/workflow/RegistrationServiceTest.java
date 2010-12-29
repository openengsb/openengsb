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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.communication.MethodCall;
import org.openengsb.core.common.communication.OutgoingPort;
import org.openengsb.core.common.communication.RequestHandler;
import org.openengsb.core.common.internal.CallRouterImpl;
import org.openengsb.core.common.internal.RequestHandlerImpl;
import org.openengsb.core.common.workflow.EventRegistrationService;
import org.openengsb.core.common.workflow.model.RemoteEvent;
import org.openengsb.core.common.workflow.model.RuleBaseElementId;
import org.openengsb.core.common.workflow.model.RuleBaseElementType;
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
        CallRouterImpl callRouterImpl = new CallRouterImpl();
        callRouterImpl.setBundleContext(bundleContext);
        requestHandler = getRequestHandler();

        regService = getRegistrationService();
        registerService(requestHandler, "requestHandler", RequestHandler.class);
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
                        requestHandler.handleCall((MethodCall) invocation.getArguments()[1]);
                    };
                };
                executorService.execute(runnable);
                return null;
            }
        }).when(outgoingPort).send(any(URI.class), any(MethodCall.class));
    }

    private RequestHandler getRequestHandler() {
        RequestHandlerImpl requestHandlerImpl = new RequestHandlerImpl();
        requestHandlerImpl.setBundleContext(bundleContext);
        return requestHandlerImpl;
    }

    private RegistrationServiceImpl getRegistrationService() {
        RegistrationServiceImpl registrationServiceImpl = new RegistrationServiceImpl();
        registrationServiceImpl.setRuleManager(manager);
        return registrationServiceImpl;
    }

    @Test
    public void testWrapRemoteEvent() throws Exception {
        TestEvent event = new TestEvent(3L);
        event.setTestProperty("bla");
        RemoteEvent wrapEvent = RemoteEventUtil.wrapEvent(event);
        Map<String, String> properties = wrapEvent.getNestedEventProperties();
        assertThat(wrapEvent.getType(), is(TestEvent.class.getName()));
        assertThat(properties.get("processId"), is("3"));
    }

    @Test
    public void testRegisterEvent() throws Exception {
        RemoteEvent reg = new RemoteEvent(TestEvent.class.getSimpleName());
        reg.setProcessId(3L);
        regService.registerEvent(reg, "testPort", URI.create("test://localhost"));
        service.processEvent(new TestEvent());
        verify(outgoingPort).send(eq(URI.create("test://localhost")), any(MethodCall.class));
    }

    @Test
    public void testRegisterEvent_shouldCreateRule() throws Exception {
        RemoteEvent reg = new RemoteEvent(TestEvent.class.getSimpleName());
        int oldCount = manager.list(RuleBaseElementType.Rule).size();
        regService.registerEvent(reg, "testPort", URI.create("test://localhost"));
        assertThat(manager.list(RuleBaseElementType.Rule).size(), is(oldCount + 1));
    }

    @Test
    public void testRegisterEvent_shouldProcessRemoteEvent() throws Exception {
        RemoteEvent reg = new RemoteEvent(TestEvent.class.getSimpleName());
        regService.registerEvent(reg, "testPort", URI.create("test://localhost"), "workflowService");
        String ruleCode = "when RemoteEvent() then example.doSomething(\"it works\");";
        manager.add(new RuleBaseElementId(RuleBaseElementType.Rule, "react to remote-event"), ruleCode);
        service.processEvent(new TestEvent());
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);
        verify(logService).doSomething("it works");
    }
}
