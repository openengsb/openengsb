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

import java.util.concurrent.atomic.AtomicReference;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.MethodRule;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.security.Credentials;
import org.openengsb.core.test.rules.DedicatedThread;
import org.openengsb.domain.authentication.AuthenticationException;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

public abstract class AbstractLoginTest extends AbstractUITest {

    @Rule
    public MethodRule dedicatedThread = new DedicatedThread();
    private Session mockShiroSession;
    protected Subject mockSubject;
    private SubjectThreadState threadState;

    /**
     * Clear Shiro's thread local so that any subject mocking we've done during the test does not pollute subsequent
     * tests.
     */
    @After
    public void detachSubject() {
        threadState.clear();
    }

    @Before
    public void setupLogin() throws Exception {
        mockAuthentication();
        tester = new WicketTester(new WicketApplication());

        tester.getApplication().addComponentInstantiationListener(
            new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context));

        mockShiroSession = mock(Session.class);
        mockSubject = mock(Subject.class);
        when(mockSubject.getSession()).thenReturn(mockShiroSession);
        threadState = new SubjectThreadState(mockSubject);
        threadState.bind();

        final AtomicReference<Object> authenticated = new AtomicReference<Object>();

        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                AuthenticationToken t = (AuthenticationToken) invocation.getArguments()[0];
                try {
                    authConnector.authenticate(t.getPrincipal().toString(), (Credentials) t.getCredentials());
                } catch (AuthenticationException e) {
                    throw new org.apache.shiro.authc.AuthenticationException(e);
                }
                authenticated.set(t.getPrincipal());
                return null;
            }
        }).when(mockSubject).login(Mockito.any(AuthenticationToken.class));

        when(mockSubject.isAuthenticated()).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return authenticated.get() != null;
            }
        });

        when(mockSubject.getPrincipal()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return authenticated.get();
            }
        });
    }
}
