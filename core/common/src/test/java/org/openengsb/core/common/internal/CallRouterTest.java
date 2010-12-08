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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.OpenEngSBService;
import org.openengsb.core.common.communication.IncomingPort;
import org.openengsb.core.common.communication.MethodCall;
import org.openengsb.core.common.communication.MethodReturn;
import org.openengsb.core.common.communication.MethodReturn.ReturnType;
import org.openengsb.core.common.communication.OutgoingPort;
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
        IncomingPort portMock = createPortMock(new MethodCall("42", "test", new Object[0], null));
        callrouter.registerIncomingPort(portMock);
        callrouter.stop();
        Thread.sleep(300);

        verify(portMock, atLeast(1)).listen(any(UUID.class));
    }

    @Test
    public void testRecieveMethodCall_shouldCallService() throws Exception {
        IncomingPort portMock = createPortMock(new MethodCall("42", "test", new Object[0], null));
        callrouter.registerIncomingPort(portMock);
        Thread.sleep(300);
        callrouter.stop();
        verify(serviceMock, atLeast(1)).test();
    }

    @Test
    public void testReceiveMethodCallWithArgument() throws Exception {
        IncomingPort portMock = createPortMock(new MethodCall("42", "test", new Object[]{ 42 }, null));
        callrouter.registerIncomingPort(portMock);
        Thread.sleep(300);
        callrouter.stop();
        verify(serviceMock, never()).test();
        verify(serviceMock, atLeast(1)).test(eq(42));
    }

    @Test
    public void recieveMethodCall_shouldSendResponse() throws Exception {
        when(serviceMock.getAnswer()).thenReturn(42);
        IncomingPort portMock = createPortMock(new MethodCall("42", "getAnswer", new Object[0], null));
        callrouter.registerIncomingPort(portMock);
        Thread.sleep(300);
        callrouter.stop();

        verify(serviceMock).getAnswer();
        MethodReturn ref = new MethodReturn(ReturnType.Object, 42, null);
        verify(portMock, atLeast(1)).sendResponse(any(UUID.class), eq(ref));
    }

    @Test
    public void testSendMethodCall_shouldCallPort() throws Exception {
        OutgoingPort portMock = mock(OutgoingPort.class);
        callrouter.registerOutgoingPort("jms", portMock);
        callrouter.call("jms", URI.create("jms://localhost"), new MethodCall());
        Thread.sleep(300);
        callrouter.stop();
        verify(portMock, atLeast(1)).send(any(URI.class), any(MethodCall.class));
    }

    @Test
    public void testSendSyncMethodCall_shouldCallPort() throws Exception {
        MethodCall methodCall = new MethodCall("42", "test", new Object[]{ 42 }, null);
        OutgoingPort portMock = mock(OutgoingPort.class);
        callrouter.registerOutgoingPort("jms", portMock);
        callrouter.callSync("jms", URI.create("jms://localhost"), methodCall);
        verify(portMock, atLeast(1)).sendSync(any(URI.class), any(MethodCall.class));
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

    private BundleContext createBundleContextMock() throws InvalidSyntaxException {
        BundleContext bundleContext = mock(BundleContext.class);
        final ServiceReference serviceRefMock = mock(ServiceReference.class);
        when(bundleContext.getServiceReferences(eq(OpenEngSBService.class.getName()), anyString())).thenReturn(
            new ServiceReference[]{ serviceRefMock, });
        serviceMock = mock(TestService.class);
        when(bundleContext.getService(serviceRefMock)).thenReturn(serviceMock);
        return bundleContext;
    }

    private IncomingPort createPortMock(final MethodCall methodCall) {
        final IncomingPort portMock = mock(IncomingPort.class);
        when(portMock.listen(any(UUID.class))).thenAnswer(new Answer<MethodCall>() {
            boolean first = true;

            @Override
            public MethodCall answer(InvocationOnMock invocation) throws Throwable {
                if (!first) {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        // ignore. this happens all the time.
                    }
                    MethodCall dummyResult = new MethodCall();
                    dummyResult.setServiceId(methodCall.getServiceId());
                    dummyResult.setArgs(new Object[0]);
                    dummyResult.setMethodName("getClass");
                    return dummyResult;
                }
                first = false;
                return methodCall;
            }
        });
        return portMock;
    }
}
