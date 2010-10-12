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

package org.openengsb.domains.jms;

import java.lang.reflect.InvocationHandler;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.jms.ConnectionFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.DomainProvider;
import org.openengsb.core.common.ServiceManager;
import org.openengsb.core.common.service.DomainService;
import org.osgi.framework.BundleContext;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.osgi.context.BundleContextAware;

public class JMSConnector implements BundleContextAware {

    Log log = LogFactory.getLog(JMSConnector.class);

    private BundleContext bundleContext;
    private final InvocationHandlerFactory handlerFactory;
    private final DomainService domainService;
    private final ConnectionFactory connectionFactory;
    private final MessageListenerContainerFactory messageListenerContainerFactory;
    private final MessageListenerFactory messageListenerFactory;

    public JMSConnector(DomainService domainService, InvocationHandlerFactory factory,
            MessageListenerContainerFactory messageListenerContainerFactory, ConnectionFactory connectionFactory,
            MessageListenerFactory messageListenerFactory) {
        this.domainService = domainService;
        this.handlerFactory = factory;
        this.messageListenerContainerFactory = messageListenerContainerFactory;
        this.messageListenerFactory = messageListenerFactory;
        this.connectionFactory = connectionFactory;
    }

    public void init() {
        addProxiesToContext();
        addEventListeners();
    }

    public void addEventListeners() {
        for (DomainProvider domain : domainService.domains()) {
            log.error("Adding EventListener for: " + domain.getId());
            AbstractMessageListenerContainer instance = messageListenerContainerFactory.instance();
            instance.setDestinationName(domain.getId() + "_event_send");
            instance.setMessageListener(messageListenerFactory.instance(domain));
            instance.setConnectionFactory(connectionFactory);
            instance.start();
        }
    }

    public void addProxiesToContext() {
        for (DomainProvider domain : domainService.domains()) {
            addProxyServiceManager(domain);
        }
    }

    private void addProxyServiceManager(DomainProvider domain) {
        InvocationHandler handler = handlerFactory.createInstance(domain);
        Dictionary<String, String> properties = new Hashtable<String, String>();
        properties.put("domain", domain.getDomainInterface().getName());
        bundleContext.registerService(ServiceManager.class.getName(), new ProxyServiceManager(domain, handler,
            bundleContext), properties);
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
