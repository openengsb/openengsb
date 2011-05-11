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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.OpenEngSBService;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.remote.CallRouter;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResult.ReturnType;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.osgi.framework.BundleContext;

public class CallRouterTest extends AbstractOsgiMockServiceTest {

    private CallRouter callrouter;
    private RequestHandlerImpl requestHandler;
    private TestService serviceMock;
    private OutgoingPort outgoingPortMock;
    private final String testURI = "jms://localhost";
    private MethodCallRequest methodCallRequest;

    @Before
    public void setUp() throws Exception {
        serviceMock = mockService(TestService.class, "foo");
        outgoingPortMock = mockService(OutgoingPort.class, "jms+json-out");
        callrouter = new DefaultCallRouter();
        requestHandler = new RequestHandlerImpl();

        Map<String, String> metaData = getMetadata("foo");
        MethodCall call = new MethodCall("test", new Object[0], metaData);
        methodCallRequest = new MethodCallRequest(call, "1");
    }

    @Test
    public void testReceiveAnything() throws Exception {
        callrouter.stop();
        Thread.sleep(300);
    }

    @Test
    public void testRecieveMethodCall_shouldCallService() throws Exception {
        requestHandler.handleCall(methodCallRequest.getMethodCall());
        callrouter.stop();
        verify(serviceMock).test();
    }

    private HashMap<String, String> getMetadata(String id) {
        HashMap<String, String> metaData = new HashMap<String, String>();
        metaData.put("serviceId", id);
        return metaData;
    }

    @Test
    public void testReceiveMethodCallWithArgument() throws Exception {
        MethodCall call2 = new MethodCall("test", new Object[]{ 42 }, getMetadata("foo"));
        requestHandler.handleCall(call2);
        callrouter.stop();
        verify(serviceMock, never()).test();
        verify(serviceMock, times(1)).test(eq(42));
    }

    @Test
    public void recieveMethodCall_shouldSendResponse() throws Exception {
        when(serviceMock.getAnswer()).thenReturn(42);
        MethodCall call2 = new MethodCall("getAnswer", new Object[0], getMetadata("foo"));
        MethodResult result = requestHandler.handleCall(call2);

        verify(serviceMock).getAnswer();
        assertThat((Integer) result.getArg(), is(42));
    }

    @Test
    public void recieveMethodCallWithVoidMethod_shouldSendResponseWithVoidType() throws Exception {
        MethodResult result = requestHandler.handleCall(methodCallRequest.getMethodCall());

        verify(serviceMock).test();
        assertThat(result.getType(), equalTo(ReturnType.Void));
        assertNull(result.getArg());
    }

    @Test
    public void testSendMethodCall_shouldCallPort() throws Exception {

        callrouter.call("jms+json-out", testURI, new MethodCallRequest());
        Thread.sleep(300);
        callrouter.stop();
        verify(outgoingPortMock, times(1)).send(any(MethodCallRequest.class));
    }

    @Test
    public void testSendSyncMethodCall_shouldCallPort() throws Exception {
        callrouter.callSync("jms+json-out", testURI, methodCallRequest);
        verify(outgoingPortMock, times(1)).sendSync(any(MethodCallRequest.class));
    }

    @Test
    public void testSendSyncMethodCall_shouldReturnResult() throws Exception {
        when(serviceMock.getAnswer()).thenReturn(42);
        MethodResultMessage value = new MethodResultMessage();
        when(outgoingPortMock.sendSync(methodCallRequest)).thenReturn(value);
        MethodResultMessage result = callrouter.callSync("jms+json-out", "jms://localhost", methodCallRequest);
        assertThat(result, is(value));
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

    // @Test(timeout = 10000)
    // public void testHandleCallsParallel() throws Exception {
    // when(serviceMock.getAnswer()).thenReturn(42);
    // final Object sync = addWaitingAnswerToServiceMock();
    // MethodCallRequest blockingCall =
    // new MethodCallRequest("getOtherAnswer", new Object[0], getMetadata("foo"), "1", true);
    // MethodCallRequest normalCall = new MethodCallRequest("getAnswer", new Object[0], getMetadata("foo"), "1", true);
    //
    // ExecutorService threadPool = Executors.newCachedThreadPool();
    // Future<MethodResultMessage> blockingFuture = threadPool.submit(new MethodCallable(blockingCall));
    // Future<MethodResultMessage> normalFuture = threadPool.submit(new MethodCallable(normalCall));
    //
    // MethodResultMessage normalResult = normalFuture.get();
    //
    // verify(serviceMock).getAnswer();
    // /* getAnswer-call is finished */
    // assertThat((Integer) normalResult.getArg(), is(42));
    // try {
    // blockingFuture.get(200, TimeUnit.MILLISECONDS);
    // fail("blocking method returned premature");
    // } catch (TimeoutException e) {
    // // ignore, this is expceted
    // }
    //
    // synchronized (sync) {
    // sync.notifyAll();
    // }
    // MethodResultMessage blockingResult = blockingFuture.get();
    // assertThat((Long) blockingResult.getArg(), is(42L));
    // }

    private Object addWaitingAnswerToServiceMock() {
        final Object sync = new Object();
        BlockingAnswer<Long> answer = new BlockingAnswer<Long>(sync) {
            @Override
            public Long realAnswer(InvocationOnMock invocationOnMock) {
                return 42L;
            }
        };
        when(serviceMock.getOtherAnswer()).thenAnswer(answer);
        return sync;
    }

    private abstract class BlockingAnswer<T> implements Answer<T> {
        private final Object sync;

        public BlockingAnswer(Object sync) {
            this.sync = sync;
        }

        @Override
        public T answer(InvocationOnMock invocation) throws Throwable {
            synchronized (sync) {
                sync.wait();
            }
            return realAnswer(invocation);
        }

        public abstract T realAnswer(InvocationOnMock invocationOnMock);
    }

    public interface TestService extends OpenEngSBService {
        void test();

        void test(Integer i);

        Integer getAnswer();

        Long getOtherAnswer();
    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        DefaultOsgiUtilsService osgiServiceUtils = new DefaultOsgiUtilsService();
        osgiServiceUtils.setBundleContext(bundleContext);
        registerService(osgiServiceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
        OpenEngSBCoreServices.setOsgiServiceUtils(osgiServiceUtils);
    }

}
