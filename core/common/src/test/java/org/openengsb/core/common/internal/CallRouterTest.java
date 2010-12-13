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

package org.openengsb.core.common.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.OpenEngSBService;
import org.openengsb.core.common.communication.IncomingPort;
import org.openengsb.core.common.communication.MethodCall;
import org.openengsb.core.common.communication.MethodReturn;
import org.openengsb.core.common.communication.OutgoingPort;
import org.openengsb.core.common.communication.RequestHandler;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class CallRouterTest {

    private TestService serviceMock;
    private CallRouterImpl callrouter;

    @Before
    public void setUp() throws Exception {
        callrouter = new CallRouterImpl();
        BundleContext bundleContext = createBundleContextMock();
        callrouter.setBundleContext(bundleContext);
    }

    @Test
    public void testReceiveAnything() throws Exception {
        IncomingPort portMock = mock(IncomingPort.class);
        callrouter.registerIncomingPort(portMock);
        callrouter.stop();
        Thread.sleep(300);

        verify(portMock).setRequestHandler(any(RequestHandler.class));
    }

    @Test
    public void testRecieveMethodCall_shouldCallService() throws Exception {
        final MethodCall call = new MethodCall("42", "test", new Object[0], new HashMap<String, String>());
        callrouter.handleCall(call);
        callrouter.stop();
        verify(serviceMock, times(1)).test();
    }

    @Test
    public void testReceiveMethodCallWithArgument() throws Exception {
        final MethodCall call = new MethodCall("42", "test", new Object[]{ 42 }, new HashMap<String, String>());
        callrouter.handleCall(call);
        callrouter.stop();
        verify(serviceMock, never()).test();
        verify(serviceMock, times(1)).test(eq(42));
    }

    @Test
    public void recieveMethodCall_shouldSendResponse() throws Exception {
        when(serviceMock.getAnswer()).thenReturn(42);
        final MethodCall call = new MethodCall("42", "getAnswer", new Object[0], new HashMap<String, String>());
        MethodReturn result = callrouter.handleCall(call);

        verify(serviceMock).getAnswer();
        assertThat((Integer) result.getArg(), is(42));
    }

    @Test
    public void testSendMethodCall_shouldCallPort() throws Exception {
        OutgoingPort portMock = mock(OutgoingPort.class);
        callrouter.registerOutgoingPort("jms", portMock);
        final URI testURI = URI.create("jms://localhost");
        callrouter.call("jms", testURI, new MethodCall());
        Thread.sleep(300);
        callrouter.stop();
        verify(portMock, times(1)).send(eq(testURI), any(MethodCall.class));
    }

    @Test
    public void testSendSyncMethodCall_shouldCallPort() throws Exception {
        MethodCall methodCall = new MethodCall("42", "test", new Object[]{ 42 }, new HashMap<String, String>());
        OutgoingPort portMock = mock(OutgoingPort.class);
        callrouter.registerOutgoingPort("jms", portMock);
        final URI testURI = URI.create("jms://localhost");
        callrouter.callSync("jms", testURI, methodCall);
        verify(portMock, times(1)).sendSync(eq(testURI), any(MethodCall.class));
    }

    @Test
    public void testSendSyncMethodCall_shouldReturnResult() throws Exception {
        when(serviceMock.getAnswer()).thenReturn(42);
        MethodCall methodCall = new MethodCall("42", "test", new Object[]{ 42 }, null);
        OutgoingPort portMock = mock(OutgoingPort.class);
        MethodReturn value = new MethodReturn();
        when(portMock.sendSync(URI.create("jms://localhost"), methodCall)).thenReturn(value);
        callrouter.registerOutgoingPort("jms", portMock);
        MethodReturn result = callrouter.callSync("jms", URI.create("jms://localhost"), methodCall);
        assertThat(result, is(value));
    }

    private class MethodCallable implements Callable<MethodReturn> {
        private MethodCall call;

        public MethodCallable(MethodCall call) {
            this.call = call;
        }

        @Override
        public MethodReturn call() throws Exception {
            return callrouter.handleCall(call);
        }
    }

    @Test(timeout = 10000)
    public void testHandleCallsParallel() throws Exception {
        when(serviceMock.getAnswer()).thenReturn(42);
        final Object sync = addWaitingAnswerToServiceMock();
        MethodCall blockingCall = new MethodCall("42", "getOtherAnswer", new Object[0], null);
        MethodCall normalCall = new MethodCall("42", "getAnswer", new Object[0], null);

        ExecutorService threadPool = Executors.newCachedThreadPool();
        Future<MethodReturn> blockingFuture = threadPool.submit(new MethodCallable(blockingCall));
        Future<MethodReturn> normalFuture = threadPool.submit(new MethodCallable(normalCall));

        MethodReturn normalResult = normalFuture.get();

        verify(serviceMock).getAnswer();
        /* getAnswer-call is finished */
        assertThat((Integer) normalResult.getArg(), is(42));
        try {
            blockingFuture.get(200, TimeUnit.MILLISECONDS);
            fail("blocking method returned premature");
        } catch (TimeoutException e) {
            // ignore, this is expceted
        }

        synchronized (sync) {
            sync.notifyAll();
        }
        MethodReturn blockingResult = blockingFuture.get();
        assertThat((Long) blockingResult.getArg(), is(42L));
    }

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
        private Object sync;

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

    private BundleContext createBundleContextMock() throws InvalidSyntaxException {
        BundleContext bundleContext = mock(BundleContext.class);
        final ServiceReference serviceRefMock = mock(ServiceReference.class);
        when(bundleContext.getServiceReferences(eq(OpenEngSBService.class.getName()), anyString())).thenReturn(
            new ServiceReference[]{ serviceRefMock, });
        serviceMock = mock(TestService.class);
        when(bundleContext.getService(serviceRefMock)).thenReturn(serviceMock);
        return bundleContext;
    }

}
