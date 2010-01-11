/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */

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

    public String doServiceCall(ClientEndpoint endpoint, String operation, String message, String context, String mep)
            throws JMSException {

        Object obj;

        try {
            obj = this.jmsTemplate.execute(new MySessionCallback(endpoint, operation, message, context, mep), true);
        } catch (RuntimeException e) {
            return e.getMessage();
        }

        if (obj instanceof TextMessage) {
            return ((TextMessage) obj).getText();
        }

        if (obj instanceof ActiveMQObjectMessage) {
            Exception e = (Exception) (((ActiveMQObjectMessage) obj).getObject());
            return e.getMessage();
        }

        throw new IllegalStateException("Unknown return type: " + obj.getClass());
    }

    private class MySessionCallback implements SessionCallback {
        private ClientEndpoint endpoint;
        private String operation;
        private String message;
        private String context;
        private String mep;

        public MySessionCallback(ClientEndpoint endpoint, String operation, String message, String context, String mep) {
            this.endpoint = endpoint;
            this.operation = operation;
            this.message = message;
            this.context = context;
            this.mep = mep;
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
            msg.setStringProperty("mep", mep);
            msg.setJMSReplyTo(responseQueue);

            MessageProducer producer = session.createProducer(requestQueue);
            producer.send(msg);

            MessageConsumer consumer = session.createConsumer(responseQueue);
            return consumer.receive();
        }
    }

}
