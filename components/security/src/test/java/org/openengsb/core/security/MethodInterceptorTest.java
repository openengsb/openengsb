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

package org.openengsb.core.security;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.security.service.AccessDeniedException;
import org.openengsb.core.security.internal.RootSecurityHolder;
import org.openengsb.core.security.internal.SecurityInterceptor;
import org.openengsb.core.test.AbstractOpenEngSBTest;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.openengsb.domain.authorization.AuthorizationDomain.Access;
import org.springframework.aop.framework.ProxyFactory;

public class MethodInterceptorTest extends AbstractOpenEngSBTest {

    private static final String DEFAULT_USER = "foo";
    private SecurityInterceptor interceptor;
    private DummyService service;
    private DummyService service2;
    private AuthorizationDomain authorizer;

    @Before
    public void setUp() throws Exception {
        RootSecurityHolder.init();
        DefaultSecurityManager sm = new DefaultSecurityManager();
        sm.setAuthenticator(new Authenticator() {
            @Override
            public AuthenticationInfo authenticate(AuthenticationToken authenticationToken)
                throws AuthenticationException {
                return new SimpleAuthenticationInfo(new SimplePrincipalCollection(authenticationToken.getPrincipal(),
                    "openengsb"), authenticationToken.getCredentials());
            }
        });
        SecurityUtils.setSecurityManager(sm);
        ThreadContext.bind(sm);

        interceptor = new SecurityInterceptor();
        authorizer = mock(AuthorizationDomain.class);
        when(authorizer.checkAccess(eq("admin"), any(MethodInvocation.class))).thenReturn(Access.GRANTED);

        interceptor.setAuthorizer(authorizer);

        service = (DummyService) secure(new DummyServiceImpl("42"));
        service2 = (DummyService) secure(new DummyServiceImpl("21"));
    }

    private Object secure(Object o) {
        ProxyFactory factory = new ProxyFactory(o);
        factory.addAdvice(interceptor);
        return factory.getProxy();
    }

    @Test(expected = AccessDeniedException.class)
    public void testInvokeMethodOnWrongServiceInstance() throws Exception {
        authenticate(DEFAULT_USER, "password");
        service2.getTheAnswerToLifeTheUniverseAndEverything();
    }

    @Test
    public void testAdminAccess() throws Exception {
        authenticate("admin", "adminpw");
        service2.getTheAnswerToLifeTheUniverseAndEverything();
        service.getTheAnswerToLifeTheUniverseAndEverything();
    }

    @Test
    public void testInvokeMethodAsRoot() throws Exception {
        authenticate(DEFAULT_USER, "password");
        RootSecurityHolder.getRootSubject().execute(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                service2.getTheAnswerToLifeTheUniverseAndEverything();
                return null;
            }
        });
    }

    private void authenticate(String user, String password) {
        Subject subject = SecurityUtils.getSubject();
        subject.login(new UsernamePasswordToken(user, password));
        System.out.println(subject.isAuthenticated());
    }
}
