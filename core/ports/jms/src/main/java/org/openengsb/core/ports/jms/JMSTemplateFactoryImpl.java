package org.openengsb.core.ports.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

public class JMSTemplateFactoryImpl implements JMSTemplateFactory {

    private final Map<String, ConnectionFactory> connections = new HashMap<String, ConnectionFactory>();

    @Override
    public JmsTemplate createJMSTemplate(String host) {
        if (!connections.containsKey(host)) {
            connections.put(host, new SingleConnectionFactory(new ActiveMQConnectionFactory(host)));
        }
        return new JmsTemplate(connections.get(host));
    }

    @Override
    public SimpleMessageListenerContainer createMessageListenerContainer() {
        return new SimpleMessageListenerContainer();
    }

}
