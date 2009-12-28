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

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.openengsb.drools.NotificationDomain;
import org.openengsb.drools.model.Notification;

public class EmailNotifier implements NotificationDomain {

    private Properties props;

    private Authenticator authenticator;

    public EmailNotifier(Properties props) {
        this.props = props;
    }

    public EmailNotifier(Properties props, String user, String password) {
        this.props = props;
        authenticator = new SmtpAuthenticator(user, password);
    }

    public void notify(Notification notification) {
        try {
            Session session = Session.getDefaultInstance(props, authenticator);

            Message msg = new MimeMessage(session);

            InternetAddress addressFrom = new InternetAddress(props.getProperty("mail.smtp.user"));
            msg.setFrom(addressFrom);

            InternetAddress addressTo = new InternetAddress(notification.getRecipient());
            msg.setRecipient(Message.RecipientType.TO, addressTo);

            msg.setSubject(notification.getSubject());
            msg.setContent(notification.getMessage(), "text/plain");

            Transport.send(msg);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private class SmtpAuthenticator extends Authenticator {

        private final String user;
        private final String password;

        public SmtpAuthenticator(String user, String password) {
            this.user = user;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(user, password);
        }
    }
}
