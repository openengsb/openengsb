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

package org.openengsb.ui.admin.userService;

import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItem;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.context.ContextCurrentService;
import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserManagementException;
import org.openengsb.core.api.security.UserManager;
import org.openengsb.core.api.security.model.User;
import org.openengsb.core.test.LocalisedTest;
import org.openengsb.ui.admin.index.Index;
import org.openengsb.ui.admin.model.OpenEngSBFallbackVersion;
import org.openengsb.ui.api.OpenEngSBVersionService;
import org.ops4j.pax.wicket.test.spring.ApplicationContextMock;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;
import org.osgi.framework.BundleContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public class UserServiceTest extends LocalisedTest {

    private WicketTester tester;

    private ApplicationContextMock context;
    private BundleContext bundleContext;
    private UserManager userManager;

    @Before
    public void setup() {
        tester = new WicketTester();
        context = new ApplicationContextMock();
        context.putBean(mock(ContextCurrentService.class));
        bundleContext = mock(BundleContext.class);
        context.putBean(bundleContext);
        context.putBean("openengsbVersion", new OpenEngSBFallbackVersion());
        List<OpenEngSBVersionService> versionService = new ArrayList<OpenEngSBVersionService>();
        context.putBean("openengsbVersionService", versionService);
        userManager = mock(UserManager.class);
        context.putBean("userManager", userManager);
        setupTesterWithSpringMockContext();
    }

    @Test
    public void testLinkAppearsWithCaptionUserManagement_shouldContainUserManagementLink() {
        tester.startPage(Index.class);
        tester.assertContains("User Management");
    }

    private void setupTesterWithSpringMockContext() {
        tester.getApplication().addComponentInstantiationListener(
            new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context));
    }

    @Test
    public void testUserCreation_shouldWork() {
        tester.startPage(UserService.class);

        FormTester formTester = tester.newFormTester("usermanagementContainer:form");
        formTester.setValue("username", "user1");
        formTester.setValue("password", "password");
        formTester.setValue("passwordVerification", "password");
        formTester.setValue("roles", "admin,user");
        formTester.submit();
        tester.assertNoErrorMessage();
        verify(userManager, times(1)).createUser(new User("user1", "password"));

    }

    @Test
    public void testUserCreationWithoutRoles_shouldWork() {
        tester.startPage(UserService.class);

        FormTester formTester = tester.newFormTester("usermanagementContainer:form");
        formTester.setValue("username", "user1");
        formTester.setValue("password", "password");
        formTester.setValue("passwordVerification", "password");
        formTester.submit();
        tester.assertNoErrorMessage();
        verify(userManager, times(1)).createUser(new User("user1", "password"));
    }

    @Test
    public void testErrorMessage_shouldReturnUserExists() {
        tester.startPage(UserService.class);
        doThrow(new UserExistsException("user exists")).when(userManager).createUser(new User("user1", "password"));
        FormTester formTester = tester.newFormTester("usermanagementContainer:form");
        formTester.setValue("username", "user1");
        formTester.setValue("password", "password");
        formTester.setValue("roles", "admin,user");
        formTester.setValue("passwordVerification", "password");
        formTester.submit();
        tester.assertErrorMessages(new String[]{ localization("userExistError") });
        verify(userManager, times(1)).createUser(new User("user1", "password"));

    }

    @Test
    public void testShowCreatedUser_shouldShowAdmin() {
        when(userManager.getAllUser()).thenAnswer(new Answer<List<User>>() {
            @Override
            public List<User> answer(InvocationOnMock invocationOnMock) {
                List<User> users = new ArrayList<User>();
                users.add(new User("admin", "password"));
                return users;
            }
        });
        tester.startPage(UserService.class);
        tester.assertContains(localization("existingUser.title"));
        tester.assertContains("admin");
        tester.assertContains("delete");
    }

    @Test
    public void testErrorMessage_shouldReturnWrongSecondPassword() throws Exception {
        tester.startPage(UserService.class);
        doThrow(new UserExistsException("user exists")).when(userManager).createUser(new User("user1", "password"));
        FormTester formTester = tester.newFormTester("usermanagementContainer:form");
        formTester.setValue("username", "user1");
        formTester.setValue("password", "password");
        formTester.setValue("passwordVerification", "password2");
        formTester.submit();
        tester.assertErrorMessages(new String[]{ localization("passwordError") });
        verify(userManager, times(0)).createUser(new User("user1", "password"));
    }

    @Test
    public void testPersistenceError_ShouldThrowUserManagementExceptionAndShowErrorMessage() {
        tester.startPage(UserService.class);
        doThrow(new UserManagementException("database error")).when(userManager).createUser(
            new User("user1", "password"));
        FormTester formTester = tester.newFormTester("usermanagementContainer:form");
        formTester.setValue("username", "user1");
        formTester.setValue("password", "password");
        formTester.setValue("roles", "admin,user");
        formTester.setValue("passwordVerification", "password");
        formTester.submit();
        tester.assertErrorMessages(new String[]{ localization("userManagementExceptionError") });
    }

    @Test
    public void testShowUserAuthorities() throws Exception {
        tester.startPage(UserService.class);
        FormTester formTester = tester.newFormTester("usermanagementContainer:form");
        formTester.setValue("username", "user1");
        formTester.setValue("password", "password");
        formTester.setValue("passwordVerification", "password");
        formTester.setValue("roles", "ROLE_ADMIN");
        formTester.submit();
        tester.assertNoErrorMessage();
        ArgumentCaptor<User> argCaptor = ArgumentCaptor.forClass(User.class);
        verify(userManager, times(1)).createUser(argCaptor.capture());
        User userCreated = argCaptor.getValue();
        assertThat(userCreated.getAuthorities(), hasItem((GrantedAuthority) new GrantedAuthorityImpl("ROLE_ADMIN")));
    }
}
