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
import org.openengsb.domains.notification.implementation.model.Notification;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailNotifier implements NotificationDomain {

    private Log log = LogFactory.getLog(getClass());
    private final String id;
    private Authenticator authenticator;

    private String user;
    private String password;
    private String smtpAuth;
    private String smtpUser;
    private String smtpHost;
    private String smtpPort;

    public EmailNotifier(String id) {
        this.id = id;
    }

    @Override
    public void notify(Notification notification) {
        log.info("Sending notification with notification connector " + id + ".");
        try {

            notifyWithoutExceptionHandling(notification);

        } catch (MessagingException e) {
            log.error("Exception on sending notification notification.", e);
        }
    }

    public void notifyWithoutExceptionHandling(Notification notification) throws MessagingException {
        Properties props = createProperties();

        Session session = Session.getDefaultInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(smtpUser));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(notification.getRecipient()));
        message.setSubject(notification.getSubject());
        message.setText(notification.getMessage());

        Transport.send(message);
    }

    private Properties createProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.socketFactory.port", smtpPort);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.port", smtpPort);

        return props;

    }



    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public void setSmtpAuth(String smtpAuth) {
        this.smtpAuth = smtpAuth;
    }


    public void setSmtpUser(String smtpUser) {
        this.smtpUser = smtpUser;
    }


    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }


    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }
}
