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
package org.openengsb.linking.http;

import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

public class JmsListener extends Thread {

    private boolean running;
    private List<Object> messages;
    MessageConsumer mc;

    public JmsListener(List<Object> messages) throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = connectionFactory.createConnection();
        // connection.setClientID(supplierName);
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
        Destination dest = new ActiveMQQueue();
        mc = session.createConsumer(dest);
        this.messages = messages;
        running = true;

    }

    @Override
    public void run() {
        while (running) {
            try {
                Message msg = mc.receive();
                msg.toString();
                messages.add(msg);
                messages.notify();
            } catch (JMSException e) {
                running = false;
                e.printStackTrace();
            }
        }
    }
}
