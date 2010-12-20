/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.ports.jms;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.common.communication.IncomingPort;
import org.openengsb.core.common.communication.MethodCall;
import org.openengsb.core.common.communication.MethodReturn;
import org.openengsb.core.common.communication.OutgoingPort;
import org.openengsb.core.common.communication.RequestHandler;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

public class JMSPort implements IncomingPort, OutgoingPort {

    private final JMSTemplateFactory factory;

    private final ConnectionFactory connectionFactory;

    private RequestHandler requestHandler;

    private SimpleMessageListenerContainer simpleMessageListenerContainer;

    private final ObjectMapper mapper = new ObjectMapper();

    public JMSPort(JMSTemplateFactory factory, ConnectionFactory connectionFactory) {
        this.factory = factory;
        this.connectionFactory = connectionFactory;
    }

    @Override
    public void send(URI destination, MethodCall call) {
        sendMessage(destination, call);
    }

    @Override
    public MethodReturn sendSync(URI destination, MethodCall call) {
        String currentTimeMillis = String.valueOf(System.currentTimeMillis());
        RequestMapping mapping = new RequestMapping(call);
        mapping.setAnswer(true);
        mapping.setCallId(currentTimeMillis);
        sendMessage(destination, mapping);
        JmsTemplate createJMSTemplate = createJMSTemplate(destination);
        Object receiveAndConvert = createJMSTemplate.receiveAndConvert(currentTimeMillis);
        if (receiveAndConvert instanceof String) {
            return createMethodReturn((String) receiveAndConvert);
        } else {
            throw new IllegalStateException("Message has to be of Type TextMessage and parseable into a String");
        }
    }

    private MethodReturn createMethodReturn(String receiveAndConvert) {
        try {
            ReturnMapping returnValue = mapper.readValue(receiveAndConvert, ReturnMapping.class);
            Class<?> classValue = Class.forName(returnValue.getClassName());
            returnValue.setArg(mapper.convertValue(returnValue.getArg(), classValue));
            return returnValue;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private JmsTemplate createJMSTemplate(URI destination) {
        return factory.createJMSTemplate(destination.getSchemeSpecificPart());
    }

    private void sendMessage(URI destination, MethodCall call) {
        StringWriter result = new StringWriter();
        try {
            new ObjectMapper().writeValue(result, call);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JmsTemplate createJMSTemplate = createJMSTemplate(destination);
        createJMSTemplate.convertAndSend(destination.getFragment(), result.toString());
    }

    @Override
    public void setRequestHandler(RequestHandler handler) {
        this.requestHandler = handler;
    }

    @Override
    public void start() {
        simpleMessageListenerContainer = factory.createMessageListenerContainer();
        simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
        simpleMessageListenerContainer.setDestinationName("receive");
        simpleMessageListenerContainer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                System.out.println("Message");
                if (message instanceof TextMessage) {
                    ObjectMapper mapper = new ObjectMapper();
                    TextMessage textMessage = (TextMessage) message;
                    try {
                        RequestMapping readValue =
                            mapper.readValue(new StringReader(textMessage.getText()), RequestMapping.class);
                        readValue.resetArgs();
                        MethodReturn handleCall = requestHandler.handleCall(readValue);
                        StringWriter stringWriter = new StringWriter();
                        mapper.writeValue(stringWriter, handleCall);

                        if (readValue.isAnswer()) {
                            new JmsTemplate(connectionFactory).convertAndSend(readValue.getCallId(),
                                stringWriter.toString());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (JMSException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        simpleMessageListenerContainer.start();
    }

    @Override
    public void stop() {
        simpleMessageListenerContainer.stop();
    }
}
