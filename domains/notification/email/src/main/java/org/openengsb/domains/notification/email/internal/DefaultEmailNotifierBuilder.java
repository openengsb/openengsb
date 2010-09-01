package org.openengsb.domains.notification.email.internal;

import org.openengsb.domains.notification.email.internal.abstraction.MailAbstraction;

import java.util.Map;

public class DefaultEmailNotifierBuilder implements EmailNotifierBuilder {

    private MailAbstraction mailAbstraction;

    public DefaultEmailNotifierBuilder(MailAbstraction mailAbstraction) {
        this.mailAbstraction = mailAbstraction;
    }

    @Override
    public EmailNotifier createEmailNotifier(String id, Map<String, String> attributes) {
        EmailNotifier notifier = new EmailNotifier(id, mailAbstraction);
        setAttributesOnNotifier(attributes, notifier);
        return notifier;
    }

    @Override
    public void updateEmailNotifier(EmailNotifier notifier, Map<String, String> attributes) {
        setAttributesOnNotifier(attributes, notifier);
    }

    private void setAttributesOnNotifier(Map<String, String> attributes, EmailNotifier notifier) {
        
        if (attributes.containsKey("user")) {
            notifier.getProperties().setUser(attributes.get("user"));
        }
        if (attributes.containsKey("password")) {
            notifier.getProperties().setPassword(attributes.get("password"));
        }
        if (attributes.containsKey("smtpAuth")) {
            notifier.getProperties().setSmtpAuth(attributes.get("smtpAuth"));
        }
        if (attributes.containsKey("smtpSender")) {
            notifier.getProperties().setSender(attributes.get("smtpSender"));
        }
        if (attributes.containsKey("smtpHost")) {
            notifier.getProperties().setSmtpHost(attributes.get("smtpHost"));
        }
        if (attributes.containsKey("smtpPort")) {
            notifier.getProperties().setSmtpPort(attributes.get("smtpPort"));
        }
    }

}
