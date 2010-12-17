package org.openengsb.core.ports.jms;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

public interface JMSTemplateFactory {

    JmsTemplate createJMSTemplate(String host);

    SimpleMessageListenerContainer createMessageListenerContainer();
}
