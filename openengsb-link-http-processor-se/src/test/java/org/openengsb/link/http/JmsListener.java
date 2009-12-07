/**

   Copyright 2009 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.link.http;

import java.io.IOException;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;

public class JmsListener extends Thread {

    private boolean running;
    MessageConsumer mc;

    public JmsListener(String ip) throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
        Connection connection = connectionFactory.createConnection();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        connection.start();
        Destination dest = new ActiveMQTopic("org.openengsb.link.http");
        System.out.println("using IP: " + ip);
        mc = session.createConsumer(dest, String.format("ip = \'%s\'", ip));
        running = true;

    }

    @Override
    public void run() {
        while (running) {
            try {
                Message msg = mc.receive();
                System.out.println("message recieved");
                System.out.println(msg.toString());

            } catch (JMSException e) {
                running = false;
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        HttpClient httpClient = new HttpClient(new MultiThreadedHttpConnectionManager());
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(30000);

        GetMethod get = new GetMethod("http://localhost:8192/Link/");
        get.setQueryString("whoami");

        try {
            if (httpClient.executeMethod(get) != 200) {
                throw new RuntimeException("unable to determine ip for response");
            }
        } catch (HttpException e2) {
            throw new RuntimeException(e2);
        } catch (IOException e2) {
            throw new RuntimeException(e2);
        }

        String response;
        try {
            response = get.getResponseBodyAsString();
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
        int pos = response.indexOf("You are ");
        int pos2 = response.indexOf("</h1>");
        String ip = response.substring(pos + 8, pos2);
        try {
            new JmsListener(ip).start();
        } catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

    }
}
