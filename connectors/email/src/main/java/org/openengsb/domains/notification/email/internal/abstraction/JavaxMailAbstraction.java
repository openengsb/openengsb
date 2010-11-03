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

package org.openengsb.domains.notification.email.internal.abstraction;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.openengsb.core.common.DomainMethodExecutionException;
import org.openengsb.core.common.util.AliveState;

public class JavaxMailAbstraction implements MailAbstraction {

    private AliveState aliveState = AliveState.OFFLINE;

    private Session createSession(final MailPropertiesImp properties) {
        Session session = Session.getDefaultInstance(properties.getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(properties.getUsername(), properties.getPassword());
            }
        });
        return session;
    }

    @Override
    public void send(MailProperties properties, String subject, String textContet, String receiver) {
        try {
            if (!(properties instanceof MailPropertiesImp)) {
                throw new RuntimeException("This implementation works only with internal mail properties");
            }
            MailPropertiesImp props = (MailPropertiesImp) properties;
            if (!(this.aliveState == AliveState.ONLINE)) {
                connect(props);
            }
            Session session = createSession(props);

            Message message = new MimeMessage(session);
            MailPropertiesImp propertiesImpl = (MailPropertiesImp) properties;
            message.setFrom(new InternetAddress(propertiesImpl.getSender()));
            message.setRecipients(RecipientType.TO, InternetAddress.parse(receiver));
            message.setSubject(buildSubject(propertiesImpl, subject));
            message.setText(textContet);
            send(message);
        } catch (Exception e) {
            throw new DomainMethodExecutionException(e);
        }
    }

    @Override
    public void connect(MailProperties properties) {
        if (!(properties instanceof MailPropertiesImp)) {
            throw new RuntimeException("This implementation works only with internal mail properties");
        }
        Session session = createSession((MailPropertiesImp) properties);
        MailPropertiesImp props = (MailPropertiesImp) properties;
        String smtpHost = (String) props.getProperties().get("mail.smtp.host");
        String username = props.getUsername();
        String password = props.getPassword();
        Transport tr = null;
        try {
            tr = session.getTransport("smtp");
            tr.connect(smtpHost, username, password);
            if (tr.isConnected()) {
                this.aliveState = AliveState.ONLINE;
            } else {
                this.aliveState = AliveState.OFFLINE;
            }
        } catch (MessagingException e) {
            this.aliveState = AliveState.OFFLINE;
            throw new DomainMethodExecutionException("Emailnotifier could not connect (wrong username/password or"
                    + " mail server unavailable) ");
        }
    }

    private String buildSubject(MailPropertiesImp properties, String subject) {
        if (properties.getPrefix() == null) {
            return subject;
        }
        return properties.getPrefix() + subject;
    }

    private void send(Message message) throws MessagingException {
        Transport.send(message);
    }

    @Override
    public MailProperties createMailProperties() {
        return new MailPropertiesImp();
    }

    private static class MailPropertiesImp implements MailProperties {

        private final Properties properties;
        private String username;
        private String password;
        private String sender;
        private String prefix;

        MailPropertiesImp() {
            properties = new Properties();
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        @Override
        public void setSmtpAuth(Boolean smtpAuth) {
            this.properties.setProperty("mail.smtp.auth", String.valueOf(smtpAuth));
        }

        @Override
        public void setSmtpHost(String smtpHost) {
            this.properties.setProperty("mail.smtp.host", smtpHost);
        }

        @Override
        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public void setUser(String user) {
            this.username = user;
        }

        @Override
        public void setSmtpPort(String smtpPort) {
            this.properties.setProperty("mail.smtp.port", smtpPort);
            this.properties.setProperty("mail.smtp.socketFactory.port", smtpPort);
        }

        public String getUsername() {
            return this.username;
        }

        public String getPassword() {
            return this.password;
        }

        public Properties getProperties() {
            return properties;
        }

        @Override
        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getSender() {
            return this.sender;
        }

        @Override
        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getPrefix() {
            return this.prefix;
        }
    }

    @Override
    public AliveState getAliveState() {
        return aliveState;
    }

}
