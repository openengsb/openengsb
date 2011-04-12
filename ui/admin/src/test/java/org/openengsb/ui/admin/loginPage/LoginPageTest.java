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

import static org.junit.Assert.assertFalse;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;
import org.openengsb.ui.admin.AbstractLogin;
import org.openengsb.ui.admin.global.header.HeaderTemplate;
import org.openengsb.ui.admin.index.Index;
import org.openengsb.ui.admin.testClient.TestClient;

public class LoginPageTest extends AbstractLogin {

    @Test
    public void testLoginPageIsDisplayed() throws Exception {
        tester.startPage(LoginPage.class);
        tester.assertRenderedPage(LoginPage.class);
    }

    @Test
    public void testRedirectToLogin() throws Exception {
        tester.startPage(TestClient.class);
        tester.assertRenderedPage(LoginPage.class);
    }

    @Test
    public void testEnterLogin() throws Exception {
        tester.startPage(LoginPage.class);
        FormTester formTester = tester.newFormTester("loginForm");
        formTester.setValue("username", "test");
        formTester.setValue("password", "password");
        formTester.submit();
        tester.assertNoErrorMessage();
        tester.assertRenderedPage(Index.class);
    }

    @Test
    public void testLogout() throws Exception {
        tester.startPage(LoginPage.class);
        FormTester formTester = tester.newFormTester("loginForm");
        formTester.setValue("username", "test");
        formTester.setValue("password", "password");
        formTester.submit();
        tester.clickLink("logout");
        tester.assertRenderedPage(Index.class);
    }

    @Test
    public void testInvalidLogin() throws Exception {
        tester.startPage(LoginPage.class);
        FormTester formTester = tester.newFormTester("loginForm");
        formTester.setValue("username", "test");
        formTester.setValue("password", "wrongpassword");
        formTester.submit();
        tester.assertRenderedPage(LoginPage.class);
        List<Serializable> messages = tester.getMessages(FeedbackMessage.ERROR);
        assertFalse(messages.isEmpty());
    }

    @Test
    public void testIfHeaderAndFooterIsVisible() {
        tester.startPage(LoginPage.class);
        tester.assertComponent("header", HeaderTemplate.class);
    }

}
