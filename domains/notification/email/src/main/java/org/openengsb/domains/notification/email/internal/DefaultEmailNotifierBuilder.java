package org.openengsb.domains.notification.email.internal;

import java.util.Map;

import org.openengsb.domains.notification.email.internal.abstraction.MailAbstraction;

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
            notifier.setUser(attributes.get("user"));
        }
        if (attributes.containsKey("password")) {
            notifier.setPassword(attributes.get("password"));
        }
        if (attributes.containsKey("smtpAuth")) {
            notifier.setSmtpAuth(attributes.get("smtpAuth"));
        }
        if (attributes.containsKey("smtpSender")) {
            notifier.setSmtpSender(attributes.get("smtpSender"));
        }
        if (attributes.containsKey("smtpHost")) {
            notifier.setSmtpHost(attributes.get("smtpHost"));
        }
        if (attributes.containsKey("smtpPort")) {
            notifier.setSmtpPort(attributes.get("smtpPort"));
        }
    }

}
