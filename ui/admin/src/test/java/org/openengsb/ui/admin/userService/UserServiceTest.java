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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ResourceBundle;

import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.test.UserManagerStub;
import org.openengsb.ui.admin.AbstractUITest;
import org.openengsb.ui.admin.index.Index;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

public class UserServiceTest extends AbstractUITest {

    private UserDataManager userManager;

    private final ResourceBundle resources;

    public UserServiceTest() {
        String name = this.getClass().getName();
        resources = ResourceBundle.getBundle(name.substring(0, name.length() - 4));
    }

    protected String localization(String resourceName) {
        if (resources != null) {
            return resources.getString(resourceName);
        } else {
            return null;
        }

    }

    @Before
    public void setup() {
        setupTesterWithSpringMockContext();
        userManager = new UserManagerStub();
        context.putBean(userManager);
        userManager.createUser("test42");
    }

    @Test
    public void testLinkAppearsWithCaptionUserManagement() throws Exception {
        tester.startPage(Index.class);
        tester.assertContains("User Management");
    }

    private void setupTesterWithSpringMockContext() {
        tester.getApplication().addComponentInstantiationListener(
            new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context));
    }

    @Test
    public void testUserCreation_ShouldWork() {
        tester.startPage(UserService.class);

        FormTester formTester = tester.newFormTester("usermanagementContainer:form");
        formTester.setValue("username", "user1");
        formTester.setValue("password", "password");
        formTester.setValue("passwordVerification", "password");
        formTester.setValue("roles", "admin,user");
        formTester.submit();

        tester.assertNoErrorMessage();
        assertThat(userManager.getUserList(), hasItem("user1"));
    }

    @Test
    public void createAndDeleteUser_ShouldWork() {
        tester.startPage(UserService.class);
        tester.debugComponentTrees();
        AjaxLink<?> link =
            (AjaxLink<?>) tester.getComponentFromLastRenderedPage("usermanagementContainer:users:0:user.delete");
        tester.executeAjaxEvent(link, "onclick");
        ListView<?> userListView =
            (ListView<?>) tester.getComponentFromLastRenderedPage("usermanagementContainer:users");
        assertThat(userListView.size(), is(0));

    }

    @Test
    public void testUserCreationWithoutRoles_ShouldWork() throws Exception {
        tester.startPage(UserService.class);

        FormTester formTester = tester.newFormTester("usermanagementContainer:form");
        formTester.setValue("username", "user1");
        formTester.setValue("password", "password");
        formTester.setValue("passwordVerification", "password");
        formTester.submit();
        tester.assertNoErrorMessage();
        assertThat(userManager.getUserCredentials("user1", "password"), is("password"));
    }

    @Test
    public void testErrorMessage_shouldReturnUserExists() {
        tester.startPage(UserService.class);
        userManager.createUser("user1");
        FormTester formTester = tester.newFormTester("usermanagementContainer:form");
        formTester.setValue("username", "user1");
        formTester.setValue("password", "password");
        formTester.setValue("roles", "admin,user");
        formTester.setValue("passwordVerification", "password");
        formTester.submit();
        tester.assertErrorMessages(new String[]{ localization("userExistError") });
    }

    @Test
    public void testShowCreatedUser_ShouldShowAdmin() {
        tester.startPage(UserService.class);
        tester.assertContains(localization("existingUser.title"));
        tester.assertContains("admin");
        tester.assertContains("delete");
    }

    @Test
    public void testErrorMessage_ShouldReturnWrongSecondPassword() {
        userManager.createUser("user1");
        tester.startPage(UserService.class);
        FormTester formTester = tester.newFormTester("usermanagementContainer:form");
        formTester.setValue("username", "user1");
        formTester.setValue("password", "password");
        formTester.setValue("passwordVerification", "password2");
        formTester.submit();
        tester.assertErrorMessages(new String[]{ localization("passwordError") });
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
        //
        // ArgumentCaptor<User> argCaptor = ArgumentCaptor.forClass(User.class);
        // verify(userManager, times(1)).createUser(argCaptor.capture());
        // User userCreated = argCaptor.getValue();
        // assertThat(userCreated.getAuthorities(), hasItem((GrantedAuthority) new GrantedAuthorityImpl("ROLE_ADMIN")));
    }
}
