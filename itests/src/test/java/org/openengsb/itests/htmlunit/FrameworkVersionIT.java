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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.itests.util.AbstractPreConfiguredExamTestHelper;
import org.ops4j.pax.exam.junit.ExamReactorStrategy;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.ops4j.pax.exam.spi.reactors.AllConfinedStagedReactorFactory;

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;

import org.osgi.framework.Bundle;

@RunWith(JUnit4TestRunner.class)
@ExamReactorStrategy(AllConfinedStagedReactorFactory.class)
public class FrameworkVersionIT extends AbstractPreConfiguredExamTestHelper
{
    private static final Integer MAX_SLEEP_TIME_IN_SECONDS = 30;

    private WebClient            webClient;
    private String               versionPageUrl;

    @Before
    public void setUp() throws Exception
    {
        webClient = new WebClient();
        String httpport = getConfigProperty("org.ops4j.pax.web",
                "org.osgi.service.http.port");
        versionPageUrl = String.format(
                "http://localhost:%s/system/framework.version.info", httpport);
        waitForSiteToBeAvailable(versionPageUrl, MAX_SLEEP_TIME_IN_SECONDS);
    }

    @After
    public void tearDown() throws Exception
    {
        webClient.closeAllWindows();
        FileUtils.deleteDirectory(new File(getWorkingDirectory()));
    }

    @Test
    public void testIfVersionsMatch_shouldWork() throws Exception
    {
        final TextPage page = webClient.getPage(versionPageUrl);
        String pageVersion = page.getContent().trim().replace("-", ".");
        Bundle[] bundles = getBundleContext().getBundles();
        Bundle infoBundle = null;

        for (int i = 0; i < bundles.length; i++)
        {
            if (bundles[i].getSymbolicName().equals("org.openengsb.framework.info"))
            {
                infoBundle = bundles[i];
                break;
            }
        }
        
        assertNotNull(infoBundle);
        assertEquals(pageVersion, infoBundle.getVersion().toString().trim());
    }
}
