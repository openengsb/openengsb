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


import com.google.code.facebookapi.*;
import com.google.code.facebookapi.schema.FqlQueryResponse;
import com.google.code.facebookapi.schema.FriendsGetResponse;
import com.google.code.facebookapi.schema.User;
import com.google.code.facebookapi.schema.UsersGetInfoResponse;
import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import sun.font.AttributeMap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.*;

import java.net.URL;
import java.io.*;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.w3c.dom.Node;


public class FacebookClientImpl implements FacebookClient {

    private Log log = LogFactory.getLog(FacebookClientImpl.class);

    private FacebookJaxbRestClient client;


    FacebookClientImpl(FacebookJaxbRestClient client) {
        this.client = client;
    }


    @Override
    public boolean updateStatus(String message) throws FacebookException {
        boolean success = client.users_setStatus(message);
        return success;
    }

    @Override
    public String publishToWall(String message) throws FacebookException {
        return client.stream_publish(message, null, new ArrayList<BundleActionLink>(), null, client.getCacheUserId());
    }

    @Override
    public Long getLoggedInUserID() throws FacebookException {
        Long userId = client.users_getLoggedInUser();
        return userId;

    }

    @Override
    public List<User> getFriends() throws FacebookException {

        client.friends_get();
        FriendsGetResponse response = (FriendsGetResponse) client.getResponsePOJO();
        List<Long> friends = response.getUid();


        client.users_getInfo(friends, EnumSet.of(ProfileField.NAME));

        UsersGetInfoResponse userResponse = (UsersGetInfoResponse) client.getResponsePOJO();//RepsonsePOJO();

        List<User> users = userResponse.getUser();

        return users;
    }

    @Override
    public Map<String, String> getStream() throws FacebookException {
        Map<String, String> result = new HashMap<String, String>();
        long id = client.users_getLoggedInUser();  

        String fql = "SELECT post_id, actor_id, message FROM stream WHERE source_id = " + id + " limit 50";
        Object obj = client.fql_query(fql);

        FqlQueryResponse jobj = (FqlQueryResponse) obj;

        for (Object res : jobj.getResults()) {
            ElementNSImpl el = (ElementNSImpl) res;

            Node node = el.getFirstChild();
            while (node != null) {
                result.put(node.getNodeName(), node.getTextContent());
                log.info(node.getNodeName() + ": " + node.getTextContent());
                node = node.getNextSibling();
            }
        }


        return result;
    }

}