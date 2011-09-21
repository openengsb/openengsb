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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.apache.wicket.PageParameters;
import org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.PopupCloseLink;
import org.apache.wicket.model.StringResourceModel;
import org.openengsb.core.api.oauth.OAuthData;
import org.openengsb.ui.common.model.OAuthPageFactory;
import org.ops4j.pax.wicket.api.PaxWicketMountPoint;

/**
 * This WebPage is a help page to enable the possibility of OAuth authorization. When a OAuth authorization begins, the
 * redirect url has to be set to this page. It handles the authorization process and prints the result token of the
 * authorization to the page. The user has to copy&paste this token to the service properties.
 */
@AuthorizeInstantiation("ROLE_USER")
@PaxWicketMountPoint(mountPoint = "oauth")
public class OAuthPage extends WebPage {

    public OAuthPage() {
        String notStarted = new StringResourceModel("oAuth.notStarted", this, null).getString();
        add(new PopupCloseLink<String>("close"));
        add(new Label("oAuthResultLabel", notStarted));
    }

    public OAuthPage(PageParameters pp) throws Exception {
        OAuthData oauth = OAuthPageFactory.getOAuthObject(getSession().getId());
        if (oauth != null) {
            String intermediate = oauth.getIntermediateParameterName();
            if (pp.containsKey(intermediate)) {
                String code = pp.getString(intermediate);
                OAuthData data = OAuthPageFactory.getOAuthObject(getSession().getId());
                data.addEntryToSecondParams(intermediate, code);

                String accessToken = performOAuthValidation(new URL(data.generateSecondCallLink()));
                OAuthPageFactory.removeOAuthObject(getSession().getId());

                String successful = new StringResourceModel("oAuth.Successful", this, null).getString();
                add(new Label("oAuthResultLabel", successful + accessToken));
                add(new PopupCloseLink<String>("close"));
                return;
            }
        }
        String failed = new StringResourceModel("oAuth.Failed", this, null).getString();
        add(new Label("oAuthResultLabel", failed));
        add(new PopupCloseLink<String>("close"));
    }

    private String performOAuthValidation(URL url) throws Exception {
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuffer output = new StringBuffer();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            output.append(inputLine);
        }
        in.close();
        String[] result = output.toString().split("=");
        if (result.length > 1) {
            return result[1];
        } else {
            return result[0];
        }
    }
}
