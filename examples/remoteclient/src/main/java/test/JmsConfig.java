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

package test;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsConfig.class);

    private String url;

    private Connection connection;
    private Session session;

    public JmsConfig(String url) {
        this.url = url;
    }

    public void init() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public MessageProducer createProducerForQueue(String name) throws JMSException {
        Destination destination = session.createQueue(name);
        return session.createProducer(destination);
    }

    public MessageConsumer createConsumerForQueue(String name) throws JMSException {
        Destination queue = session.createQueue(name);
        return session.createConsumer(queue);
    }

    public MessageConsumer createConsumerForQueue(String name, MessageListener listener) throws JMSException {
        MessageConsumer result = createConsumerForQueue(name);
        result.setMessageListener(listener);
        return result;
    }

    public void sendMessage(String queue, String message) throws JMSException {
        LOGGER.info("sending message {}", message);
        LOGGER.info("to queue {}", queue);
        TextMessage message2 = session.createTextMessage(message);
        MessageProducer resultProducer = createProducerForQueue(queue);
        resultProducer.send(message2);

    }

    public void stop() throws JMSException {
        session.close();
        connection.stop();
        connection.close();
    }

}
