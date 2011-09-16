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

package org.openengsb.core.api.oauth;

import java.io.Serializable;
import java.util.Map;

/**
 * Object containing the data required to be transfered for an OAuth Request
 */
public class OAuthData implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, String> firstURLParameters;
    private Map<String, String> secondURLParameters;
    private String firstURL;
    private String secondURL;
    private String redirectParameterName;
    private String intermediateName;

    /**
     * firstURLParameters - the parameters which should be extended via ?key=value to the first URL secondURLParameters
     * - the parameters which should be extended via ?key=value to the second URL firstURL - the URL where the request
     * for the code have to be sent to. The code is needed in the second URL call secondURL - the URL to send the
     * request to get a user token redirectParameterName - the name of the parameter to redirect the code or token to
     * intermediateParameterName - the name of the code value in the result of the first URL
     */
    public OAuthData(Map<String, String> firstURLParameters, Map<String, String> secondURLParameters,
            String firstURL, String secondURL, String redirectParameterName, String intermediateParameterName) {
        this.firstURLParameters = firstURLParameters;
        this.secondURLParameters = secondURLParameters;
        this.firstURL = firstURL;
        this.secondURL = secondURL;
        this.redirectParameterName = redirectParameterName;
        this.intermediateName = intermediateParameterName;
    }

    public void setRedirectURL(String redirectUrl) {
        firstURLParameters.put(redirectParameterName, redirectUrl);
        secondURLParameters.put(redirectParameterName, redirectUrl);
    }

    public void addEntryToFirstParams(String key, String value) {
        firstURLParameters.put(key, value);
    }

    public void addEntryToSecondParams(String key, String value) {
        secondURLParameters.put(key, value);
    }

    public String getIntermediateParameterName() {
        return intermediateName;
    }

    public String generateFirstCallLink() {
        StringBuilder builder = new StringBuilder();
        if (!firstURL.endsWith("?")) {
            builder.append(firstURL).append("?");
        }
        builder.append(generateParamString(firstURLParameters));
        return builder.toString();
    }

    public String generateSecondCallLink() {
        StringBuilder builder = new StringBuilder();
        if (!secondURL.endsWith("?")) {
            builder.append(secondURL).append("?");
        }
        builder.append(generateParamString(secondURLParameters));
        return builder.toString();
    }

    private String generateParamString(Map<String, String> params) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (builder.length() != 0) {
                builder.append("&");
            }
            builder.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return builder.toString();
    }
}
