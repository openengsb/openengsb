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

package org.openengsb.itests.htmlunit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class BaseUiInfrastructureIT extends AbstractPreConfiguredExamTestHelper {

    private WebClient webClient;
    private static final Integer MAX_SLEEP_TIME_IN_SECONDS = 30;
    private String loginPageUrl;

    @Before
    public void setUp() throws Exception {
        waitForUserDataInitializer();
        webClient = new WebClient();
        String httpPort = getConfigProperty("org.ops4j.pax.web", "org.osgi.service.http.port");
        loginPageUrl = String.format("http://localhost:%s/openengsb/login", httpPort);
        waitForSiteToBeAvailable(loginPageUrl, MAX_SLEEP_TIME_IN_SECONDS);
    }

    @After
    public void tearDown() throws Exception {
        webClient.closeAllWindows();
        FileUtils.deleteDirectory(new File(getWorkingDirectory()));
    }

    @Test
    public void testIfAllMainNavigationLinksWork_shouldWork() throws Exception {
        final HtmlPage page = webClient.getPage(loginPageUrl);
        HtmlForm form = page.getForms().get(0);
        HtmlSubmitInput loginButton = form.getInputByValue("Login");
        form.getInputByName("username").setValueAttribute("admin");
        form.getInputByName("password").setValueAttribute("password");
        HtmlPage indexPage = loginButton.click();
        assertTrue(indexPage.asText().contains("Welcome to the web based administration of the"
                + " open engineering service bus"));
        HtmlPage testClient = indexPage.getAnchorByText("Test Client").click();
        assertTrue(testClient.asText().contains("Current Project"));
        HtmlPage sendEventpage = testClient.getAnchorByText("Send Event Page").click();
        assertTrue(sendEventpage.asText().contains("Current Project"));
        HtmlPage servicePage = testClient.getAnchorByText("Services").click();
        webClient.waitForBackgroundJavaScript(2000);
        assertThat(servicePage.asText(), containsString("ONLINE"));
        HtmlPage usermanagementPage = testClient.getAnchorByText("User Management").click();
        assertTrue(usermanagementPage.asText().contains("Create new user"));
        HtmlPage taskOverviewPage = testClient.getAnchorByText("Task-Overview").click();
        assertTrue(taskOverviewPage.asText().contains("Task-Overview"));
        HtmlPage wiringPage = testClient.getAnchorByText("Wiring").click();
        assertTrue(wiringPage.asText().contains("Name of the global variable"));
    }

    @Test
    public void testUserLoginWithLimitedAccess_shouldHaveLimitedAccess() throws Exception {
        final HtmlPage page = webClient.getPage(loginPageUrl);
        HtmlForm form = page.getForms().get(0);
        HtmlSubmitInput loginButton = form.getInputByValue("Login");
        form.getInputByName("username").setValueAttribute("user");
        form.getInputByName("password").setValueAttribute("password");
        HtmlPage indexPage = loginButton.click();
        assertTrue(indexPage.asText().contains("Welcome to the web based administration of the"
                + " open engineering service bus"));
        assertFalse(indexPage.asText().contains("User Management"));
    }

    // TODO: OPENENGSB-3286 analyze this test and get it working again.
    @Test
    @Ignore("cannot click button without form")
    public void testCreateAndLoginNewUser_shouldNotShowUserManagementTab() throws Exception {
        String httpPort = getConfigProperty("org.ops4j.pax.web", "org.osgi.service.http.port");
        HtmlPage page = webClient.getPage("http://localhost:" + httpPort + "/openengsb/");
        page = page.getAnchorByText("Login").click();

        HtmlForm form = page.getForms().get(0);
        HtmlSubmitInput loginButton = form.getInputByValue("Login");
        form.getInputByName("username").setValueAttribute("admin");
        form.getInputByName("password").setValueAttribute("password");
        HtmlPage indexPage = loginButton.click();
        assertTrue(indexPage.asText().contains("Welcome to the web based administration of the"
                + " open engineering service bus"));

        HtmlPage usermanagementPage = indexPage.getAnchorByText("User Management").click();

        assertTrue(usermanagementPage.asText().contains("Create new user"));
        form = usermanagementPage.getForms().get(1);
        assertNotNull(form);

        // The create button can not be accessed this way, since it is no longer in the form. More information
        // about the problem here can be read in JIRA.
        HtmlSubmitInput createButton = form.getInputByValue("Create");
        createButton.click();

        // get user creation form:
        form = usermanagementPage.getForms().get(1);
        assertNotNull(form);

        HtmlSubmitInput okButton = form.getInputByValue("OK");
        form.getInputByName("username").setValueAttribute("newUser");
        form.getInputByName("password").setValueAttribute("password");
        form.getInputByName("passwordVerification").setValueAttribute("password");
        indexPage = okButton.click();

        assertTrue(indexPage.asText().contains("newUser"));

        HtmlPage logoutPage = indexPage.getAnchorByText("Logout").click();
        HtmlPage loginPage = logoutPage.getAnchorByText("Login").click();
        form = loginPage.getForms().get(0);
        loginButton = form.getInputByValue("Login");
        form.getInputByName("username").setValueAttribute("newUser");
        form.getInputByName("password").setValueAttribute("password");
        HtmlPage userIndexPage = loginButton.click();
        assertTrue(userIndexPage.asText().contains("Welcome to the web based administration of the"
                + " open engineering service bus"));
        assertFalse(userIndexPage.asText().contains("User Management"));
    }
}
