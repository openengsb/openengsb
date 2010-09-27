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


package org.openengsb.domains.jms;

import java.io.IOException;
import java.io.StringWriter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class JMSSender implements Sender {

    final String queueId;
    final JmsTemplate template;

    public static final String METHOD_SEND = "_method_send";
    public static final String METHOD_RETURN = "_method_return";

    public JMSSender(String queueId, JmsTemplate template) {
        super();
        this.queueId = queueId;
        this.template = template;
    }

    @Override
    public String send(String anyString, final Object anyObject) {
        MessageMapping mapping = new MessageMapping(MessageType.Call, anyString, anyObject.toString());
        ObjectMapper mapper = new ObjectMapper();
        try {
            final StringWriter writer = new StringWriter();
            mapper.writeValue(writer, mapping);
            template.send(queueId + METHOD_SEND, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage(writer.toString());
                }
            });
            Message receive = template.receive(queueId + METHOD_RETURN);
            if (receive instanceof TextMessage) {
                String text = ((TextMessage) receive).getText();
                MessageMapping readValue = new ObjectMapper().readValue(text, MessageMapping.class);
                switch (readValue.type) {
                    case Exception:
                        throw new JMSConnectorException(readValue.message);
                    case Return:
                        return readValue.message;
                    default:
                        throw new JMSConnectorException("Type has to be Return or Exception at this stage");
                }
            } else {
                throw new IllegalStateException("Message cannot be of any type other than TextMessage");
            }
        } catch (JsonGenerationException e1) {
            throw new JMSConnectorException(e1.getMessage());
        } catch (JsonMappingException e1) {
            throw new JMSConnectorException(e1.getMessage());
        } catch (IOException e1) {
            throw new JMSConnectorException(e1.getMessage());
        } catch (JMSException e) {
            throw new JMSConnectorException(e.getMessage());
        }
    }

    public static final class MessageMapping {
        public MessageType type;
        public String name;
        public String message;

        public MessageMapping() {
            // TODO Auto-generated constructor stub
        }

        public MessageMapping(MessageType type, String name, String parameters) {
            super();
            this.type = type;
            this.name = name;
            this.message = parameters;
        }
    }

    private static enum MessageType {
        Call, Return, Exception
    }
}
