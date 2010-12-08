package org.openengsb.ui.web;

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
import org.openengsb.core.common.service.DomainService;
import org.openengsb.core.usermanagement.UserManagerImpl;
import org.openengsb.core.usermanagement.model.User;
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
    }

    public WicketTester getTester() {
        return tester;
    }
}
