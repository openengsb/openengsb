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

package org.openengsb.core.common.xlink;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import org.openengsb.core.api.xlink.XLinkTemplate;

// @extract-start XLinkUtilsTest
public class XLinkUtilsTest {

    private String servletUrl = "http://openengsb.org/registryServlet.html";
    private String modelId = "OOSourceCodeDomainId";
    private int expiresInDays = 3;
    private List<String> identifierKeyNames = Arrays.asList("methodName", "className", "packageName");

    @Test
    public void testPrepareXLinkTemplate() {
        XLinkTemplate xLinkTemplate =
            XLinkUtils.prepareXLinkTemplate(servletUrl, modelId, identifierKeyNames, expiresInDays);
        // xLinkTemplate.getBaseUrl() =
        // http://openengsb.org/registryServlet.html?modelId=OOSourceCodeDomain&expirationDate=20120302121927

        assertTrue(xLinkTemplate.getBaseUrl().contains(XLinkUtils.XLINK_MODELID_KEY + "=" + modelId));
        assertTrue(xLinkTemplate.getKeyNames().contains("methodName"));
    }

    @Test
    public void testGenerateValidXLinkUrl() {
        XLinkTemplate xLinkTemplate =
            XLinkUtils.prepareXLinkTemplate(servletUrl, modelId, identifierKeyNames, expiresInDays);
        List<String> values = Arrays.asList("testMethod", "testClass", "testPackage");

        String xLinkUrl = XLinkUtils.generateValidXLinkUrl(xLinkTemplate, values);
        // xLinkUrl =
        // http://openengsb.org/registryServlet.html?modelId=OOSourceCodeDomain&expirationDate=20120302121927&
        // methodName=testMethod&className=testClass&packageName=testPackage

        assertTrue(xLinkUrl.contains("methodName=testMethod"));
        assertTrue(xLinkUrl.contains("className=testClass"));
        assertTrue(xLinkUrl.contains("packageName=testPackage"));

    }

}
// @extract-end
