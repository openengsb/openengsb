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
package org.openengsb.twitter;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.util.FileUpload;
import org.openengsb.drools.NotificationDomain;
import org.openengsb.drools.model.Notification;
import org.openengsb.twitter.common.TwitterConnector;
import org.openengsb.twitter.common.util.UrlShortenerUtil;
import org.openengsb.twitter.common.util.ZipUtil;

public class TwitterNotifier implements NotificationDomain {
    private Log log = LogFactory.getLog(getClass());
    private TwitterConnector twitterCon;
    private FileUpload fileUpload;

    public void notify(Notification notification) {
        if (notification.getAttachments().length > 0) {
            try {
                byte[] zip = ZipUtil.zipAttachments(notification.getAttachments());
                URL url = fileUpload.uploadFile(zip, "zip");
                String shortUrl = UrlShortenerUtil.getTinyUrl(url);

                notification.setMessage("Attachments: " + shortUrl + "\n" + notification.getMessage());
                log.info("Attachments successfully added.");
            } catch (IOException e) {
                log.error("Error creating ZIP-file. Attachments will be skipped.");
            }
        }

        if (notification.getRecipient() == null || notification.getRecipient().equals("")) {
            twitterCon.updateStatus(notification.getMessage());

        } else {
            // got Recipient --> Direct Message
            twitterCon.sendMessage(notification.getRecipient(), notification.getMessage());
        }
    }

    public void setTwitterCon(TwitterConnector twitterCon) {
        this.twitterCon = twitterCon;
    }

    public void setFileUpload(FileUpload fileUpload) {
        this.fileUpload = fileUpload;
    }
}
