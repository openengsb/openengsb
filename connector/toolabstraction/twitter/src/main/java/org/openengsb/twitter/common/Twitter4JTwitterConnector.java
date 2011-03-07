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

package org.openengsb.twitter.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class Twitter4JTwitterConnector implements TwitterConnector {
    private Log log = LogFactory.getLog(getClass());

    private Twitter twitter;

    @Override
    public void updateStatus(String status) {
        try {
            twitter.updateStatus(status);
            log.info("Successfully updated user status.");
        } catch (TwitterException e) {
            handleTwitterException(e);
        }
    }

    @Override
    public void sendMessage(String receiver, String message) {
        try {
            twitter.sendDirectMessage(receiver, message);
            log.info("Successfully sent message to " + receiver + ".");
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

    public void setTwitter(Twitter twitter) {
        this.twitter = twitter;
    }
}
