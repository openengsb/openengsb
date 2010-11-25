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
import javax.jms.MessageListener;

import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.context.ContextCurrentService;
import org.osgi.framework.BundleContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.osgi.context.BundleContextAware;

public class MessageListenerFactory implements BundleContextAware {

    private BundleContext bundleContext;
    private final ConnectionFactory connectionFactory;
    private final ContextCurrentService contextService;

    public MessageListenerFactory(ConnectionFactory connectionFactory, ContextCurrentService contextService) {
        this.connectionFactory = connectionFactory;
        this.contextService = contextService;
    }

    public MessageListener instance(DomainProvider domain) {
        return new JMSEventListener(domain.getId(), new EventCaller(bundleContext, domain), new JmsTemplate(
            connectionFactory), contextService);
    }

    @Override
    public final void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}
