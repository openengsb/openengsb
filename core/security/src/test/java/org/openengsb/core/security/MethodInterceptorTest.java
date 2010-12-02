package org.openengsb.core.security;

import org.aopalliance.intercept.MethodInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.aop.framework.ProxyFactory;

public class MethodInterceptorTest {

    private MethodInterceptor interceptor;

    @Before
    public void setUp() {
        interceptor = new ServiceCallInterceptor();
    }

    private Object secure(Object o) {
        ProxyFactory factory = new ProxyFactory(o);
        factory.addAdvice(interceptor);
        return factory.getProxy();
    }

    @Test
    public void testApp() {
        DummyService service = (DummyService) secure(new DummyServiceImpl());
        service.getTheAnswerToLifeTheUniverseAndEverything();
    }
}
