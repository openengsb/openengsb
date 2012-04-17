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

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

/**
 * Hello world!
 */
public final class PlainSampleApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(PlainSampleApp.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final String URL = "tcp://127.0.0.1:6549";

    private static Connection connection;
    private static Session session;
    private static MessageProducer producer;

    private PlainSampleApp() {
      // required because this is a util class
    }

    private static void init() throws JMSException {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(URL);
        connection = connectionFactory.createConnection();
        connection.start();

        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = session.createQueue("receive");
        producer = session.createProducer(destination);
    }

    private static MethodResult call(MethodCall call) throws IOException, JMSException, InterruptedException,
        ClassNotFoundException {
        MethodCallRequest methodCallRequest = new MethodCallRequest(call);
        String requestString = MAPPER.writeValueAsString(methodCallRequest);
        sendMessage(requestString);
        String resultString = getResultFromQueue(methodCallRequest.getCallId());
        return convertStringToResult(resultString);
    }

    private static MethodResult convertStringToResult(String resultString) throws IOException,
        ClassNotFoundException {
        MethodResultMessage resultMessage = MAPPER.readValue(resultString, MethodResultMessage.class);
        MethodResult result = resultMessage.getResult();
        return result;
    }

    private static void sendMessage(String requestString) throws JMSException {
        TextMessage message = session.createTextMessage(requestString);
        producer.send(message);
    }

    private static String getResultFromQueue(String callId) throws JMSException, InterruptedException {
        Destination resultDest = session.createQueue(callId);
        MessageConsumer consumer = session.createConsumer(resultDest);
        final Semaphore messageSem = new Semaphore(0);
        final AtomicReference<String> resultReference = new AtomicReference<String>();
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    String text = ((TextMessage) message).getText();
                    resultReference.set(text);
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                } finally {
                    messageSem.release();
                }
            }
        });
        LOGGER.info("waiting for response");
        messageSem.acquire();
        LOGGER.info("response received");
        return resultReference.get();
    }

    private static void stop() throws JMSException {
        session.close();
        connection.stop();
        connection.close();
    }

    /**
     * Sample app for a plain simple communication app
     */
    public static void main(String[] args) throws Exception {
        LOGGER.info("initializing");
        init();
        LOGGER.info("initialized");
        MethodCall methodCall =
            new MethodCall("doSomething", new Object[]{ "Hello World!" }, ImmutableMap.of("serviceId",
                "example+example+testlog", "contextId", "foo"));
        LOGGER.info("calling method");
        MethodResult methodResult = call(methodCall);
        System.out.println(methodResult);

        stop();
    }
}

