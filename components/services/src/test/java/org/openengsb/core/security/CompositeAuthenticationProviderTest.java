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

import java.lang.reflect.Proxy;
import java.util.Hashtable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.security.model.User;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.security.internal.AuthenticationProviderStrategy;
import org.openengsb.core.security.internal.SystemUserAuthenticationProvider;
import org.openengsb.core.services.internal.virtual.CompositeConnector;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class CompositeAuthenticationProviderTest extends AbstractOsgiMockServiceTest {

    private AuthenticationProvider authenticationManager;

    @Before
    public void setUp() {
        DaoAuthenticationProvider daoProvider = new DaoAuthenticationProvider();
        UserDetailsService userDetails = mock(UserDetailsService.class);
        when(userDetails.loadUserByUsername(anyString())).thenAnswer(new Answer<UserDetails>() {
            @Override
            public UserDetails answer(InvocationOnMock invocation) throws Throwable {
                return new User((String) invocation.getArguments()[0], "password");
            }
        });
        daoProvider.setUserDetailsService(userDetails);
        registerService(daoProvider, new Hashtable<String, Object>(), AuthenticationProvider.class);
        registerService(new SystemUserAuthenticationProvider(), new Hashtable<String, Object>(),
            AuthenticationProvider.class);

        CompositeConnector compositeConnector = new CompositeConnector("foo");
        compositeConnector.setCompositeHandler(new AuthenticationProviderStrategy());
        compositeConnector.setQueryString(String.format("(%s=%s)", Constants.OBJECTCLASS,
            AuthenticationProvider.class.getName()));

        AuthenticationProvider newProxyInstance =
            (AuthenticationProvider) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class<?>[]{ AuthenticationProvider.class, }, compositeConnector);
        authenticationManager = newProxyInstance;
    }

    @Test
    public void testAuthenticateBundle_shouldReturnAuthenticatedToken() throws Exception {
        Authentication authenticatedToken =
            authenticationManager.authenticate(new BundleAuthenticationToken("mybundle", "mykey"));
        assertThat(authenticatedToken.isAuthenticated(), is(true));
        assertThat(authenticatedToken.getAuthorities(), hasItem((GrantedAuthority) new GrantedAuthorityImpl(
            "ROLE_ADMIN")));
    }

    @Test
    public void testAuthenticateUser_shouldReturnAuthenticatedToken() throws Exception {
        Authentication authenticatedToken =
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("testuser", "password"));
        assertThat(authenticatedToken.isAuthenticated(), is(true));
    }

    @Test(expected = BadCredentialsException.class)
    public void testAuthenticateUserWithWrongPassword_shouldThrowException() throws Exception {
        Authentication authenticatedToken =
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken("testuser", "password2"));
        assertThat(authenticatedToken.isAuthenticated(), is(true));
    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        DefaultOsgiUtilsService osgiServiceUtils = new DefaultOsgiUtilsService();
        osgiServiceUtils.setBundleContext(bundleContext);
        registerService(osgiServiceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
        OpenEngSBCoreServices.setOsgiServiceUtils(osgiServiceUtils);
    }
}
