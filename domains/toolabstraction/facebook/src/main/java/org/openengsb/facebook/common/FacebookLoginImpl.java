/**

 Copyright 2010 OpenEngSB Division, Vienna University of Technology

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE\-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package org.openengsb.facebook.common;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.google.code.facebookapi.FacebookException;
import com.google.code.facebookapi.FacebookJaxbRestClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

public class FacebookLoginImpl implements FacebookLogin {

    private Log log = LogFactory.getLog(FacebookLoginImpl.class);


    private DefaultHttpClient httpClient;
    private HttpResponse response;
    private HttpEntity entity;
    private FacebookJaxbRestClient facebookClient;
    private static final String FACEBOOK_LOGIN = "http://www.facebook.com/login.php";

    FacebookLoginImpl() throws FacebookException {
        initHttpClient();
    }


    @Override
    public boolean loginUser(String email, String password, String api) throws FacebookException {
        try {
            if (httpClient == null) {
                initHttpClient();
            }
            HttpPost httpost = new HttpPost(FACEBOOK_LOGIN);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("email", email));
            nvps.add(new BasicNameValuePair("pass", password));
            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            response = httpClient.execute(httpost);
            entity = response.getEntity();

            log.info("Login form get: " + response.getStatusLine());
            if (entity != null) {
                entity.consumeContent();
            }

            log.info("Post logon cookies:");
            List<Cookie> cookies = httpClient.getCookieStore().getCookies();

            return checkCookies(cookies);

        } catch (Exception e) {
            throw new FacebookException(-1, "Error on login ");
        }
    }


    @Override
    public void closeHttpClient() {
        if (httpClient != null) {
            httpClient.getConnectionManager().shutdown();
        }
    }


    private void initHttpClient() throws FacebookException {
        try {
            httpClient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(FACEBOOK_LOGIN);

            response = httpClient.execute(httpget);
            entity = response.getEntity();


            if (entity != null) {
                entity.consumeContent();
            }

            List<Cookie> cookies = httpClient.getCookieStore().getCookies();
            if (cookies.isEmpty()) {
                log.info("None cookies found");
            } else {
                log.info("cookies found");
            }
        }
        catch (Exception e) {
            throw new FacebookException(-1, "Error on login");
        }
    }


    @Override
    public FacebookJaxbRestClient createLoggedInFacebookClient(String api_key, String secret) throws FacebookException {

        facebookClient = new FacebookJaxbRestClient(api_key, secret);
        String sessionKey = getSessionKey(api_key);

        String tempSecret = facebookClient.getCacheSessionSecret();
        facebookClient = new FacebookJaxbRestClient(api_key, tempSecret, sessionKey);

        return facebookClient;

    }


    private boolean checkCookies(List<Cookie> cookies) {

        if (cookies.isEmpty()) {
            log.info("None cookies found");
            return false;
        } else {
            for (int i = 0; i < cookies.size(); i++) {
                log.info("- " + cookies.get(i).toString());
                if ("reg_fb_ref".equals(cookies.get(i).getName()) && "http%3A%2F%2Fwww.facebook.com%2Flogin.php".equals(cookies.get(i).getValue())) {
                    return false;
                } else if ("sct".equals(cookies.get(i).getName())) { // contains session cookies
                    return true;
                }
            }
        }
        return false;
    }


    private String getSessionKey(String api_key) throws FacebookException {

        try {

            String token = facebookClient.auth_createToken();
            HttpPost httpost = new HttpPost(FACEBOOK_LOGIN);
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("api_key", api_key));
            nvps.add(new BasicNameValuePair("v", "1.0"));
            nvps.add(new BasicNameValuePair("auth_token", token));
            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            response = httpClient.execute(httpost);
            entity = response.getEntity();


            if (entity != null) {
                entity.consumeContent();
            }

            List<Cookie> cookies = httpClient.getCookieStore().getCookies();

            if (checkCookies(cookies)) {
                return facebookClient.auth_getSession(token, true);
            }

        } catch (Exception ex) {
            throw new FacebookException(-1, "Could not get Session key for user");
        }

        throw new FacebookException(-1, "Could not get Session key for user");
    }


}
