package org.openengsb.domains.notification.email.internal.abstraction;

public interface MailProperties {
    void setSmtpAuth(String smtpAuth);

    void setSmtpHost(String smtpHost);

    void setPassword(String password);

    void setUser(String user);

    void setSmtpPort(String smtpPort);

    void setSender(String sender);

}
