/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.domains.notification.email.internal;

import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.common.ServiceInstanceFactory;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.core.common.validation.MultipleAttributeValidationResultImpl;
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
        if (attributes.containsKey("prefix")) {
            notifier.getProperties().setPrefix(attributes.get("prefix"));
        }
        if (attributes.containsKey("smtpAuth")) {
            notifier.getProperties().setSmtpAuth(Boolean.parseBoolean(attributes.get("smtpAuth")));
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
    public ServiceDescriptor getDescriptor(ServiceDescriptor.Builder builder) {
        builder.name("email.name").description("email.description");

        builder
            .attribute(buildAttribute(builder, "user", "username.outputMode", "username.outputMode.description"))
            .attribute(
                builder.newAttribute().id("password").name("password.outputMode")
                    .description("password.outputMode.description").defaultValue("").required().asPassword().build())
            .attribute(buildAttribute(builder, "prefix", "prefix.outputMode", "prefix.outputMode.description"))
            .attribute(
                builder.newAttribute().id("smtpAuth").name("mail.smtp.auth.outputMode")
                    .description("mail.smtp.auth.outputMode.description").defaultValue("false").asBoolean().build())
            .attribute(
                buildAttribute(builder, "smtpSender", "mail.smtp.sender.outputMode",
                    "mail.smtp.sender.outputMode.description"))
            .attribute(
                buildAttribute(builder, "smtpPort", "mail.smtp.port.outputMode",
                    "mail.smtp.port.outputMode.description"))
            .attribute(
                buildAttribute(builder, "smtpHost", "mail.smtp.host.outputMode",
                    "mail.smtp.host.outputMode.description")).build();

        return builder.build();
    }

    private AttributeDefinition buildAttribute(ServiceDescriptor.Builder builder, String id, String nameId,
            String descriptionId) {
        return builder.newAttribute().id(id).name(nameId).description(descriptionId).defaultValue("").required()
            .build();

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

    @Override
    public MultipleAttributeValidationResult updateValidation(EmailNotifier instance, Map<String, String> attributes) {
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }

    @Override
    public MultipleAttributeValidationResult createValidation(String id, Map<String, String> attributes) {
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }
}
