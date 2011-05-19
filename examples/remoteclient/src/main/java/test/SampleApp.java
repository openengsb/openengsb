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
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
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
 *
 */
public class SampleApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleApp.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String URL = "tcp://127.0.0.1:6549";

    private static Connection connection;
    private static Session session;
    private static MessageProducer producer;

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
        String requestString = mapper.writeValueAsString(methodCallRequest);
        sendMessage(requestString);
        String resultString = getResultFromQueue(methodCallRequest.getCallId());
        return convertStringToResult(resultString);
    }

    private static MethodResult convertStringToResult(String resultString) throws IOException, JsonParseException,
        JsonMappingException, ClassNotFoundException {
        MethodResultMessage resultMessage = mapper.readValue(resultString, MethodResultMessage.class);
        MethodResult result = resultMessage.getResult();
        Class<?> clazz = Class.forName(result.getClassName());
        Object resultValue = mapper.convertValue(result.getArg(), clazz);
        result.setArg(resultValue);
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
