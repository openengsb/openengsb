package org.openengsb.domains.notification.email.internal.abstraction;

import javax.mail.MessagingException;

public interface MailAbstraction {

    void send( MailProperties properties, String subject, String textContet, String receiver) throws MessagingException;

    MailProperties createMailProperties();
}
