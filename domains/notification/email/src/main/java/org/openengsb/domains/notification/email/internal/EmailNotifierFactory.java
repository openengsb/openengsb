package org.openengsb.domains.notification.email.internal;

import java.util.Locale;
import java.util.Map;

import org.openengsb.core.config.ServiceInstanceFactory;
import org.openengsb.core.config.descriptor.AttributeDefinition;
import org.openengsb.core.config.descriptor.ServiceDescriptor;
import org.openengsb.core.config.util.BundleStrings;
import org.openengsb.domains.notification.NotificationDomain;
import org.openengsb.domains.notification.email.internal.abstraction.MailAbstraction;

public class EmailNotifierFactory implements ServiceInstanceFactory<NotificationDomain, EmailNotifier> {

    private final MailAbstraction mailAbstraction;

    public EmailNotifierFactory(MailAbstraction mailAbstraction) {
        this.mailAbstraction = mailAbstraction;
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

    @Override
    public ServiceDescriptor getDescriptor(ServiceDescriptor.Builder builder, Locale locale, BundleStrings strings) {
        return builder
                .name(strings.getString("email.name", locale))
                .description(strings.getString("email.description", locale))
                .attribute(
                        buildAttribute(locale, strings, "user", "username.outputMode",
                                "username.outputMode.description"))
                .attribute(
                        buildAttribute(locale, strings, "password", "password.outputMode",
                                "password.outputMode.description"))
                .attribute(
                        buildAttribute(locale, strings, "smtpAuth", "mail.smtp.auth.outputMode",
                                "mail.smtp.auth.outputMode.description"))
                .attribute(
                        buildAttribute(locale, strings, "smtpSender", "mail.smtp.sender.outputMode",
                                "mail.smtp.sender.outputMode.description"))
                .attribute(
                        buildAttribute(locale, strings, "smtpPort", "mail.smtp.port.outputMode",
                                "mail.smtp.port.outputMode.description"))
                .attribute(
                        buildAttribute(locale, strings, "smtpHost", "mail.smtp.host.outputMode",
                                "mail.smtp.host.outputMode.description")).build();
    }

    private AttributeDefinition buildAttribute(Locale locale, BundleStrings strings, String id, String nameId,
            String descriptionId) {
        return AttributeDefinition.builder().id(id).name(strings.getString(nameId, locale))
                .description(strings.getString(descriptionId, locale)).defaultValue("").required().build();

    }

    @Override
    public void updateServiceInstance(EmailNotifier instance, Map<String, String> attributes) {
        setAttributesOnNotifier(attributes, instance);
    }

    @Override
    public EmailNotifier createServiceInstance(String id, Map<String, String> attributes) {
        EmailNotifier notifier = new EmailNotifier(id, mailAbstraction);
        setAttributesOnNotifier(attributes, notifier);
        return notifier;
    }
}
