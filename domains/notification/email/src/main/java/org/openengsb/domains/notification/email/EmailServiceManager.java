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

import org.openengsb.core.config.Domain;
import org.openengsb.core.config.ServiceManager;
import org.openengsb.core.config.descriptor.AttributeDefinition;
import org.openengsb.core.config.descriptor.ServiceDescriptor;
import org.openengsb.core.config.util.BundleStrings;
import org.openengsb.domains.notification.implementation.NotificationDomain;
import org.openengsb.domains.notification.email.internal.EmailNotifier;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;


public class EmailServiceManager implements ServiceManager, BundleContextAware {

    private BundleContext bundleContext;
    private BundleStrings strings;
    private final Map<String, EmailNotifier> services = new HashMap<String, EmailNotifier>();

    @Override
    public ServiceDescriptor getDescriptor() {
        return getDescriptor(Locale.getDefault());
    }

    @Override
    public ServiceDescriptor getDescriptor(Locale locale) {
        return ServiceDescriptor.builder()
                .id(EmailNotifier.class.getName())
                .implementsInterface(NotificationDomain.class.getName())
                .type(EmailNotifier.class)
                .name(strings.getString("email.name", locale))
                .description(strings.getString("email.description", locale))
                .attribute(AttributeDefinition.builder()
                        .id("user")
                        .name(strings.getString("username.outputMode.username", locale))
                        .description(strings.getString("username.outputMode.description", locale))
                        .defaultValue("openengsb.notification.test@gmail.com")
                        .required()
                        .build())
                .attribute(AttributeDefinition.builder()
                        .id("password")
                        .name(strings.getString("password.outputMode", locale))
                        .description(strings.getString("password.outputMode.description", locale))
                        .defaultValue("pwd-openengsb")
                        .required()
                        .build())
                .attribute(AttributeDefinition.builder()
                        .id("smtpAuth")
                        .name(strings.getString("mail.smtp.auth.outputMode", locale))
                        .description(strings.getString("mail.smtp.auth.outputMode.description", locale))
                        .defaultValue("true")
                        .required()
                        .build())
                .attribute(AttributeDefinition.builder()
                        .id("smtpUser")
                        .name(strings.getString("mail.smtp.user.outputMode", locale))
                        .description(strings.getString("mail.smtp.user.outputMode.description", locale))
                        .defaultValue("openengsb.notification.test@gmail.com")
                        .required()
                        .build())
                .attribute(AttributeDefinition.builder()
                        .id("smtpPort")
                        .name(strings.getString("mail.smtp.port.outputMode", locale))
                        .description(strings.getString("mail.smtp.port.outputMode.description", locale))
                        .defaultValue("465")
                        .required()
                        .build())
                .attribute(AttributeDefinition.builder()
                        .id("smtpHost")
                        .name(strings.getString("mail.smtp.host.outputMode", locale))
                        .description(strings.getString("mail.smtp.host.outputMode.description", locale))
                        .defaultValue("smtp.gmail.com")
                        .required()
                        .build())
                .build();
    }

    @Override
    public void update(String id, Map<String, String> attributes) {
        boolean isNew = false;
        EmailNotifier en = null;
        synchronized (services) {
            en = services.get(id);
            if (en == null) {
                en = new EmailNotifier(id);
                services.put(id, en);
                isNew = true;
            }
            if (attributes.containsKey("user")) {
                en.setUser(attributes.get("user"));
            }
            if (attributes.containsKey("password")) {
                en.setPassword(attributes.get("password"));
            }
            if (attributes.containsKey("smtpAuth")) {
                en.setSmtpAuth(attributes.get("smtpAuth"));
            }
            if (attributes.containsKey("smtpUser")) {
                en.setSmtpUser(attributes.get("smtpUser"));
            }
            if (attributes.containsKey("smtpHost")) {
                en.setSmtpHost(attributes.get("smtpHost"));
            }
            if (attributes.containsKey("smtpPort")) {
                en.setSmtpPort(attributes.get("smtpPort"));
            }
        }
        if (isNew) {
            Hashtable<String, String> props = new Hashtable<String, String>();
            props.put("id", id);
            props.put("domain", NotificationDomain.class.getName());
            props.put("class", EmailNotifier.class.getName());
            bundleContext.registerService(new String[]{EmailNotifier.class.getName(), NotificationDomain.class.getName(),
                    Domain.class.getName()},
                    en, props);
        }
    }

    @Override
    public void delete(String id) {
        synchronized (services) {
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
