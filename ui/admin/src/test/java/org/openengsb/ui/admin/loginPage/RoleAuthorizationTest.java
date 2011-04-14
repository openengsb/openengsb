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

package org.openengsb.ui.admin.loginPage;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.apache.wicket.Component;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;
import org.openengsb.ui.admin.AbstractLogin;
import org.openengsb.ui.admin.global.BookmarkablePageLabelLink;
import org.openengsb.ui.admin.testClient.TestClient;
import org.openengsb.ui.admin.userService.UserService;

/**
 * This class tests the ui for visible components depending on the logged in user roles
 */
public class RoleAuthorizationTest extends AbstractLogin {

    @Test
    public void testHeaderComponentsForAdmin_UserServiceShouldBeVisible() {
        tester.startPage(LoginPage.class);
        FormTester formTester = tester.newFormTester("loginForm");
        formTester.setValue("username", "admin");
        formTester.setValue("password", "password");
        formTester.submit();
        BookmarkablePageLabelLink userServiceLink =
            (BookmarkablePageLabelLink) tester.getComponentFromLastRenderedPage("header:headerMenuItems:5:link");
        assertNotNull(userServiceLink);
        assertThat(userServiceLink.getPageClass().getCanonicalName(), is(UserService.class.getCanonicalName()));
    }

    @Test
    public void testHeaderComponentsForNormalUser_UserServiceShouldNotBeVisible() {
        tester.startPage(LoginPage.class);
        FormTester formTester = tester.newFormTester("loginForm");
        formTester.setValue("username", "user");
        formTester.setValue("password", "password");
        formTester.submit();
        BookmarkablePageLabelLink userServiceLink =
            (BookmarkablePageLabelLink) tester.getComponentFromLastRenderedPage("header:headerMenuItems:5:link");
        assertNull(userServiceLink);
    }

    @Test
    public void testTestClientVisibleComponentsForAdmin_EveryThingShouldBeVisible() {
        tester.startPage(LoginPage.class);
        FormTester formTester = tester.newFormTester("loginForm");
        formTester.setValue("username", "admin");
        formTester.setValue("password", "password");
        formTester.submit();
        tester.clickLink("header:headerMenuItems:1:link");
        tester.assertRenderedPage(TestClient.class);
        Component domains = tester.getComponentFromLastRenderedPage("serviceManagementContainer");
        assertNotNull(domains);
    }

    @Test
    public void testTestClientVisibleComponentsForNormalUser_serviceManagementContainerShouldNotBeVisible() {
        tester.startPage(LoginPage.class);
        FormTester formTester = tester.newFormTester("loginForm");
        formTester.setValue("username", "test");
        formTester.setValue("password", "password");
        formTester.submit();
        tester.clickLink("header:headerMenuItems:1:link");
        tester.assertRenderedPage(TestClient.class);
        Component domains = tester.getComponentFromLastRenderedPage("serviceManagementContainer");
        assertNull(domains);
    }

}
