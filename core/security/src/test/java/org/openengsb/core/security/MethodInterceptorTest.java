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

package org.openengsb.core.security;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceService;
import org.openengsb.core.security.model.User;
import org.openengsb.core.security.usermanagement.UserManagerImpl;
import org.openengsb.core.test.DummyPersistenceManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.intercept.aopalliance.MethodSecurityInterceptor;
import org.springframework.security.access.method.MethodSecurityMetadataSource;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import edu.emory.mathcs.backport.java.util.Arrays;

public class MethodInterceptorTest {

    private static final String DEFAULT_USER = "foo";
    private MethodSecurityInterceptor interceptor;
    private Bundle bundleMock;
    private ProviderManager authenticationManager;
    private PersistenceService persistence;
    private DummyService service;
    private DummyService service2;

    @Before
    public void setUp() throws Exception {
        authenticationManager = initAuthenticationManager();
        interceptor = new MethodSecurityInterceptor();
        MethodSecurityMetadataSource metadataSource = new MetadataSource();
        interceptor.setSecurityMetadataSource(metadataSource);
        interceptor.setRejectPublicInvocations(true);
        interceptor.setAuthenticationManager(authenticationManager);

        final AffirmativeBased accessDecisionManager = new AffirmativeBased();
        accessDecisionManager.setDecisionVoters(makeVoterList());
        interceptor.setAccessDecisionManager(accessDecisionManager);
        service = (DummyService) secure(new DummyServiceImpl("42"));
        service2 = (DummyService) secure(new DummyServiceImpl("21"));
    }

    private List<AccessDecisionVoter> makeVoterList() {
        List<AccessDecisionVoter> result = new ArrayList<AccessDecisionVoter>();
        result.add(makeVoter());
        return result;
    }

    private AccessDecisionVoter makeVoter() {
        AccessDecisionVoter voter = new AuthenticatedUserAccessDecisionVoter();
        return voter;
    }

    private ProviderManager initAuthenticationManager() throws PersistenceException {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();

        final UserManagerImpl userDetailsService = createUserDetailsService();
        p.setUserDetailsService(userDetailsService);

        ProviderManager authenticationManager = new ProviderManager();
        authenticationManager.setProviders(Arrays.asList(new Object[]{ p }));
        return authenticationManager;
    }

    private UserManagerImpl createUserDetailsService() throws PersistenceException {
        final UserManagerImpl userDetailsService = new UserManagerImpl();
        final DummyPersistenceManager persistenceManager = new DummyPersistenceManager();
        userDetailsService.setPersistenceManager(persistenceManager);
        BundleContext bundleContextMock = mock(BundleContext.class);
        bundleMock = mock(Bundle.class);
        when(bundleContextMock.getBundle()).thenReturn(bundleMock);
        persistence = persistenceManager.getPersistenceForBundle(bundleMock);
        persistence.create(new User(DEFAULT_USER, "password"));
        userDetailsService.setBundleContext(bundleContextMock);
        userDetailsService.init();
        return userDetailsService;
    }

    private Object secure(Object o) {
        ProxyFactory factory = new ProxyFactory(o);
        factory.addAdvice(interceptor);
        return factory.getProxy();
    }

    @Test
    public void testAuthenticate() {
        Authentication authentication =
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(DEFAULT_USER, "password"));
        assertThat(authentication.isAuthenticated(), is(true));
    }

    @Test(expected = BadCredentialsException.class)
    public void testFalseAuthenticate() {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(DEFAULT_USER, "wrong"));
    }

    @Test
    public void testInvokeMethod() throws Exception {
        Authentication authentication =
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(DEFAULT_USER, "password"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        // just invoke the method, and avoid failing
        service.getTheAnswerToLifeTheUniverseAndEverything();
    }

    @Test(expected = AccessDeniedException.class)
    public void testInvokeMethodOnWrongServiceInstance() throws Exception {
        Authentication authentication =
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(DEFAULT_USER, "password"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        service2.getTheAnswerToLifeTheUniverseAndEverything();
    }
}
