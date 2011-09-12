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

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.openengsb.connector.usernamepassword.internal.UsernamePasswordServiceImpl;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.test.UserManagerStub;
import org.ops4j.pax.wicket.api.ApplicationLifecycleListener;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

public abstract class AbstractLoginTest extends AbstractUITest {

    private UserDataManager userManager;

    @Before
    public void setupLogin() throws Exception {
        mockAuthentication();
        ApplicationLifecycleListener listener = mock(ApplicationLifecycleListener.class);
        tester = new WicketTester(new WicketApplication(listener) {
            @Override
            protected void addInjector() {
                addComponentInstantiationListener(new PaxWicketSpringBeanComponentInjector(this, context));
            }
        });
    }

    private void mockAuthentication() throws UserNotFoundException {
        userManager = new UserManagerStub();
        userManager.createUser("test");
        userManager.setUserCredentials("test", "password", "password");

        userManager.createUser("admin");
        userManager.setUserCredentials("admin", "password", "password");
        UsernamePasswordServiceImpl authConnector = new UsernamePasswordServiceImpl();
        authConnector.setUserManager(userManager);
        context.putBean("authenticationManager", authConnector);
    }
}
