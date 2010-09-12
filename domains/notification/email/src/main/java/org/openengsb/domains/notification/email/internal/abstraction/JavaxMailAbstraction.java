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

public class JavaxMailAbstraction implements MailAbstraction {

    private Session createSession(MailProperties properties) {
        if (!(properties instanceof MailPropertiesImp)) {
            throw new RuntimeException("This implementation works only with internal mail properties");
        }
        final MailPropertiesImp props = (MailPropertiesImp) properties;

        return Session.getDefaultInstance(props.getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(props.getUsername(), props.getPassword());
            }
        });
    }

    @Override
    public void send(MailProperties properties, String subject, String textContet, String receiver) {
        try {
            if (!(properties instanceof MailPropertiesImp)) {
                throw new RuntimeException("This implementation works only with internal mail properties");
            }
            Session session = createSession(properties);
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

    private class MailPropertiesImp implements MailProperties {

        private Properties properties;
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

}
