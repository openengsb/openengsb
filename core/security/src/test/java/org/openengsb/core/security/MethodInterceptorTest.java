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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openengsb.core.api.persistence.PersistenceException;
import org.openengsb.core.common.util.Users;
import org.openengsb.core.security.internal.MetadataSource;
import org.openengsb.core.security.model.Permission;
import org.openengsb.core.security.model.PermissionAuthority;
import org.openengsb.core.security.model.Role;
import org.openengsb.core.security.model.RoleAuthority;
import org.openengsb.core.security.model.ServicePermission;
import org.openengsb.core.test.AbstractOpenEngSBTest;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.google.common.collect.Sets;

public class MethodInterceptorTest extends AbstractOpenEngSBTest {

    private static final String DEFAULT_USER = "foo";
    private MethodSecurityInterceptor interceptor;
    private ProviderManager authenticationManager;
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
        result.add(new AdminRoleVoter());
        result.add(new AnnotationRoleVoter());
        result.add(new OpenEngSBAccessDecisionVoter());
        return result;
    }

    private ProviderManager initAuthenticationManager() throws PersistenceException {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();

        final UserDetailsService userDetailsService = createUserDetailsService();
        p.setUserDetailsService(userDetailsService);

        ProviderManager authenticationManager = new ProviderManager();
        authenticationManager.setProviders(Arrays.asList(new Object[]{ p }));
        return authenticationManager;
    }

    private UserDetailsService createUserDetailsService() throws PersistenceException {
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        UserDetails user =
            Users.create(DEFAULT_USER, "password",
                Arrays.asList((GrantedAuthority) new GrantedAuthorityImpl("ROLE_USER")));
        when(userDetailsService.loadUserByUsername(DEFAULT_USER)).thenReturn(user);

        List<GrantedAuthority> adminAuthorities =
            Arrays.asList((GrantedAuthority) new GrantedAuthorityImpl("ROLE_ADMIN"));
        User admin = Users.create("admin", "adminpw", adminAuthorities);
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(admin);

        Permission servicePermission = new ServicePermission("42");
        UserDetails testuser = Users.create("serviceuser", "password", new PermissionAuthority(servicePermission));
        when(userDetailsService.loadUserByUsername("serviceuser")).thenReturn(testuser);

        Role role = new Role("serviceUsers", Sets.newHashSet(servicePermission));

        UserDetails roleUser = Users.create("roleuser", "password", new RoleAuthority(role));
        when(userDetailsService.loadUserByUsername("roleuser")).thenReturn(roleUser);

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

    @Ignore("implement proper permissions first")
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

    @Test
    public void testServicePermission_shouldGrantAccess() throws Exception {
        authenticate("serviceuser", "password");
        service.getTheAnswerToLifeTheUniverseAndEverything();
    }

    @Test(expected = AccessDeniedException.class)
    public void testServicePermission_shouldDenyAccess() throws Exception {
        authenticate("serviceuser", "password");
        service2.getTheAnswerToLifeTheUniverseAndEverything();
    }

    @Test
    public void testRolePermission_shouldGrantAccess() throws Exception {
        authenticate("roleuser", "password");
        service.getTheAnswerToLifeTheUniverseAndEverything();
    }

    @Test(expected = AccessDeniedException.class)
    public void testRolePermission_shouldDenyAccess() throws Exception {
        authenticate("roleuser", "password");
        service2.getTheAnswerToLifeTheUniverseAndEverything();
    }

    private void authenticate(String user, String password) {
        Authentication authentication =
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

}
