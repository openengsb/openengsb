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
package org.openengsb.domains.notification.email.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.domains.notification.implementation.NotificationDomain;
import org.openengsb.domains.notification.implementation.model.Attachment;
import org.openengsb.domains.notification.implementation.model.Notification;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.util.Properties;

public class EmailNotifier implements NotificationDomain {

    private Log log = LogFactory.getLog(getClass());
    private final String id;
    private Authenticator authenticator;

    private String user;
    private String password;
    private String smtpAuth;
    private String smtpUser;
    private String smtpStarttls;
    private String smtpHost;


    public EmailNotifier(String id) {
        this.id = id;
        authenticator = new SmtpAuthenticator(user, password);
    }

    @Override
    public void notify(Notification notification) {
        log.info("Sending notification with notification connector.");
        try {
            Properties properties = createProperties();

            Session session = Session.getDefaultInstance(properties, authenticator);
            session.setDebug(true);

            Message msg = new MimeMessage(session);
            
            InternetAddress addressFrom = new InternetAddress(properties.getProperty("mail.smtp.user"));
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
            log.error("Exception on sending notification notification.", e);
        }
    }

    private Properties createProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.starttls.enable", smtpStarttls);
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.user", smtpUser);
        props.put("mail.smtp.password", password);
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", 465);
        return props;

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

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getUser() {
        return user;
    }

    public String getSmtpAuth() {
        return smtpAuth;
    }

    public void setSmtpAuth(String smtpAuth) {
        this.smtpAuth = smtpAuth;
    }

    public String getSmtpUser() {
        return smtpUser;
    }

    public void setSmtpUser(String smtpUser) {
        this.smtpUser = smtpUser;
    }

    public String getSmtpStarttls() {
        return smtpStarttls;
    }

    public void setSmtpStarttls(String smtpStarttls) {
        this.smtpStarttls = smtpStarttls;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }
}
