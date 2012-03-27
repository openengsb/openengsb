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

import org.openengsb.core.api.xlink.XLinkRegisteredTools;
import org.openengsb.core.api.xlink.XLinkTemplate;

public class XLinkUtilsTest {

    // @extract-start XLinkUtilsTestConfigs

    private String servletUrl = "http://openengsb.org/registryServlet.html";
    private String contextId = "ExampleContext";
    private String version = "1.0";
    private String modelId = "OOSourceCodeDomainId";
    private int expiresInDays = 3;
    private List<String> identifierKeyNames = Arrays.asList("methodName", "className", "packageName");
    private List<XLinkRegisteredTools> registeredTools = null;
    private String connectorId = "exampleConnectorId";
    private String viewId = "exampleViewId";

    // @extract-end

    // @extract-start XLinkUtilsTestPrepareTemplate
    @Test
    public void testPrepareXLinkTemplate() {
        XLinkTemplate xLinkTemplate =
            XLinkUtils.prepareXLinkTemplate(servletUrl, contextId, version, modelId, identifierKeyNames, expiresInDays,
                registeredTools);

        // baseUrl =
        // http://openengsb.org/registryServlet.html?contextId=ExampleProject&versionId=1.0
        // &modelId=OOSourceCodeDomainId&expirationDate=20120305181636

        assertTrue(xLinkTemplate.getBaseUrl().contains(XLinkUtils.XLINK_MODELID_KEY + "=" + modelId));
        assertTrue(xLinkTemplate.getKeyNames().contains("methodName"));
    }

    // @extract-end

    // @extract-start XLinkUtilsTestGenerateValidXLinkUrl
    @Test
    public void testGenerateValidXLinkUrl() {
        XLinkTemplate xLinkTemplate =
            XLinkUtils.prepareXLinkTemplate(servletUrl, contextId, version, modelId, identifierKeyNames, expiresInDays,
                registeredTools);
        List<String> values = Arrays.asList("testMethod", "testClass", "testPackage");

        String xLinkUrl = XLinkUtils.generateValidXLinkUrl(xLinkTemplate, values);

        // xLinkUrl =
        // http://openengsb.org/registryServlet.html?contextId=ExampleContext&versionId=1.0
        // &modelId=OOSourceCodeDomainId&expirationDate=20120305181636&methodName=testMethod
        // &className=testClass&packageName=testPackage

        assertTrue(xLinkUrl.contains("methodName=testMethod"));
        assertTrue(xLinkUrl.contains("className=testClass"));
        assertTrue(xLinkUrl.contains("packageName=testPackage"));

    }

    // @extract-end

    // @extract-start XLinkUtilsTestGenerateValidXLinkUrlLocalSwitching
    @Test
    public void testGenerateValidXLinkUrlForLocalSwitching() {
        XLinkTemplate xLinkTemplate =
            XLinkUtils.prepareXLinkTemplate(servletUrl, contextId, version, modelId, identifierKeyNames, expiresInDays,
                registeredTools);
        List<String> values = Arrays.asList("testMethod", "testClass", "testPackage");
        String xLinkUrl = XLinkUtils.generateValidXLinkUrlForLocalSwitching(xLinkTemplate, values, connectorId, viewId);

        // xLinkUrl =
        // http://openengsb.org/registryServlet.html?contextId=ExampleContext&versionId=1.0
        // &modelId=OOSourceCodeDomainId&expirationDate=20120305181636&methodName=testMethod
        // &className=testClass&packageName=testPackage&connectorID=exampleConnectorId&viewId=exampleViewId

        assertTrue(xLinkUrl.contains(XLinkUtils.XLINK_CONNECTORID_KEY + "=" + connectorId));
        assertTrue(xLinkUrl.contains(XLinkUtils.XLINK_VIEW_KEY + "=" + viewId));

    }
    // @extract-end

}
