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

import java.util.Properties;

import org.openengsb.drools.NotificationDomain;
import org.openengsb.drools.model.Notification;
import org.openengsb.twitter.common.TwitterConnector;


public class TwitterNotifier implements NotificationDomain {
    private Properties props;
    private TwitterConnector twitterCon;
    
	public TwitterNotifier(){
		
	}
	
    public TwitterNotifier(Properties props) {
        this.props = props;
    }

    public void notify(Notification notification) {

        if (notification.getAttachments().length > 0) {
            // Has Attachments --> zip, upload, shortenurl + attach url to
            // message
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
}
