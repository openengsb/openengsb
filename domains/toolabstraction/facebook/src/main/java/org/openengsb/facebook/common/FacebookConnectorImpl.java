/**

 Copyright 2010 OpenEngSB Division, Vienna University of Technology

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package org.openengsb.facebook.common;

import com.google.code.facebookapi.FacebookException;
import com.google.code.facebookapi.FacebookJaxbRestClient;
import com.google.code.facebookapi.FacebookXmlRestClient;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FacebookConnectorImpl implements FacebookConnector {
    private Log log = LogFactory.getLog(FacebookConnectorImpl.class);

    private FacebookJaxbRestClient facebookClient;
    private String secret;
    private String email;
    private String password;
    private String api;
    private static final String FACEBOOK_LOGIN = "http://www.facebook.com/login.php";
    private static final String FACEBOOK_LOGOUT = "http://www.facebook.com/logout.php";
    private static final int MAXCHAR = 419;
    private String session;

    FacebookConnectorImpl(String email, String password, String API_KEY, String SECRET) {
        this.email = email;
        this.password = password;
        this.api = API_KEY;
        this.secret = SECRET;
    }

    private FacebookJaxbRestClient login() throws IOException, FacebookException {
        log.debug("Connection started");
        facebookClient = new FacebookJaxbRestClient(api, secret);
        String token = facebookClient.auth_createToken();

        HttpClient http = new HttpClient();
        http.setParams(new HttpClientParams());
        http.setState(new HttpState());

        GetMethod get = new GetMethod(FACEBOOK_LOGIN + "?api_key=" + api + "&v=1.0&auth_token=" + token);

        http.executeMethod(get);

        PostMethod post = new PostMethod(FACEBOOK_LOGIN);
        post.addParameter(new org.apache.commons.httpclient.NameValuePair("api_key", api));
        post.addParameter(new org.apache.commons.httpclient.NameValuePair("v", "1.0"));
        post.addParameter(new org.apache.commons.httpclient.NameValuePair("auth_token", token));
        post.addParameter(new org.apache.commons.httpclient.NameValuePair("email", email));
        post.addParameter(new org.apache.commons.httpclient.NameValuePair("pass", password));
        http.executeMethod(post);


        session = facebookClient.auth_getSession(token);
        FacebookXmlRestClient xmlClient = new FacebookXmlRestClient(api, secret, session);//just necessary don't know why
        log.debug("Session key is " + session);
        return facebookClient;
    }

    private void logout() {
        try {
            HttpClient http = new HttpClient();
            http.setParams(new HttpClientParams());
            http.setState(new HttpState());
            GetMethod get = new GetMethod(FACEBOOK_LOGOUT + "?api_key=" + api + "&ession_key=" + session);
            http.executeMethod(get);
        } catch (IOException e) {
            //nevermind
        }
    }

    @Override
    public void updateStatus(String message) {
        try {
            if (facebookClient == null) {
                facebookClient = login();
            } else {
                try {
                    String token = facebookClient.auth_createToken();
                    String session = facebookClient.auth_getSession(token);
                    FacebookXmlRestClient xmlClient = new FacebookXmlRestClient(api, secret, session);
                    xmlClient.users_setStatus(message);
                } catch (FacebookException e) {
                    facebookClient = login();
                    facebookClient.users_setStatus(message);
                }
                return;
            }
            List<String> messages = new ArrayList<String>();

            if (message.length() >= MAXCHAR) {
                int i = 0;
                while (i + MAXCHAR < message.length()) {
                    messages.add(message.substring(i, i + MAXCHAR));
                    i = i + (MAXCHAR);
                }
                messages.add(message.substring(i));
                for (String s : messages) {
                    facebookClient.users_setStatus(s);
                }
            } else {
                facebookClient.users_setStatus(message);
            }
        } catch (FacebookException e) {
            handleFacebookException(e);
        } catch (IOException e) {
            try {
                throw new FacebookException(-3, e.getMessage());
            } catch (FacebookException e1) {
                handleFacebookException(e1);
            }
        } finally {
            logout();

        }
    }

    private void handleFacebookException(FacebookException e) {
        switch (e.getCode()) {
            case -1:
                log.error("HTTP - error on login, wrong password or username");
                break;
            case -3:
                log.error("IO - Exception while sending");
                break;
            case 102:
                log.error("Session key invalid or no longer valid");
                break;
            default:
                log.error("Action failed. Cause: " + e.getMessage() + " " + e.getCode() + " ");
                e.printStackTrace();
                break;
        }
    }

}
