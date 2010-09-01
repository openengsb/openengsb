package org.openengsb.domains.notification.email.internal.abstraction;

import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class JavaxMailAbstraction implements MailAbstraction {


    private Session createSession(MailProperties properties) {
        if (!(properties instanceof MailPropertiesImp)) {
            throw new RuntimeException("This implementation works only with internal mail properties");
        }
        final MailPropertiesImp props = (MailPropertiesImp)properties;

        return Session.getDefaultInstance(props.getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(props.getUsername(), props.getPassword());
            }
        });
    }

    @Override
    public void createMessage(MailProperties properties,
                                 String subject, String textContet, String receiver) throws MessagingException {
        if (!(properties instanceof MailPropertiesImp)) {
            throw new RuntimeException("This implementation works only with internal mail properties");
        }
        Session session = createSession(properties);
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(((MailPropertiesImp)properties).getSender()));
        message.setRecipients(RecipientType.TO, InternetAddress.parse(receiver));
        message.setSubject(subject);
        message.setText(textContet);

        send(message);
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

        MailPropertiesImp() {
            properties = new Properties();
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }


        @Override
        public void setSmtpAuth(String smtpAuth) {
            this.properties.put("mail.smtp.auth", smtpAuth);
        }

        @Override
        public void setSmtpHost(String smtpHost) {
            this.properties.put("mail.smtp.host", smtpHost);
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
            this.properties.put("mail.smtp.port", smtpPort);
            this.properties.put("mail.smtp.socketFactory.port", smtpPort);
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

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getSender() {
            return this.sender;
        }
    }

}
