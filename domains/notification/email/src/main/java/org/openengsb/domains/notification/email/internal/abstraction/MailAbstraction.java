package org.openengsb.domains.notification.email.internal.abstraction;

public interface MailAbstraction {

    void send( MailProperties properties, String subject, String textContet, String receiver);

    MailProperties createMailProperties();
}
