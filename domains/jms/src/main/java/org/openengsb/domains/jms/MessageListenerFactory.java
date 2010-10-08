package org.openengsb.domains.jms;

import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;

import org.openengsb.core.common.DomainProvider;
import org.osgi.framework.BundleContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.osgi.context.BundleContextAware;

public class MessageListenerFactory implements BundleContextAware {

    private BundleContext bundleContext;
    private final ConnectionFactory connectionFactory;

    public MessageListenerFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public MessageListener instance(DomainProvider domain) {
        return new JMSEventListener(domain.getId(), new EventCaller(bundleContext, domain), new JmsTemplate(
            connectionFactory));
    }

    @Override
    public final void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

}
