package org.openengsb.domains.jms;

import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

public class MessageListenerContainerFactory {

    public AbstractMessageListenerContainer instance() {
        return new SimpleMessageListenerContainer();
    }
}
