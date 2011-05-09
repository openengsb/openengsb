/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.ports.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

public class JMSTemplateFactoryImpl implements JMSTemplateFactory {

    /** This one can be overwritten whenever required */
    private static final Long DEFAULT_TIMEOUT = 3000L;

    /** Shares single connection factories to avoid the extended afford of creating one */
    private Map<String, ConnectionFactory> connections = new HashMap<String, ConnectionFactory>();

    @Override
    public JmsTemplate createJMSTemplate(DestinationUrl destination) {
        ConnectionFactory connectionFactory = retrieveJmsConnectionFactory(destination.getHost());
        JmsTemplate template = retrieveJmsTemplate(destination.getJmsDestination(), connectionFactory);
        return template;
    }

    private synchronized ConnectionFactory retrieveJmsConnectionFactory(String host) {
        if (!connections.containsKey(host)) {
            connections.put(host, new SingleConnectionFactory(new ActiveMQConnectionFactory(host)));
        }
        return connections.get(host);
    }

    private JmsTemplate retrieveJmsTemplate(String jmsDestination, ConnectionFactory connectionFactory) {
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setDefaultDestinationName(jmsDestination);
        template.setReceiveTimeout(DEFAULT_TIMEOUT);
        return template;
    }

    @Override
    public SimpleMessageListenerContainer createMessageListenerContainer() {
        return new SimpleMessageListenerContainer();
    }

}
