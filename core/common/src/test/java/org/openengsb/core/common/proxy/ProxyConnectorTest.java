package org.openengsb.core.common.proxy;

import static junit.framework.Assert.fail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Proxy;
import java.util.HashMap;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openengsb.core.common.communication.CallRouter;
import org.openengsb.core.common.communication.MethodCall;
import org.openengsb.core.common.communication.MethodReturn;
import org.openengsb.core.common.communication.MethodReturn.ReturnType;

public class ProxyConnectorTest {

    @Test
    public void callInvoke_shouldCreateMethodCallAndReturnResult() throws Throwable {
        CallRouter router = mock(CallRouter.class);
        ProxyConnector proxy = new ProxyConnector();
        proxy.setCallRouter(router);
        String id = "id";
        String test = "test";

        proxy.setPortId(id);
        proxy.setDestination(test);

        proxy.addMetadata("key", "value");
        ArgumentCaptor<MethodCall> captor = ArgumentCaptor.forClass(MethodCall.class);
        MethodReturn methodReturn = new MethodReturn(ReturnType.Object, id, new HashMap<String, String>());
        when(router.callSync(Mockito.eq(id), Mockito.eq(test), captor.capture())).thenReturn(methodReturn);

        Object[] args = new Object[]{id, test};
        Interface newProxyInstance =
            (Interface) Proxy.newProxyInstance(Interface.class.getClassLoader(), new Class[]{Interface.class}, proxy);
        String result = newProxyInstance.test(id, test);

        MethodCall value = captor.getValue();
        assertThat(value.getMethodName(), equalTo(test));
        assertThat(value.getArgs(), equalTo(args));
        assertThat(value.getMetaData().size(), equalTo(1));
        assertThat(value.getMetaData().get("key"), equalTo("value"));
        assertThat(value.getClasses().size(), equalTo(2));
        assertThat(result, equalTo(id));
    }

    @Test
    public void callInvokeWithException_ShouldThrowException() {
        CallRouter router = mock(CallRouter.class);
        ProxyConnector proxy = new ProxyConnector();
        proxy.setCallRouter(router);
        String message = "Message";
        MethodReturn methodReturn = new MethodReturn(ReturnType.Exception, message, new HashMap<String, String>());
        when(router.callSync(any(String.class), any(String.class), any(MethodCall.class))).thenReturn(methodReturn);
        Interface newProxyInstance =
            (Interface) Proxy.newProxyInstance(Interface.class.getClassLoader(), new Class[]{Interface.class}, proxy);
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
