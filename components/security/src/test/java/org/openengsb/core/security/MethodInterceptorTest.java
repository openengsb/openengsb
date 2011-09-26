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

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.security.SpecialAccessControlHandler;
import org.openengsb.core.api.security.service.AccessDeniedException;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.security.internal.SecurityInterceptor;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.openengsb.domain.authorization.AuthorizationDomain.Access;
import org.osgi.framework.BundleContext;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class MethodInterceptorTest extends AbstractOsgiMockServiceTest {

    private static final String DEFAULT_USER = "foo";
    private SecurityInterceptor interceptor;
    private DummyService service;
    private DummyService service2;
    private AuthorizationDomain authorizer;

    @Before
    public void setUp() throws Exception {
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
    public void testSpecialHandler_shouldGrant() throws Exception {
        authenticate(DEFAULT_USER, "password");
        when(authorizer.checkAccess(eq("admin"), any(MethodInvocation.class))).thenReturn(Access.ABSTAINED);
        SpecialAccessControlHandler handler = createParamAccessHandler("x");
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("controlHandler.id", "special-test");
        registerService(handler, props, SpecialAccessControlHandler.class);

        service.specialMethod("x");
    }

    @Test(expected = AccessDeniedException.class)
    public void testSpecialHandler_shouldDeny() throws Exception {
        authenticate(DEFAULT_USER, "password");
        when(authorizer.checkAccess(eq("admin"), any(MethodInvocation.class))).thenReturn(Access.ABSTAINED);
        SpecialAccessControlHandler handler = createParamAccessHandler("y");
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put("controlHandler.id", "special-test");
        registerService(handler, props, SpecialAccessControlHandler.class);

        service.specialMethod("x");
    }

    private SpecialAccessControlHandler createParamAccessHandler(final String... allowed) {
        return new SpecialAccessControlHandler() {
            @Override
            public boolean isAuthorized(String user, MethodInvocation invocation) {
                String arg = (String) invocation.getArguments()[0];
                return ArrayUtils.contains(allowed, arg);
            }
        };
    }

    private void authenticate(String user, String password) {
        Authentication authentication =
            new UsernamePasswordAuthenticationToken(user, password, new ArrayList<GrantedAuthority>());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        DefaultOsgiUtilsService osgiServiceUtils = new DefaultOsgiUtilsService();
        osgiServiceUtils.setBundleContext(bundleContext);
        registerService(osgiServiceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
        OpenEngSBCoreServices.setOsgiServiceUtils(osgiServiceUtils);
    }
}
