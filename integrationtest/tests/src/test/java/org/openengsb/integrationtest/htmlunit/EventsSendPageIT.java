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

package org.openengsb.integrationtest.htmlunit;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.integrationtest.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

@RunWith(JUnit4TestRunner.class)
public class EventsSendPageIT extends AbstractExamTestHelper {

    private WebClient webClient;
    private HtmlPage indexPage;

    @Before
    public void login() throws Exception {
        final WebClient webClient = new WebClient();
        String loginPageEntryUrl =
            "http://localhost:8090/openengsb/?wicket:bookmarkablePage=:org.openengsb.ui.web.LoginPage";
        final HtmlPage page = webClient.getPage(loginPageEntryUrl);
        HtmlForm form = page.getForms().get(0);
        HtmlSubmitInput loginButton = form.getInputByValue("Login");
        form.getInputByName("username").setValueAttribute("admin");
        form.getInputByName("password").setValueAttribute("password");
        indexPage = loginButton.click();
        assertTrue(indexPage.asText().contains("This page represents"));
    }

    @Test
    public void testAuditShownInTestEvent() throws Exception {
        HtmlPage sendEventpage = indexPage.getAnchorByText("Send Event Page").click();
        HtmlForm form = sendEventpage.getForms().get(0);
        HtmlSubmitInput sendButton = form.getInputByValue("Send");
        form.getInputByName("fieldContainer:fields:1:row:field").setValueAttribute("test");
        sendButton.click();
        sendEventpage = sendEventpage.getAnchorByText("Send Event Page").click();
        assertTrue(sendEventpage.asText().contains(
            "Event Properties => class:class org.openengsb.core.common.Event; name:asdfasdfasdf; type:Event;"));
        webClient.closeAllWindows();
    }
}
