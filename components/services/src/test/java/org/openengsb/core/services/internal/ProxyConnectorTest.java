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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Proxy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResult.ReturnType;
import org.openengsb.core.api.remote.OutgoingPortUtilService;
import org.openengsb.core.services.internal.virtual.ProxyConnector;
import org.openengsb.core.test.AbstractOpenEngSBTest;

public class ProxyConnectorTest extends AbstractOpenEngSBTest {

    private OutgoingPortUtilService router;
    private ProxyConnector proxy;

    @Before
    public void setUp() {
        router = mock(OutgoingPortUtilService.class);
        proxy = new ProxyConnector("foo", null);
        proxy.setOutgoingPortUtilService(router);
        proxy.setPortId("id");
        proxy.setDestination("test");
        proxy.addMetadata("key", "value");
    }

    @Test
    public void testCallInvoke_shouldCreateMethodCallAndReturnResult() throws Exception {
        ArgumentCaptor<MethodCall> captor = ArgumentCaptor.forClass(MethodCall.class);
        MethodResult result2 = new MethodResult("id");
        when(router.sendMethodCallWithResult(Mockito.eq("id"), Mockito.eq("test"), captor.capture())).thenReturn(
            result2);

        Object[] args = new Object[]{ "id", "test" };
        Interface newProxyInstance =
            (Interface) Proxy.newProxyInstance(Interface.class.getClassLoader(), new Class[]{ Interface.class }, proxy);
        String result = newProxyInstance.test("id", "test");

        MethodCall value = captor.getValue();
        assertThat(value.getMethodName(), equalTo("test"));
        assertThat(value.getArgs(), equalTo(args));
        assertThat(value.getMetaData().size(), equalTo(1));
        assertThat(value.getMetaData().get("key"), equalTo("value"));
        assertThat(value.getClasses().size(), equalTo(2));
        assertThat(result, equalTo("id"));
    }

    @Test
    public void callInvokeWithException_ShouldThrowException() throws Exception {
        String message = "Message";
        MethodResult result = new MethodResult(message, ReturnType.Exception);
        when(router.sendMethodCallWithResult(any(String.class), any(String.class), any(MethodCall.class)))
            .thenReturn(result);
        Interface newProxyInstance =
            (Interface) Proxy.newProxyInstance(Interface.class.getClassLoader(), new Class[]{ Interface.class }, proxy);
        try {
            newProxyInstance.testException();
            fail();
        } catch (RuntimeException e) {
            assertThat(e.getMessage(), equalTo(message));
        }
    }

    private interface Interface {
        String test(String id, String uri);

        void testException();
    }
}
