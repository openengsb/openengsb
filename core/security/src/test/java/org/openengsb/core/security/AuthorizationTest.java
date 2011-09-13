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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aopalliance.intercept.MethodInvocation;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.common.util.Users;
import org.openengsb.core.security.model.AbstractPermission;
import org.openengsb.core.security.model.PermissionAuthority;
import org.openengsb.core.security.model.RoleAuthority;
import org.openengsb.core.security.model.RoleImpl;
import org.openengsb.core.security.model.ServicePermission;
import org.openengsb.core.test.AbstractOpenEngSBTest;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.util.MethodInvocationUtils;

import com.google.common.collect.Lists;

public class AuthorizationTest extends AbstractOpenEngSBTest {

    private AccessDecisionManager acManager;
    private AuthenticationProvider authProvider;
    private MethodInvocation invocation;
    private Map<String, UserDetails> userData = new HashMap<String, UserDetails>();

    @Before
    public void setup() throws Exception {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        createAuthenticationProvider();
        createAuthorizationManager();

        DummyService dummy = new DummyServiceImpl("a");
        invocation = MethodInvocationUtils.create(dummy, "test");
    }

    private void createAuthorizationManager() {
        List<AccessDecisionVoter> voters = new LinkedList<AccessDecisionVoter>();
        voters.add(new OpenEngSBAccessDecisionVoter());
        voters.add(new AuthorizedRoleAnnotationVoter());
        voters.add(new PublicAnnotationVoter());
        AffirmativeBased affirmativeBased = new AffirmativeBased();
        affirmativeBased.setDecisionVoters(voters);
        acManager = affirmativeBased;
    }

    private void createAuthenticationProvider() {
        UserDetailsService userDetailsService = mock(UserDetailsService.class);
        when(userDetailsService.loadUserByUsername(anyString())).thenAnswer(new Answer<UserDetails>() {
            @Override
            public UserDetails answer(InvocationOnMock invocation) throws Throwable {
                String arg = (String) invocation.getArguments()[0];
                return userData.get(arg);
            }
        });

        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        authProvider = daoAuthenticationProvider;
    }

    @Test
    public void testControlServicePermission_shouldGrant() throws Exception {
        GrantedAuthority authority = new PermissionAuthority(new ServicePermission("a"));
        registerUser(Users.create("admin", "password", Lists.newArrayList(authority)));

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken("admin", "password");
        Authentication successToken = authProvider.authenticate(authToken);
        acManager.decide(successToken, invocation, null);
    }

    @Test(expected = AccessDeniedException.class)
    public void testControlServicePermission_shouldDenyAccess() throws Exception {
        registerUser(Users.create("user", "password"));
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken("user", "password");
        Authentication successToken = authProvider.authenticate(authToken);
        acManager.decide(successToken, invocation, null);
    }

    @Test
    public void testControlViaRole_shouldGrant() throws Exception {
        AbstractPermission servicePermission = new ServicePermission("a");
        RoleImpl role = new RoleImpl("admins", Lists.newArrayList(servicePermission));
        GrantedAuthority roleAuthority = new RoleAuthority(role);
        registerUser(Users.create("admin", "password", Lists.newArrayList(roleAuthority)));

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken("admin", "password");
        Authentication successToken = authProvider.authenticate(authToken);
        acManager.decide(successToken, invocation, null);
    }

    @Test(expected = AccessDeniedException.class)
    public void testControlViaRole_shouldDenyAccess() throws Exception {
        AbstractPermission servicePermission = new ServicePermission("b");
        RoleImpl role = new RoleImpl("admins", Lists.newArrayList(servicePermission));
        GrantedAuthority roleAuthority = new RoleAuthority(role);
        registerUser(Users.create("admin", "password", Lists.newArrayList(roleAuthority)));

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken("admin", "password");
        Authentication successToken = authProvider.authenticate(authToken);
        acManager.decide(successToken, invocation, null);
    }

    @Test
    public void testContextDependentPermission_shouldAllowAccess() throws Exception {
        GrantedAuthority authority = new PermissionAuthority(new ServicePermission("a", "foo"));
        registerUser(Users.create("admin", "password", Lists.newArrayList(authority)));

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken("admin", "password");
        Authentication successToken = authProvider.authenticate(authToken);

        ContextHolder.get().setCurrentContextId("foo");
        acManager.decide(successToken, invocation, null);
    }

    @Test(expected = AccessDeniedException.class)
    public void testWrongContextPermission_shouldDenyAccess() throws Exception {
        GrantedAuthority authority = new PermissionAuthority(new ServicePermission("a", "bar"));
        registerUser(Users.create("admin", "password", Lists.newArrayList(authority)));

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken("admin", "password");
        Authentication successToken = authProvider.authenticate(authToken);

        ContextHolder.get().setCurrentContextId("foo");
        acManager.decide(successToken, invocation, null);
    }

    @Test
    public void testPublicAccessMethod_shouldGrantAccess() throws Exception {
        acManager.decide(null, MethodInvocationUtils.create(new DummyServiceImpl("foo"), "getInstanceId"), null);
    }

    private void registerUser(UserDetails user) {
        userData.put(user.getUsername(), user);
    }
}
