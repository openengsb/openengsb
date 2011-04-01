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

package org.openengsb.connector.email.internal.abstraction;

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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.AliveState;
import org.openengsb.core.common.DomainMethodExecutionException;

public class JavaxMailAbstraction implements MailAbstraction {

    private Log log = LogFactory.getLog(JavaxMailAbstraction.class);

    private AliveState aliveState = AliveState.OFFLINE;
    
    private SessionManager sessionManager = new SessionManager();

    private Session getSession(final MailPropertiesImp properties) {
        return sessionManager.getSession(properties);
    }

    @Override
    public void send(MailProperties properties, String subject, String textContent, String receiver) {
        try {
            if (!(properties instanceof MailPropertiesImp)) {
                throw new RuntimeException("This implementation works only with internal mail properties");
            }
            MailPropertiesImp props = (MailPropertiesImp) properties;

            Session session = getSession(props);

            Message message = new MimeMessage(session);

            message.setFrom(new InternetAddress(props.getSender()));
            message.setRecipients(RecipientType.TO, InternetAddress.parse(receiver));
            message.setSubject(buildSubject(props, subject));
            message.setText(textContent);
            send(message, session);
        } catch (Exception e) {
            throw new DomainMethodExecutionException(e);
        }
    }

    @Override
    public void connect(MailProperties properties) {
        if (!(properties instanceof MailPropertiesImp)) {
            throw new RuntimeException("This implementation works only with internal mail properties");
        }
        Session session = getSession((MailPropertiesImp) properties);
        getTransport(session);
    }
    
    private Transport getTransport(Session session) {
        Transport transport = null;
        try {
            transport = session.getTransport("smtp");
            log.debug("connecting smtp-transport " + transport);
            transport.connect();
            if (transport.isConnected()) {
                this.aliveState = AliveState.ONLINE;
            } else {
                this.aliveState = AliveState.OFFLINE;
            }
            log.debug("State is now " + this.aliveState);
        } catch (MessagingException e) {
            log.error("could not connect transport ", e);
            this.aliveState = AliveState.OFFLINE;
            throw new DomainMethodExecutionException("Emailnotifier could not connect (wrong username/password or"
                    + " mail server unavailable) ");
        }
        
        return transport;
    }

    private void send(Message message, Session session) throws MessagingException {
        log.info("sending email-message");
        message.saveChanges();
        Transport transport = getTransport(session);
        transport.sendMessage(message, message.getAllRecipients());
        log.info("email has been sent");
    }
    
    private String buildSubject(MailPropertiesImp properties, String subject) {
        log.debug("building subject");
        if (properties.getPrefix() == null) {
            return subject;
        }
        return new StringBuilder().append(properties.getPrefix()).append(" ").append(subject).toString();
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
        private SecureMode secureMode = SecureMode.PLAIN;

        MailPropertiesImp() {
            properties = new Properties();
            properties.setProperty("mail.debug", "true");
            properties.setProperty("mail.smtp.timeout", "35000");
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
        
        @Override
        public void setSecureMode(String secureMode) {
            if (SecureMode.SSL.toString().equals(secureMode)) {
                this.secureMode = SecureMode.SSL;
                properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            } else if (SecureMode.STARTTLS.toString().equals(secureMode)) {
                this.secureMode = SecureMode.STARTTLS;
                properties.put("mail.smtp.starttls.enable", "true");
            } else {
                this.secureMode = SecureMode.PLAIN;
            }
        }

        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj);
        }
        
        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }
    }

    @Override
    public AliveState getAliveState() {
        return aliveState;
    }
    
    private static class SessionManager {
        private Log log = LogFactory.getLog(SessionManager.class);
        
        private Session session;
        private MailPropertiesImp properties;
        
        public Session getSession(MailPropertiesImp newProperties) {         
            if (session == null || !newProperties.equals(properties)) {
                log.info("create new mail session");
                
                properties = newProperties;
                session = Session.getInstance(properties.getProperties(), new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(properties.getUsername(), properties.getPassword());
                    }
                });
            }
            return session;
        }
    }

}
