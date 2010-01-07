package org.openengsb.swingclient;

import java.util.UUID;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;

public class JmsService {

    private JmsTemplate jmsTemplate;

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public Object doServiceCall(ClientEndpoint endpoint, String operation, String message, String context)
            throws JMSException {
        Object obj = this.jmsTemplate.execute(new MySessionCallback(endpoint, operation, message, context), true);

        if (obj instanceof TextMessage) {
            return ((TextMessage) obj).getText();
        }

        if (obj instanceof ActiveMQObjectMessage) {
            Exception e = (Exception) (((ActiveMQObjectMessage) obj).getObject());
            return e.getMessage();
        }

        System.out.println("ERRRR" + obj.getClass());
        return "";

    }

    private class MySessionCallback implements SessionCallback {
        private ClientEndpoint endpoint;
        private String operation;
        private String message;
        private String context;

        public MySessionCallback(ClientEndpoint endpoint, String operation, String message, String context) {
            this.endpoint = endpoint;
            this.operation = operation;
            this.message = message;
            this.context = context;
        }

        @Override
        public Object doInJms(Session session) throws JMSException {
            Destination requestQueue = jmsTemplate.getDestinationResolver().resolveDestinationName(session,
                    endpoint.getDestinationName(), false);
            Destination responseQueue = jmsTemplate.getDestinationResolver().resolveDestinationName(session,
                    "org.openengsb.test.response." + UUID.randomUUID(), false);

            Message msg = session.createTextMessage(message);
            msg.setStringProperty("operation", operation);
            msg.setStringProperty("contextId", context);
            msg.setJMSReplyTo(responseQueue);

            MessageProducer producer = session.createProducer(requestQueue);
            producer.send(msg);

            MessageConsumer consumer = session.createConsumer(responseQueue);
            return consumer.receive();
        }
    }

}
