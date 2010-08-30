/**

 Copyright 2010 OpenEngSB Division, Vienna University of Technology

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */
package org.openengsb.domains.notification.email;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import org.openengsb.core.config.Domain;
import org.openengsb.core.config.ServiceManager;
import org.openengsb.core.config.descriptor.AttributeDefinition;
import org.openengsb.core.config.descriptor.ServiceDescriptor;
import org.openengsb.core.config.util.BundleStrings;
import org.openengsb.domains.notification.NotificationDomain;
import org.openengsb.domains.notification.email.internal.EmailNotifier;
import org.openengsb.domains.notification.email.internal.EmailNotifierBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.context.BundleContextAware;

public class EmailServiceManager implements ServiceManager, BundleContextAware {

    private BundleContext bundleContext;
    private BundleStrings strings;
    private final Map<String, EmailNotifier> services = new HashMap<String, EmailNotifier>();
    private EmailNotifierBuilder emailNotifierBuilder;

    public EmailServiceManager(EmailNotifierBuilder emailNotifierBuilder) {
        this.emailNotifierBuilder = emailNotifierBuilder;
    }

    @Override
    public ServiceDescriptor getDescriptor() {
        return getDescriptor(Locale.getDefault());
    }

    @Override
    public ServiceDescriptor getDescriptor(Locale locale) {
        return ServiceDescriptor
                .builder()
                .id(EmailNotifier.class.getName())
                .implementsInterface(NotificationDomain.class.getName())
                .type(EmailNotifier.class)
                .name(strings.getString("email.name", locale))
                .description(strings.getString("email.description", locale))
                .attribute(buildAttribute(locale, "user", "username.outputMode", "username.outputMode.description"))
                .attribute(buildAttribute(locale, "password", "password.outputMode", "password.outputMode.description"))
                .attribute(
                        buildAttribute(locale, "smtpAuth", "mail.smtp.auth.outputMode",
                                "mail.smtp.auth.outputMode.description"))
                .attribute(
                        buildAttribute(locale, "smtpSender", "mail.smtp.sender.outputMode",
                                "mail.smtp.sender.outputMode.description"))
                .attribute(
                        buildAttribute(locale, "smtpPort", "mail.smtp.port.outputMode",
                                "mail.smtp.port.outputMode.description"))
                .attribute(
                        buildAttribute(locale, "smtpHost", "mail.smtp.host.outputMode",
                                "mail.smtp.host.outputMode.description")).build();
    }

    private AttributeDefinition buildAttribute(Locale locale, String id, String nameId, String descriptionId) {
        return AttributeDefinition.builder().id(id).name(strings.getString(nameId, locale))
                .description(strings.getString(descriptionId, locale)).defaultValue("").required().build();
    }

    @Override
    public void update(String id, Map<String, String> attributes) {
        EmailNotifier en = null;
        synchronized (services) {
            en = services.get(id);
            if (en == null) {
                en = emailNotifierBuilder.createEmailNotifier(id, attributes);
                services.put(id, en);
                Hashtable<String, String> serviceProperties = createNotificationServiceProperties(id);
                ServiceRegistration serviceRegistration = bundleContext.registerService(new String[] {
                        EmailNotifier.class.getName(), NotificationDomain.class.getName(), Domain.class.getName() },
                        en, serviceProperties);
                en.setServiceRegistration(serviceRegistration);
            } else {
                emailNotifierBuilder.updateEmailNotifier(en, attributes);
            }
        }
    }

    private Hashtable<String, String> createNotificationServiceProperties(String id) {
        Hashtable<String, String> serviceProperties = new Hashtable<String, String>();
        serviceProperties.put("id", id);
        serviceProperties.put("domain", NotificationDomain.class.getName());
        serviceProperties.put("class", EmailNotifier.class.getName());
        return serviceProperties;
    }

    @Override
    public void delete(String id) {
        synchronized (services) {
            EmailNotifier notifier = services.get(id);
            notifier.getServiceRegistration().unregister();
            services.remove(id);
        }
    }

    public void init() {
        strings = new BundleStrings(bundleContext.getBundle());
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
