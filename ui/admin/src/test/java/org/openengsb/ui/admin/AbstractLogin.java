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

package org.openengsb.ui.admin;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.security.model.User;
import org.openengsb.core.common.service.DomainService;
import org.openengsb.core.security.internal.UserManagerImpl;
import org.openengsb.ui.admin.model.OpenEngSBVersion;
import org.osgi.framework.ServiceReference;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public abstract class AbstractLogin {

    private WicketTester tester;
    private ApplicationContextMock contextMock;
    private UserManagerImpl userManager;

    @Before
    public void setup() {
        contextMock = new ApplicationContextMock();
        mockAuthentication();
        mockIndex();

        WebApplication app = new WicketApplication() {
            @Override
            protected void addInjector() {
                addComponentInstantiationListener(new SpringComponentInjector(this, contextMock, true));
            }
        };
        tester = new WicketTester(app);
    }

    private void mockIndex() {
        DomainService managedServicesMock = mock(DomainService.class);
        when(managedServicesMock.getAllServiceInstances()).thenAnswer(new Answer<List<ServiceReference>>() {
            @Override
            public List<ServiceReference> answer(InvocationOnMock invocation) {
                return Collections.emptyList();
            }
        });
        contextMock.putBean(managedServicesMock);
        contextMock.putBean(mock(ContextCurrentService.class));
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
        contextMock.putBean("authenticationManager", authManager);
        contextMock.putBean("openengsbVersion", new OpenEngSBVersion());
    }

    public WicketTester getTester() {
        return tester;
    }
}
