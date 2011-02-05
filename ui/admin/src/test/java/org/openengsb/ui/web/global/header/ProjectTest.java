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

package org.openengsb.ui.web.global.header;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.form.DropDownChoice;
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
import org.openengsb.ui.common.OpenEngSBWebSession;
import org.openengsb.ui.web.basePage.BasePage;
import org.openengsb.ui.web.index.Index;
import org.openengsb.ui.web.model.OpenEngSBVersion;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public class ProjectTest {
    private WicketTester tester;
    private ContextCurrentService contextService;
    private Page basePage;
    private ApplicationContextMock appContext;

    @Before
    public void setup() {
        contextService = mock(ContextCurrentService.class);
        appContext = new ApplicationContextMock();
        appContext.putBean(contextService);
        appContext.putBean("openengsbVersion", new OpenEngSBVersion());
        mockAuthentication();
        tester = new WicketTester(new WebApplication() {

            @Override
            protected void init() {
                super.init();
                addComponentInstantiationListener(new SpringComponentInjector(this, appContext, false));
            }

            @Override
            public Class<? extends Page> getHomePage() {
                return Index.class;
            }

            @Override
            public Session newSession(Request request, Response response) {
                return new OpenEngSBWebSession(request);
            }
        });

        // Maybe there is a more elegant way to do this...
        AuthenticatedWebSession session = AuthenticatedWebSession.get();
        session.signIn("", "password");

        when(contextService.getAvailableContexts()).thenReturn(Arrays.asList(new String[]{ "foo", "bar" }));
        basePage = tester.startPage(new BasePage());
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
        appContext.putBean("authenticationManager", authManager);
    }

    @Test
    public void testIfLabelIsPresent() {
        String labelString =
            tester.getApplication().getResourceSettings().getLocalizer().getString("project.choice.label",
                basePage.get("header"));
        tester.assertContains(labelString);
    }

    @Test
    public void testInitDefaultContext_shouldSetFooContext() {
        tester.assertComponent("projectChoiceForm:projectChoice", DropDownChoice.class);
        assertThat("foo", is(OpenEngSBWebSession.get().getThreadContextId()));
    }

    @Test
    public void testChangeContextDropdown_shouldChangeThreadlocal() {
        tester.assertComponent("projectChoiceForm:projectChoice", DropDownChoice.class);
        assertThat("foo", is(OpenEngSBWebSession.get().getThreadContextId()));

        verify(contextService).setThreadLocalContext("foo");

        FormTester formTester = tester.newFormTester("projectChoiceForm");
        formTester.select("projectChoice", 1);

        assertThat("bar", is(OpenEngSBWebSession.get().getThreadContextId()));

        // simulated page reload...
        tester.startPage(new BasePage());
        verify(contextService).setThreadLocalContext("bar");
    }

    @Test
    public void testSelectContext_shouldRedirectToChoicesResponsePage() {
        FormTester formTester = tester.newFormTester("projectChoiceForm");
        formTester.select("projectChoice", 1);

        @SuppressWarnings("unchecked")
        DropDownChoice<String> choice =
            (DropDownChoice<String>) tester.getComponentFromLastRenderedPage("projectChoiceForm:projectChoice");

        Class<? extends Page> responsePage = choice.getRequestCycle().getResponsePageClass();
        assertThat(choice.getPage(), is(responsePage));
    }
}
