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

package org.openengsb.ui.common.oauth;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.PageParameters;
import org.apache.wicket.RedirectToUrlException;
import org.apache.wicket.Request;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.PopupCloseLink;
import org.apache.wicket.protocol.http.WebRequest;
import org.openengsb.core.api.OAuthData;
import org.openengsb.core.api.OAuthValidation;
import org.openengsb.ui.common.model.OAuthPageFactory;

public class OAuthPage extends WebPage {

    public OAuthPage(OAuthData pageData) throws MalformedURLException {
        add(new PopupCloseLink<String>("close"));
        String currentURL = buildCurrentURL(getRequest());
        String redirectURL = buildRedirectURL(currentURL);

        StringBuffer link = new StringBuffer();
        link.append(pageData.getFirstCallLink());
        link.append("&").append(pageData.getRedirectParameterName()).append("=").append(redirectURL);
        OAuthPageFactory.putOAuthObject(getSession().getId(), pageData);
        throw new RedirectToUrlException(link.toString());
    }

    public OAuthPage(PageParameters pp) throws Exception {
        Request request = getRequest();
        String currentURL = buildCurrentURL(request);
        String redirectURL = buildRedirectURL(currentURL);

        // get redirectURL parameter
        int paramLoc = currentURL.lastIndexOf("&");
        if (paramLoc == -1) {
            paramLoc = currentURL.indexOf("?");
        }
        String receivedParam = currentURL.substring(paramLoc + 1);
        String receivedParamName = receivedParam.substring(0, receivedParam.indexOf("="));

        OAuthData test = OAuthPageFactory.getOAuthObject(getSession().getId());

        OAuthValidation oAuth = new OAuthValidation();
        StringBuilder nextURL = new StringBuilder();

        nextURL.append(test.getSecondCallLink()).append("&").append(test.getRedirectParameterName());
        nextURL.append("=").append(redirectURL).append("&").append(receivedParamName).append("=");
        nextURL.append(request.getParameter("code"));
        String accessToken = oAuth.performOAuthValidation(new URL(nextURL.toString()));
        test.setOutParameter(accessToken);

        OAuthPageFactory.putOAuthObject(getSession().getId(), test);
        add(new Label("oAuthResultLabel", "oAuth authentication successful."));
        add(new PopupCloseLink<String>("close"));
    }

    private String buildCurrentURL(Request request) {
        if (request instanceof WebRequest) {
            HttpServletRequest hsr = ((WebRequest) request).getHttpServletRequest();
            String currentURL = hsr.getRequestURL().toString();
            String queryString = hsr.getQueryString();
            if (queryString != null) {
                currentURL += "?" + queryString;
            }
            return currentURL;
        }
        return null;
    }

    private String buildRedirectURL(String currentURL) {
        if (currentURL == null) {
            return currentURL;
        }
        return currentURL + getRequestCycle().urlFor(getPageMap(), OAuthPage.class, null).toString();
    }
}
