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

package org.openengsb.ui.web;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.common.service.DomainService;
import org.osgi.framework.ServiceReference;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LoginPageTest {

    private WicketTester tester;
    private ApplicationContextMock contextMock;

    @Before
    public void setUp() {
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
        AuthenticationManager authManager = mock(AuthenticationManager.class);
        final Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
        when(authManager.authenticate(any(Authentication.class))).thenAnswer(new Answer<Authentication>() {
            @Override
            public Authentication answer(InvocationOnMock invocation) {
                Authentication auth = (Authentication) invocation.getArguments()[0];
                if (auth.getCredentials().equals("password")) {
                    return new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(),
                        authorities);
                }
                throw new BadCredentialsException("wrong password");
            }
        });
        contextMock.putBean("authenticationManager", authManager);
    }

    @Test
    public void testLoginPageIsDisplayed() throws Exception {
        tester.startPage(LoginPage.class);
        tester.assertRenderedPage(LoginPage.class);
    }

    @Test
    public void testRedirectToLogin() throws Exception {
        tester.startPage(Index.class);
        tester.assertRenderedPage(LoginPage.class);
    }

    @Test
    public void testEnterLogin() throws Exception {
        tester.startPage(LoginPage.class);
        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("username", "test");
        formTester.setValue("password", "password");
        formTester.submit();
        tester.assertNoErrorMessage();
        tester.assertRenderedPage(Index.class);
    }

    @Test
    public void testLogout() throws Exception {
        tester.startPage(LoginPage.class);
        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("username", "test");
        formTester.setValue("password", "password");
        formTester.submit();
        tester.clickLink("logout");
        tester.assertRenderedPage(LoginPage.class);
    }

    @Test
    public void testInvalidLogin() throws Exception {
        tester.startPage(LoginPage.class);
        FormTester formTester = tester.newFormTester("form");
        formTester.setValue("username", "test");
        formTester.setValue("password", "wrongpassword");
        formTester.submit();
        tester.assertRenderedPage(LoginPage.class);
        List<Serializable> messages = tester.getMessages(FeedbackMessage.ERROR);
        assertFalse(messages.isEmpty());
    }
}
