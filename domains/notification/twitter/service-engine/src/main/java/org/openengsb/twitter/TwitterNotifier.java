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

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.openengsb.drools.NotificationDomain;
import org.openengsb.drools.model.Notification;
import org.openengsb.twitter.common.Twitter4JTwitterConnector;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class TwitterNotifier implements NotificationDomain {

    private Properties props;
    private Twitter4JTwitterConnector twitterCon;
    private Twitter twitter;

    public TwitterNotifier(Properties props) {
        this.props = props;
    }

    public TwitterNotifier(String user, String password) {
    	twitterCon = new Twitter4JTwitterConnector();
    	twitter = new TwitterFactory().getInstance(user, password);
    	twitterCon.setTwitter(twitter);
    }

    public void notify(Notification notification) {
    	
    	if(notification.getAttachments().length>0){
    		//Has Attachments --> zip, upload, shortenurl + attach url to message
    	}
    	
    	if(notification.getRecipient().length()>0){
    		//got Recipient --> Direct Message
    		twitterCon.sendMessage(notification.getRecipient(), notification.getMessage());
    	}else {
    		twitterCon.updateStatus(notification.getMessage());
    	}
    }
}
