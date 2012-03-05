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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.api.persistence.PersistenceManager;
import org.openengsb.core.api.persistence.PersistenceService;
import org.openengsb.core.api.security.model.ServiceAuthorizedList;
import org.openengsb.core.api.security.model.User;
import org.openengsb.core.persistence.internal.DefaultPersistenceManager;
import org.openengsb.core.security.internal.MetadataSource;
import org.openengsb.core.security.internal.UserManagerImpl;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;

public class MethodInterceptorTest {

    private static final String DEFAULT_USER = "foo";
    private MethodSecurityInterceptor interceptor;
    private ProviderManager authenticationManager;
    private PersistenceService persistence;
    private DummyService service;
    private DummyService service2;
    private PersistenceManager persistenceManager;
    private BundleContext bundleContextMock;

    @Before
    public void setUp() throws Exception {
        initPersistence();
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

    private void initPersistence() throws PersistenceException {
        persistenceManager = new DefaultPersistenceManager();
        ((DefaultPersistenceManager) persistenceManager)
            .setPersistenceRootDir("target/" + UUID.randomUUID().toString());
        bundleContextMock = mock(BundleContext.class);
        Bundle bundleMock = mock(Bundle.class);
        when(bundleContextMock.getBundle()).thenReturn(bundleMock);
        when(bundleMock.getSymbolicName()).thenReturn(UUID.randomUUID().toString());
        persistence = persistenceManager.getPersistenceForBundle(bundleMock);

        List<GrantedAuthority> authorities =
            Arrays.asList(new GrantedAuthority[]{ new GrantedAuthorityImpl("ROLE_USER") });
        persistence.create(new ServiceAuthorizedList("42", authorities));

        User user = new User(DEFAULT_USER, "password", authorities);
        persistence.create(user);

        List<GrantedAuthority> adminAuthorities =
            Arrays.asList(new GrantedAuthority[]{ new GrantedAuthorityImpl("ROLE_ADMIN") });
        User admin = new User("admin", "adminpw", adminAuthorities);
        persistence.create(admin);
    }

    private List<AccessDecisionVoter> makeVoterList() {
        List<AccessDecisionVoter> result = new ArrayList<AccessDecisionVoter>();
        result.add(makeVoter());
        return result;
    }

    private AccessDecisionVoter makeVoter() {
        AuthenticatedUserAccessDecisionVoter voter = new AuthenticatedUserAccessDecisionVoter();
        voter.setPersistenceManager(persistenceManager);
        voter.setBundleContext(bundleContextMock);
        voter.init();
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
        userDetailsService.setPersistenceManager(persistenceManager);
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
        authenticate(DEFAULT_USER, "wrong");
    }

    @Test
    public void testInvokeMethod() throws Exception {
        authenticate(DEFAULT_USER, "password");
        // just invoke the method, and avoid failing
        service.getTheAnswerToLifeTheUniverseAndEverything();
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
    public void testAccessAnnotatedMethod() throws Exception {
        authenticate(DEFAULT_USER, "password");
        service2.publicTest();
    }

    private void authenticate(String user, String password) {
        Authentication authentication =
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
