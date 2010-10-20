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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.integrationtest.util.AbstractExamTestHelper;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@RunWith(JUnit4TestRunner.class)
public class DemoTestToBeRemovedIT extends AbstractExamTestHelper {

    @Test
    public void homePage() throws Exception {
        final WebClient webClient = new WebClient();
        final HtmlPage page = webClient.getPage("http://htmlunit.sourceforge.net");
        assertEquals("HtmlUnit - Welcome to HtmlUnit", page.getTitleText());

        final String pageAsXml = page.asXml();
        assertTrue(pageAsXml.contains("<body class=\"composite\">"));

        final String pageAsText = page.asText();
        assertTrue(pageAsText.contains("Support for the HTTP and HTTPS protocols"));

        webClient.closeAllWindows();
    }

}
