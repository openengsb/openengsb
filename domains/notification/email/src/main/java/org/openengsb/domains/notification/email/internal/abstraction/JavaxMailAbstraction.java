package org.openengsb.domains.notification.email.internal.abstraction;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JavaxMailAbstraction implements MailAbstraction {

    @Override
    public Session createSession(Properties properties, Authenticator authenticator) {
        return Session.getDefaultInstance(properties, authenticator);
    }

    @Override
    public Message createMessage(Session session, InternetAddress from, RecipientType recipientType,
            Address[] recipients, String subject, String textContet) throws MessagingException {
        Message message = new MimeMessage(session);
        message.setFrom(from);
        message.setRecipients(recipientType, recipients);
        message.setSubject(subject);
        message.setText(textContet);
        return message;
    }

    @Override
    public void send(Message message) throws MessagingException {
        Transport.send(message);
    }

}
