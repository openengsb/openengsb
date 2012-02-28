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

package org.openengsb.core.api.xlink;

import java.util.List;

// @extract-start XLinkTemplate
/**
 * Modelclass for the XLinkTemplate definitions. Transfered to each Tool that participates in XLinking. Defines the
 * baseURL to the XLink HTTP-Servlet and a list of Keynames used to identify modelObjects. To create valid XLink-URLs,
 * the KeyNames with their values must be concatenated to the baseURL as GET-Parameters.
 */
public class XLinkTemplate {

    /**
     * URL to the Registry´s HTTP-Servlet without the IdentifierÂ´s fields. Already contains the modelId of the
     * sourceModel and the expirationDate of the Link as GET-Parameters. XLink-URLs expire after a certain amount of
     * days.
     */
    String baseUrl;

    /**
     * List containing keynames that determine how single modelObjects are identified. Must be concatenated to the
     * baseUrl as GET-Parameters.
     */
    List<String> keyNames;

    public XLinkTemplate() {
    }

    public XLinkTemplate(String baseUrl, List<String> keyNames) {
        this.baseUrl = baseUrl;
        this.keyNames = keyNames;
    }

    /**
     * URL to the Registry´s HTTP-Servlet without the Identifier Fields as Parameters
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * List containing keynames that determine how single modelObjects are identified. Must be concatenated to the
     * baseUrl as GET-Parameters.
     */
    public List<String> getKeyNames() {
        return keyNames;
    }

    public void setKeyNames(List<String> keyNames) {
        this.keyNames = keyNames;
    }

}
// @extract-end
