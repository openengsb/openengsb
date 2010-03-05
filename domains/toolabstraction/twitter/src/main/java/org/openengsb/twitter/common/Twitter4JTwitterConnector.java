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
package org.openengsb.twitter.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class Twitter4JTwitterConnector implements TwitterConnector {
    private Log log = LogFactory.getLog(getClass());

    private String username;
    private String password;
    
    private Twitter twitter;
    
    public void init()
    {
        twitter = new TwitterFactory().getInstance(username, password);
    }

    @Override
    public void updateStatus(String message) {
        try {
            twitter.updateStatus(message);
        } catch (TwitterException e) {
            handleTwitterException(e);
        }
    }

    @Override
    public void sendMessage(String receiver, String message) {
        try {
            twitter.sendDirectMessage(receiver, message);
        } catch (TwitterException e) {
            handleTwitterException(e);
        }
    }

    private void handleTwitterException(TwitterException e) {
        switch (e.getStatusCode()) {
        case -1:
            log.error("Twitter-API is currently not available. Action failed.");
            break;
        case 401:
            log.error("Incorrect or missing username or password. Authentication failed.");
            break;
        case 404:
            log.error("Unknown receiver for the message. Transmission failed.");
            break;
        default:
            log.error("Action failed. Cause: " + e.getMessage());
            break;
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
