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

package org.openengsb.core.services.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.OpenEngSBService;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallMessage;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResult.ReturnType;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.api.remote.OutgoingPortUtilService;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;

public class OutgoingPortUtilServiceTest extends AbstractOsgiMockServiceTest {

    private OutgoingPortUtilService callrouter;
    private RequestHandlerImpl requestHandler;
    private TestService serviceMock;
    private OutgoingPort outgoingPortMock;
    private final String testURI = "jms://localhost";
    private MethodCall methodCall;

    @Before
    public void setUp() throws Exception {
        serviceMock = mockService(TestService.class, "foo");
        outgoingPortMock = mockService(OutgoingPort.class, "jms+json-out");
        callrouter = new DefaultOutgoingPortUtilService(new DefaultOsgiUtilsService(bundleContext));
        requestHandler = new RequestHandlerImpl();
        requestHandler.setUtilsService(new DefaultOsgiUtilsService(bundleContext));

        Map<String, String> metaData = getMetadata("foo");
        methodCall = new MethodCall("test", new Object[0], metaData);
    }

    @Test
    public void testRecieveMethodCall_shouldCallService() {
        requestHandler.handleCall(methodCall);
        verify(serviceMock).test();
    }

    private HashMap<String, String> getMetadata(String id) {
        HashMap<String, String> metaData = new HashMap<String, String>();
        metaData.put("serviceId", id);
        return metaData;
    }

    @Test
    public void testReceiveMethodCallWithArgument_shouldCallMethod() {
        MethodCall call2 = new MethodCall("test", new Object[]{ 42 }, getMetadata("foo"));
        requestHandler.handleCall(call2);
        verify(serviceMock, never()).test();
        verify(serviceMock, times(1)).test(eq(42));
    }

    @Test
    public void testRecieveMethodCall_shouldSendResponse() {
        when(serviceMock.getAnswer()).thenReturn(42);
        MethodCall call2 = new MethodCall("getAnswer", new Object[0], getMetadata("foo"));
        MethodResult result = requestHandler.handleCall(call2);

        verify(serviceMock).getAnswer();
        assertThat((Integer) result.getArg(), is(42));
    }

    @Test
    public void testReceiveMethodCallWithVoidMethod_shouldSendResponseWithVoidType() {
        MethodResult result = requestHandler.handleCall(methodCall);

        verify(serviceMock).test();
        assertThat(result.getType(), equalTo(ReturnType.Void));
        assertNull(result.getArg());
    }

    @Test
    public void testSendMethodCall_shouldCallPort() throws Exception {
        callrouter.sendMethodCall("jms+json-out", testURI, new MethodCall());
        Thread.sleep(300);
        verify(outgoingPortMock, times(1)).send(any(MethodCallMessage.class));
    }

    @Test
    public void testSendSyncMethodCall_shouldReturnResult() {
        when(serviceMock.getAnswer()).thenReturn(42);
        MethodResult expectedResult = new MethodResult();
        MethodResultMessage value = mock(MethodResultMessage.class);
        when(value.getResult()).thenReturn(expectedResult);
        when(outgoingPortMock.sendSync(any(MethodCallMessage.class))).thenReturn(value);
        MethodResult result = callrouter.sendMethodCallWithResult("jms+json-out", "jms://localhost", methodCall);
        assertThat(result, is(expectedResult));
    }

    private class MethodCallable implements Callable<MethodResult> {
        private final MethodCall call;

        public MethodCallable(MethodCall call) {
            this.call = call;
        }

        @Override
        public MethodResult call() throws Exception {
            return requestHandler.handleCall(call);
        }
    }

    @Test(timeout = 10000)
    public void testHandleCallsParallel_shouldWork() throws Exception {
        when(serviceMock.getAnswer()).thenReturn(42);
        final Semaphore sync = new Semaphore(0);
        Answer<Long> answer = new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocationOnMock) {
                try {
                    sync.acquire();
                } catch (InterruptedException e) {
                    fail(e.toString());
                }
                return 42L;
            }
        };
        when(serviceMock.getOtherAnswer()).thenAnswer(answer);
        MethodCall blockingCall = new MethodCall("getOtherAnswer", new Object[0], getMetadata("foo"));
        MethodCall normalCall = new MethodCall("getAnswer", new Object[0], getMetadata("foo"));

        ExecutorService threadPool = Executors.newCachedThreadPool();
        Future<MethodResult> blockingFuture = threadPool.submit(new MethodCallable(blockingCall));
        Future<MethodResult> normalFuture = threadPool.submit(new MethodCallable(normalCall));

        MethodResult normalResult = normalFuture.get();

        verify(serviceMock).getAnswer();
        /* getAnswer-call is finished */
        assertThat((Integer) normalResult.getArg(), is(42));
        try {
            blockingFuture.get(200, TimeUnit.MILLISECONDS);
            fail("blocking method returned premature");
        } catch (TimeoutException e) {
            // ignore, this is expceted
        }

        sync.release();
        MethodResult blockingResult = blockingFuture.get();
        assertThat((Long) blockingResult.getArg(), is(42L));
    }

    public interface TestService extends OpenEngSBService {
        void test();

        void test(Integer i);

        Integer getAnswer();

        Long getOtherAnswer();
    }

}
