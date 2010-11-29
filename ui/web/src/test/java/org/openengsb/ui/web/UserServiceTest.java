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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.spring.injection.annot.SpringComponentInjector;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.common.context.ContextCurrentService;
import org.openengsb.core.usermanagement.UserManager;
import org.openengsb.core.usermanagement.exceptions.UserExistsException;
import org.openengsb.core.usermanagement.model.User;
import org.openengsb.ui.web.model.OpenEngSBVersion;
import org.osgi.framework.BundleContext;

public class UserServiceTest {

    private WicketTester tester;

    private ApplicationContextMock context;
    private FormTester formTester;
    private BundleContext bundleContext;
    private UserManager userManager;

    @Before
    public void setup() {
        tester = new WicketTester();
        context = new ApplicationContextMock();
        context.putBean(mock(ContextCurrentService.class));
        bundleContext = mock(BundleContext.class);
        context.putBean(bundleContext);
        context.putBean("openengsbVersion", new OpenEngSBVersion());
        userManager = mock(UserManager.class);
        context.putBean("userManager", userManager);
        setupTesterWithSpringMockContext();
    }

    @Test
    public void testLinkAppearsWithCaptionUserManagement() throws Exception {
        tester.startPage(Index.class);
        tester.assertContains("User Management");
    }

    private void setupTesterWithSpringMockContext() {
        tester.getApplication()
            .addComponentInstantiationListener(new SpringComponentInjector(tester.getApplication(), context, true));
    }

    @Test
    public void testUserCreation_ShouldWork() {
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
        doThrow(new UserExistsException("user exists")).
            when(userManager).createUser(new User("user1", "password"));
        FormTester formTester = tester.newFormTester("usermanagementContainer:form");
        formTester.setValue("username", "user1");
        formTester.setValue("password", "password");
        formTester.setValue("passwordVerification", "password");
        formTester.submit();
        tester.assertErrorMessages(new String[]{ "User already exists" });
        verify(userManager, times(1)).createUser(new User("user1", "password"));

    }

    @Test
    public void testShowCreatedUser_ShouldShowAdmin() {
        when(userManager.getAllUser()).thenAnswer(new Answer<List<User>>() {
            @Override
            public List<User> answer(InvocationOnMock invocationOnMock) {
                List<User> users = new ArrayList<User>();
                users.add(new User("admin", "password"));
                return users;
            }
        });
        tester.startPage(UserService.class);
        tester.assertContains("Existing Users");
        tester.assertContains("admin");
        tester.assertContains("delete");
    }

    @Test
    public void testErrorMessage_ShouldReturnWrongSecondPassword() {
        tester.startPage(UserService.class);
        doThrow(new UserExistsException("user exists")).
            when(userManager).createUser(new User("user1", "password"));
        FormTester formTester = tester.newFormTester("usermanagementContainer:form");
        formTester.setValue("username", "user1");
        formTester.setValue("password", "password");
        formTester.setValue("passwordVerification", "password2");
        formTester.submit();
        tester.assertErrorMessages(new String[]{ "Invalid password" });
        verify(userManager, times(0)).createUser(new User("user1", "password"));
    }

}
