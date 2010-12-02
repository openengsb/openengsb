package org.openengsb.core.security;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
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
import org.springframework.security.access.ConfigAttribute;
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

    private ServiceCallInterceptor interceptor;
    private Bundle bundleMock;
    private ProviderManager authenticationManager;
    private PersistenceService persistence;
    private DummyService service;

    @Before
    public void setUp() throws Exception {
        authenticationManager = initAuthenticationManager();

        interceptor = new ServiceCallInterceptor();
        MethodSecurityMetadataSource metadataSource = mock(MethodSecurityMetadataSource.class);
        interceptor.setSecurityMetadataSource(metadataSource);
        interceptor.setAuthenticationManager(authenticationManager);

        final AffirmativeBased accessDecisionManager = new AffirmativeBased();
        accessDecisionManager.setDecisionVoters(makeVoterList());
        interceptor.setAccessDecisionManager(accessDecisionManager);
        service = (DummyService) secure(new DummyServiceImpl("42"));
    }

    private List<AccessDecisionVoter> makeVoterList() {
        List<AccessDecisionVoter> result = new ArrayList<AccessDecisionVoter>();
        result.add(makeVoter());
        return result;
    }

    private AccessDecisionVoter makeVoter() {
        AccessDecisionVoter voter = new AccessDecisionVoter() {
            @Override
            public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
                return ACCESS_GRANTED;
            }

            @Override
            public boolean supports(Class<?> clazz) {
                return true;
            }

            @Override
            public boolean supports(ConfigAttribute attribute) {
                return true;
            }
        };
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
        persistence.create(new User("user", "password"));
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
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("user", "password"));
        assertThat(authentication.isAuthenticated(), is(true));
    }

    @Test
    public void testInvokeMethod() throws Exception {
        Authentication authentication =
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("user", "password"));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        service.getTheAnswerToLifeTheUniverseAndEverything();
    }

    @Test(expected = BadCredentialsException.class)
    public void testApp() {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("user", "wrong"));
    }
}
