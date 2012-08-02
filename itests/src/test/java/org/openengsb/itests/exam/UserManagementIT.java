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

package org.openengsb.itests.exam;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.security.model.Authentication;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.security.SecurityContext;
import org.openengsb.domain.authentication.AuthenticationDomain;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

@RunWith(JUnit4TestRunner.class)
// This one will run each test in it's own container (slower speed)
// @ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class UserManagementIT extends AbstractPreConfiguredExamTestHelper {

    private UserDataManager userManager;
    private AuthenticationDomain authenticator;

    @Before
    public void setUp() throws Exception {
        userManager = getOsgiService(UserDataManager.class);
        authenticator = getOsgiService(AuthenticationDomain.class, "(location.root=authentication-root)", 20000);
    }

    @After
    public void tearDown() throws Exception {
        SecurityContext.executeWithSystemPermissions(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                userManager.deleteUser("test");
                return null;
            }
        });
    }

    @Test
    public void testCreateUserAndLogin_shouldAuthenticateSuccessful() throws Exception {
        SecurityContext.executeWithSystemPermissions(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                userManager.createUser("test");
                userManager.setUserCredentials("test", "password", "password");
                return null;
            }
        });
        Authentication authenticate = authenticator.authenticate("test", new Password("password"));
        assertThat(authenticate, not(nullValue()));
    }

}
