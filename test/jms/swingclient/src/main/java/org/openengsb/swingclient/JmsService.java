package org.openengsb.swingclient;

import java.util.UUID;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.jms.JmsException;
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

    public Object doServiceCall(String message, String context) {
        try {
            TextMessage textMessage = (((TextMessage) this.jmsTemplate.execute(new MySessionCallback(message, context),
                    true)));
            return textMessage.getText();
        } catch (JmsException e) {
            throw new RuntimeException(e);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private class MySessionCallback implements SessionCallback {
        private String message;
        private String context;

        public MySessionCallback(String message, String context) {
            this.message = message;
            this.context = context;
        }

        @Override
        public Object doInJms(Session session) throws JMSException {
            Destination requestQueue = jmsTemplate.getDestinationResolver().resolveDestinationName(session,
                    "org.openengsb.test.emailService", false);
            Destination responseQueue = jmsTemplate.getDestinationResolver().resolveDestinationName(session,
                    "org.openengsb.test.response." + UUID.randomUUID(), false);

            Message msg = session.createTextMessage(message);
            msg.setStringProperty("operation", "methodcall");
            msg.setStringProperty("contextId", context);
            msg.setJMSReplyTo(responseQueue);

            MessageProducer producer = session.createProducer(requestQueue);
            producer.send(msg);

            MessageConsumer consumer = session.createConsumer(responseQueue);
            return consumer.receive();
        }
    }

}
