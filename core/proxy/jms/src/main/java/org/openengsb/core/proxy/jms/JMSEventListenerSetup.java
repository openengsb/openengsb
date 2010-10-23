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

package org.openengsb.core.proxy.jms;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.service.DomainService;
import org.springframework.jms.listener.AbstractMessageListenerContainer;

public class JMSEventListenerSetup {

    Log log = LogFactory.getLog(JMSEventListenerSetup.class);

    private final DomainService domainService;
    private final ConnectionFactory connectionFactory;
    private final MessageListenerContainerFactory messageListenerContainerFactory;
    private final MessageListenerFactory messageListenerFactory;

    public JMSEventListenerSetup(DomainService domainService,
            MessageListenerContainerFactory messageListenerContainerFactory, ConnectionFactory connectionFactory,
            MessageListenerFactory messageListenerFactory) {
        this.domainService = domainService;
        this.messageListenerContainerFactory = messageListenerContainerFactory;
        this.messageListenerFactory = messageListenerFactory;
        this.connectionFactory = connectionFactory;

        addEventListeners();
    }

    private void addEventListeners() {
        for (DomainProvider domain : domainService.domains()) {
            log.error("Adding EventListener for: " + domain.getId());
            AbstractMessageListenerContainer instance = messageListenerContainerFactory.instance();
            instance.setDestinationName(domain.getId() + "_event_send");
            instance.setMessageListener(messageListenerFactory.instance(domain));
            instance.setConnectionFactory(connectionFactory);
            instance.start();
        }
    }
}
