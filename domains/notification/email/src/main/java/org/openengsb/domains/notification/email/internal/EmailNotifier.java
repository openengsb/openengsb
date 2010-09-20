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

import org.openengsb.core.common.util.AliveEnum;
import org.openengsb.domains.notification.NotificationDomain;
import org.openengsb.domains.notification.email.internal.abstraction.MailAbstraction;
import org.openengsb.domains.notification.email.internal.abstraction.MailProperties;
import org.openengsb.domains.notification.model.Notification;
import org.osgi.framework.ServiceRegistration;

public class EmailNotifier implements NotificationDomain {

    private final String id;

    private final MailAbstraction mailAbstraction;
    private ServiceRegistration serviceRegistration;
    private final MailProperties properties;

    public EmailNotifier(String id, MailAbstraction mailAbstraction) {
        this.id = id;
        this.mailAbstraction = mailAbstraction;
        properties = mailAbstraction.createMailProperties();
    }

    @Override
    public void notify(Notification notification) {
        mailAbstraction.send(properties, notification.getSubject(), notification.getMessage(), notification
                .getRecipient());
    }

    @Override
    public AliveEnum getAliveState() {
        AliveEnum aliveState = mailAbstraction.getAliveState();
        if (aliveState == null) {
            return AliveEnum.OFFLINE;
        }
        return aliveState;
    }

    public String getId() {
        return id;
    }

    public ServiceRegistration getServiceRegistration() {
        return serviceRegistration;
    }

    public void setServiceRegistration(ServiceRegistration serviceRegistration) {
        this.serviceRegistration = serviceRegistration;
    }

    public MailProperties getProperties() {
        return properties;
    }
}
