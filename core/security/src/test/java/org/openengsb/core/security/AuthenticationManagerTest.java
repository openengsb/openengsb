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
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.util.Users;
import org.openengsb.core.security.internal.SystemUserAuthenticationProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class AuthenticationManagerTest {

    private ProviderManager authenticationManager;

    @Test
    public void testAuthenticateBundle() throws Exception {
        Authentication authenticatedToken =
            authenticationManager.authenticate(new BundleAuthenticationToken("mybundle", "mykey"));
        assertThat(authenticatedToken.isAuthenticated(), is(true));
        assertThat(authenticatedToken.getAuthorities(), hasItem((GrantedAuthority) new GrantedAuthorityImpl(
            "ROLE_ADMIN")));
    }

    @Test
    public void testAuthenticateUser() throws Exception {
        Authentication authenticatedToken =
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("testuser", "password"));
        assertThat(authenticatedToken.isAuthenticated(), is(true));
    }

    @Before
    public void setUp() {
        ProviderManager manager = new ProviderManager();
        List<AuthenticationProvider> providers = new ArrayList<AuthenticationProvider>();
        final DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();

        UserDetailsService userDetails = mock(UserDetailsService.class);
        when(userDetails.loadUserByUsername(anyString())).thenAnswer(new Answer<UserDetails>() {
            @Override
            public UserDetails answer(InvocationOnMock invocation) throws Throwable {
                return Users.create((String) invocation.getArguments()[0], "password");
            }
        });
        daoProvider.setUserDetailsService(userDetails);
        providers.add(daoProvider);
        providers.add(new SystemUserAuthenticationProvider());
        manager.setProviders(providers);
        authenticationManager = manager;
    }
}
