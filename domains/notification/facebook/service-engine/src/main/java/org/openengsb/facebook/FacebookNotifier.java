/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.facebook;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.drools.NotificationDomain;
import org.openengsb.drools.model.Attachment;
import org.openengsb.drools.model.Notification;
import org.openengsb.facebook.common.FacebookConnector;

import java.io.IOException;
import java.net.URL;


public class FacebookNotifier implements NotificationDomain {

    private Log log = LogFactory.getLog(FacebookNotifier.class);
    FacebookConnector facebookConnector;
    private final int maxChars = 420;

    @Override
    public void notify(Notification notification) {
        if (notification.getMessage().length() > maxChars) {
            log.warn("Your message is going to be truncated.");
        }
        if (notification.getRecipient() == null || notification.getRecipient().equals("")) {
            facebookConnector.updateStatus(notification.getMessage());
        } else {
            // TODO: add recipient
        }
    }

    public void setFacebookConnector(FacebookConnector facebookConnector) {
        this.facebookConnector = facebookConnector;
    }
}
