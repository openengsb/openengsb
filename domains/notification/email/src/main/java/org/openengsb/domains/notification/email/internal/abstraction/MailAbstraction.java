package org.openengsb.domains.notification.email.internal.abstraction;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

public interface MailAbstraction {

    Session createSession(Properties properties, Authenticator authenticator);

    Message createMessage(Session session, InternetAddress from, RecipientType recipientType, Address[] recipients,
            String subject, String textContet) throws MessagingException;

    void send(Message message) throws MessagingException;

}
