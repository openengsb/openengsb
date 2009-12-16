/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.email;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.openengsb.drools.NotificationDomain;

public class EMailNotifier implements NotificationDomain {

    private String smtpHost;
    private String sender;
    private String password;

    public EMailNotifier(String smtpHost, String sender, String password) {
        this.smtpHost = smtpHost;
        this.sender = sender;
        this.password = password;
    }

    @Override
    public void notify(String recipient, String subject, String text) {
        try {
            // Set the host smtp address
            Properties props = new Properties();
            props.setProperty("mail.smtp.auth", "true");

            // create some properties and get the default Session
            Session session = Session.getDefaultInstance(props, null);

            // create a message
            Message msg = new MimeMessage(session);

            // set the from and to address
            InternetAddress addressFrom = new InternetAddress(sender);
            msg.setFrom(addressFrom);

            InternetAddress addressTo = new InternetAddress(recipient);
            msg.setRecipient(Message.RecipientType.TO, addressTo);

            // Setting the Subject and Content Type
            msg.setSubject(subject);
            msg.setContent(text, "text/plain");

            Transport tr = session.getTransport("smtp");
            tr.connect(smtpHost, sender, password);
            msg.saveChanges(); // don't forget this
            tr.sendMessage(msg, msg.getAllRecipients());
            tr.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
