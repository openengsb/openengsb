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
package org.openengsb.email;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.util.jaf.ByteArrayDataSource;
import org.openengsb.drools.NotificationDomain;
import org.openengsb.drools.model.Attachment;
import org.openengsb.drools.model.Notification;

public class EmailNotifier implements NotificationDomain {

    private Log log = LogFactory.getLog(getClass());

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

            // create the message part
            MimeBodyPart messageBodyPart = new MimeBodyPart();

            // fill in message
            messageBodyPart.setText(notification.getMessage());

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            for (Attachment attachment : notification.getAttachments()) {
                // Part two is attachment
                messageBodyPart = new MimeBodyPart();
                DataSource source = new ByteArrayDataSource(attachment.getData(), attachment.getType());
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(attachment.getName());
                multipart.addBodyPart(messageBodyPart);
            }

            // Put parts in message
            msg.setContent(multipart);
            Transport.send(msg);
        } catch (Exception e) {
            log.error("Exception on sending email notification.", e);
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
