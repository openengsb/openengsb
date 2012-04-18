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
import static org.junit.Assert.assertThat;

import java.util.ResourceBundle;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.ui.admin.AbstractUITest;
import org.openengsb.ui.admin.index.Index;
import org.ops4j.pax.wicket.test.spring.PaxWicketSpringBeanComponentInjector;

public class UserServiceTest extends AbstractUITest {

    protected String localization(Class<?> component, String resourceName) {
        ResourceBundle resources = ResourceBundle.getBundle(component.getName());
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
        tester.getApplication().getComponentInstantiationListeners()
            .add(new PaxWicketSpringBeanComponentInjector(tester.getApplication(), context));
    }

    //TODO Fix test as UserEditPage does not exist anymore. Now there is used a Modal dialogue in the UserListPage
    /*
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
        PageParameters parameters = new PageParameters();
        parameters.set("user", "admin");
        tester.startPage(UserEditPage.class, parameters);
        tester.assertRenderedPage(UserEditPage.class);
        Component usernameField =
            tester.getComponentFromLastRenderedPage("userEditor:userEditorContainer:userForm:username");
        assertFalse(usernameField.isEnabled());
    }
    */
    //TODO Fix test as UserEditPage does not exist anymore. Now there is used a Modal dialogue in the UserListPage
    /*
    @Test
    public void createUserLink_shouldCreateEmptyEditPage() throws Exception {
        tester.startPage(UserListPage.class);
        tester.debugComponentTrees();
        AjaxLink<?> button =
            (AjaxLink<?>) tester.getComponentFromLastRenderedPage("lazy:createButton");
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void testCreatePermission() {
        tester.startPage(UserEditPage.class);
        tester.debugComponentTrees();
        tester.executeAjaxEvent("userEditor:userEditorContainer:userForm:permissionListContainer:createPermission",
            "onclick");

        DropDownChoice<Class> dropdown = (DropDownChoice<Class>) tester.getComponentFromLastRenderedPage(
            "userEditor:userEditorContainer:userForm:permissionListContainer:"
                    + "createPermissionContainer:createPermissionContent:container:form:permissionTypeSelect");

        List<Class> choices = (List<Class>) dropdown.getChoices();
        assertThat(choices, hasItem((Class) WicketPermission.class));

        FormTester permissionFormTester = tester.newFormTester(
            "userEditor:userEditorContainer:userForm:permissionListContainer:"
                    + "createPermissionContainer:createPermissionContent:container:form");
        permissionFormTester.select("permissionTypeSelect", 0);

        tester.debugComponentTrees();
    }
    */
    @Test
    public void deleteUser_shouldBeRemovedFromList() throws Exception {
        tester.startPage(UserListPage.class);
        tester.debugComponentTrees();
        tester.clickLink("lazy:userList:listContainer:form:list:0:item.delete");
        tester.debugComponentTrees();
        tester.executeAjaxEvent("lazy:userList:listContainer:form:list:0:confirm:yes", "onclick");
        assertThat(userManager.getUserList(), not(hasItem("test")));
    }

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
        tester.assertErrorMessages(new String[]{ localization(UserEditPanel.class, "userExistError") });
    }
    */
    @Test
    public void testShowCreatedUser_ShouldShowAdmin() {
        tester.startPage(UserListPage.class);
        tester.assertContains("admin");
    }

    //TODO Fix test as UserEditPage does not exist anymore. Now there is used a Modal dialogue in the UserListPage
    /*
    @Test
    public void testErrorMessage_ShouldReturnWrongSecondPassword() throws Exception {
        userManager.createUser("user1");
        PageParameters parameters = new PageParameters();
        parameters.set("user", "user1");
        tester.startPage(UserEditPage.class, parameters);
        FormTester formTester = tester.newFormTester("userEditor:userEditorContainer:userForm");
        formTester.setValue("username", "user1");
        formTester.setValue("password", "password");
        formTester.setValue("passwordVerification", "password2");
        formTester.submit();
        tester.assertErrorMessages(new String[]{ localization(UserEditPanel.class, "passwordError") });
    }
   */
}
