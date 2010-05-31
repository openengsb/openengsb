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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.facebook.common.exceptions.FacebookConnectorException;



public class FacebookConnectorImpl implements FacebookConnector {
    private Log log = LogFactory.getLog(FacebookConnectorImpl.class);
    private FacebookLoginImpl login;
    private FacebookJaxbRestClient client;
    private FacebookClientImpl facebookClient;

    FacebookConnectorImpl(String email, String password, String API_KEY, String SECRET) {

        try {
            login = new FacebookLoginImpl();
            
            login.loginUser(email, password, API_KEY);
            client = login.createLoggedInFacebookClient(API_KEY, SECRET);
        } catch (FacebookException e) {
            handleFacebookException(e);
        } catch (FacebookConnectorException e) {
            handleFacebookException(e);
        }
        facebookClient = new FacebookClientImpl(client);
    }

    @Override
    public void updateStatus(String message) {
        try {
            facebookClient.updateStatus(message);
        } catch (FacebookException e) {
            handleFacebookException(e);
        }
    }


    private void handleFacebookException(FacebookException e) {
        switch (e.getCode()) {
            case -1:
                log.error("HTTP - error on login, wrong password or username");
                break;
            default:
                log.error("Action failed. Cause: " + e.getMessage() +" " +e.getCode() + " " );
                e.printStackTrace();
                break;
        }
    }


    private void handleFacebookException(FacebookConnectorException e) {
        switch (e.getStatusCode()) {
            case -1:
                log.error("HTTP - error befor login");
                break;
            case -2:
                log.error("Incorrect or missing username or password. Authentication failed.");
                break;
            default:
                log.error("Action failed. Cause: " + e.getMessage());
                break;
        }
    }
}
