package org.openengsb.domains.notification.email.internal.abstraction;

public interface MailProperties {
    void setSmtpAuth(Boolean smtpAuth);

    void setSmtpHost(String smtpHost);

    void setPassword(String password);

    void setUser(String user);

    void setSmtpPort(String smtpPort);

    void setSender(String sender);

    void setPrefix(String string);
}
