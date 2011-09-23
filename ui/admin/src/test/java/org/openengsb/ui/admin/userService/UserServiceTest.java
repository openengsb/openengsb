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
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ResourceBundle;

import org.apache.wicket.Component;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.ui.admin.AbstractUITest;
import org.openengsb.ui.admin.index.Index;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

import com.google.common.collect.ImmutableMap;

public class UserServiceTest extends AbstractUITest {

    protected String localization(Class<?> component, String resourceName) {
        ResourceBundle resources = ResourceBundle.getBundle(UserListPage.class.getName());
        if (resources != null) {
            return resources.getString(resourceName);
        } else {
            return null;
        }

    }

    @Before
    public void setup() throws Exception {
        setupTesterWithSpringMockContext();
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
    public void createUserPageWithoutParams_shouldEnableUsernameField() throws Exception {
        tester.startPage(UserEditPage.class);
        tester.assertRenderedPage(UserEditPage.class);
        Component usernameField =
            tester.getComponentFromLastRenderedPage("userEditor:userEditorContainer:userForm:username");
        assertTrue(usernameField.isEnabled());
    }

    @Test
    public void createUserPageWithUserParam_shouldDisableUsernameField() throws Exception {
        tester.startPage(UserEditPage.class, new PageParameters(ImmutableMap.of("user", "admin")));
        tester.assertRenderedPage(UserEditPage.class);
        Component usernameField =
            tester.getComponentFromLastRenderedPage("userEditor:userEditorContainer:userForm:username");
        assertFalse(usernameField.isEnabled());
    }

    @Test
    public void createUserLink_shouldCreateEmptyEditPage() throws Exception {
        tester.startPage(UserListPage.class);
        tester.debugComponentTrees();
        AjaxButton button =
            (AjaxButton) tester.getComponentFromLastRenderedPage("lazy:usermanagementContainer:form:createButton");
        tester.executeAjaxEvent(button, "onclick");
        tester.assertRenderedPage(UserEditPage.class);
        Component usernameField =
            tester.getComponentFromLastRenderedPage("userEditor:userEditorContainer:userForm:username");
        assertTrue(usernameField.isEnabled());
    }

    @Test
    public void testUserCreation_ShouldWork() {
        tester.startPage(UserEditPage.class);

        FormTester formTester = tester.newFormTester("userEditor:userEditorContainer:userForm");
        formTester.setValue("username", "user1");
        formTester.setValue("password", "password");
        formTester.setValue("passwordVerification", "password");
        formTester.submit();

        tester.assertNoErrorMessage();
        assertThat(userManager.getUserList(), hasItem("user1"));
    }

    @Test
    public void testCreatePermission() {
        tester.startPage(UserEditPage.class);
        tester.executeAjaxEvent("userEditor:userEditorContainer:userForm:createPermission", "onclick");
        tester.debugComponentTrees();
    }

    @Test
    public void deleteUser_shouldBeRemovedFromList() throws Exception {
        tester.startPage(UserListPage.class);
        tester.clickLink("lazy:usermanagementContainer:form:users:0:user.delete");

        tester.executeAjaxEvent("lazy:usermanagementContainer:form:users:0:confirm:yes", "onclick");
        assertThat(userManager.getUserList(), not(hasItem("test")));
    }

    //
    // @Test
    // public void testUserCreationWithoutRoles_ShouldWork() throws Exception {
    // tester.startPage(UserListPage.class);
    //
    // FormTester formTester = tester.newFormTester("usermanagementContainer:form");
    // formTester.setValue("username", "user1");
    // formTester.setValue("password", "password");
    // formTester.setValue("passwordVerification", "password");
    // formTester.submit();
    // tester.assertNoErrorMessage();
    // assertThat(userManager.getUserCredentials("user1", "password"), is("password"));
    // }
    //
    @Test
    public void testErrorMessage_shouldReturnUserExists() throws Exception {
        tester.startPage(UserEditPage.class);
        userManager.createUser("user1");
        tester.debugComponentTrees();
        FormTester formTester = tester.newFormTester("userEditor:userEditorContainer:userForm");
        formTester.setValue("username", "user1");
        formTester.setValue("password", "password");
        formTester.setValue("passwordVerification", "password");
        formTester.submit();
        tester.assertErrorMessages(new String[]{ localization(UserListPage.class, "userExistError") });
    }

    @Test
    public void testShowCreatedUser_ShouldShowAdmin() {
        tester.startPage(UserListPage.class);
        tester.assertContains("admin");
    }

    @Test
    public void testErrorMessage_ShouldReturnWrongSecondPassword() throws Exception {
        userManager.createUser("user1");
        tester.startPage(UserEditPage.class, new PageParameters(ImmutableMap.of("user", "user1")));
        FormTester formTester = tester.newFormTester("userEditor:userEditorContainer:userForm");
        formTester.setValue("username", "user1");
        formTester.setValue("password", "password");
        formTester.setValue("passwordVerification", "password2");
        formTester.submit();
        tester.assertErrorMessages(new String[]{ localization(UserListPage.class, "passwordError") });
    }

    // @Test
    // public void testShowUserAuthorities() throws Exception {
    // tester.startPage(UserListPage.class);
    // FormTester formTester = tester.newFormTester("usermanagementContainer:form");
    // formTester.setValue("username", "user1");
    // formTester.setValue("password", "password");
    // formTester.setValue("passwordVerification", "password");
    // formTester.setValue("roles", "ROLE_ADMIN");
    // formTester.submit();
    // tester.assertNoErrorMessage();
    // //
    // // ArgumentCaptor<User> argCaptor = ArgumentCaptor.forClass(User.class);
    // // verify(userManager, times(1)).createUser(argCaptor.capture());
    // // User userCreated = argCaptor.getValue();
    // // assertThat(userCreated.getAuthorities(), hasItem((GrantedAuthority) new GrantedAuthorityImpl("ROLE_ADMIN")));
    // }
}
