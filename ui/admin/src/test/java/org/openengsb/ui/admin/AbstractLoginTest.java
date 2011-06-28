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

package org.openengsb.ui.admin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.security.UserManager;
import org.openengsb.core.api.security.model.User;
import org.openengsb.core.security.internal.UserManagerImpl;
import org.ops4j.pax.wicket.api.ApplicationLifecycleListener;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public abstract class AbstractLoginTest extends AbstractUITest {

    private UserManager userManager;

    @Before
    public void setup() {
        mockAuthentication();
        ApplicationLifecycleListener listener = mock(ApplicationLifecycleListener.class);
        tester = new WicketTester(new WicketApplication(listener) {
            @Override
            protected void addInjector() {
                addComponentInstantiationListener(new PaxWicketSpringBeanComponentInjector(this, context));
            }
        });
    }

    private void mockAuthentication() {
        ProviderManager authManager = new ProviderManager();
        List<AuthenticationProvider> providers = new ArrayList<AuthenticationProvider>();
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        userManager = mock(UserManagerImpl.class);
        provider.setUserDetailsService(userManager);
        providers.add(provider);
        authManager.setProviders(providers);

        final User user = new User("test", "password");
        when(userManager.loadUserByUsername("test")).thenAnswer(new Answer<User>() {
            @Override
            public User answer(InvocationOnMock invocationOnMock) {
                return user;
            }
        });
        ArrayList<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
        grantedAuthorities.add(new GrantedAuthorityImpl("ROLE_USER"));
        final User admin = new User("admin", "password", grantedAuthorities);
        when(userManager.loadUserByUsername("admin")).thenAnswer(new Answer<User>() {
            @Override
            public User answer(InvocationOnMock invocationOnMock) {
                return admin;
            }
        });
        context.putBean("authenticationManager", authManager);
    }
}
