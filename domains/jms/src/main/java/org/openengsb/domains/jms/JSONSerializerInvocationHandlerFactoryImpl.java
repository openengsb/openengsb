package org.openengsb.domains.jms;

import java.lang.reflect.InvocationHandler;

import org.openengsb.core.common.DomainProvider;
import org.springframework.jms.core.JmsTemplate;

public class JSONSerializerInvocationHandlerFactoryImpl implements InvocationHandlerFactory {

    private final JmsTemplate template;

    public JSONSerializerInvocationHandlerFactoryImpl(JmsTemplate template) {
        super();
        this.template = template;
    }

    @Override
    public InvocationHandler createInstance(DomainProvider domain) {
        JMSSender sender = new JMSSender(domain.getId(), this.template);
        return new JSONSerialisationInvocationHandler(sender);
    }
}
