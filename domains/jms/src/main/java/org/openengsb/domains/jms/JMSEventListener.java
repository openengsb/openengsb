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
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.common.Event;
import org.springframework.jms.core.JmsTemplate;

public class JMSEventListener implements MessageListener {

    Log log = LogFactory.getLog(JMSEventListener.class);

    private final EventCaller caller;

    private final String id;

    private final JmsTemplate template;

    public static final String EVENT_RETURN = "_event_return";

    private final ObjectMapper mapper = new ObjectMapper();

    public JMSEventListener(String id, EventCaller caller, JmsTemplate template) {
        super();
        this.id = id;
        this.caller = caller;
        this.template = template;
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            try {
                JsonNode parsedMessage = mapper.readValue(textMessage.getText(), JsonNode.class);
                Class<?> eventClass = getEventType(parsedMessage);
                if (Event.class.isAssignableFrom(eventClass)) {
                    JsonNode event = parsedMessage.path("event");
                    Object readValue2 = mapper.readValue(event.traverse(), eventClass);
                    String destinationName = id + EVENT_RETURN;
                    try {
                        caller.raiseEvent((Event) readValue2);
                    } catch (Exception e) {
                        template.convertAndSend(destinationName, createExceptionMessage(e.getMessage()));
                    }

                    String okMessage = createOKMessage();

                    template.convertAndSend(destinationName, okMessage);
                } else {
                    log.error("Serializable Event type has to be subclass of Event");
                }
            } catch (JsonParseException e) {
                throw new JMSConnectorException(e);
            } catch (JsonMappingException e) {
                throw new JMSConnectorException(e);
            } catch (IOException e) {
                throw new JMSConnectorException(e);
            } catch (JMSException e) {
                throw new JMSConnectorException(e);
            } catch (SecurityException e) {
                throw new JMSConnectorException(e);
            } catch (IllegalArgumentException e) {
                throw new JMSConnectorException(e);
            } catch (ClassNotFoundException e) {
                throw new JMSConnectorException(e);
            }

        } else {
            throw new IllegalArgumentException("Message has to be Textmessage");
        }
    }

    private Class<?> getEventType(JsonNode parsedMessage) throws ClassNotFoundException {
        JsonNode type = parsedMessage.path("type");
        String typeValue = type.getTextValue();
        Class<?> eventClass = Class.forName(typeValue);
        return eventClass;
    }

    private String createExceptionMessage(String message) throws IOException {
        MessageMapping mapping = new MessageMapping();
        mapping.setType(MessageType.Exception);
        mapping.setMessage(message);
        return serialise(mapping);
    }

    private String createOKMessage() throws IOException {
        MessageMapping mapping = new MessageMapping();
        mapping.setType(MessageType.Return);
        mapping.setMessage("OK");
        return serialise(mapping);
    }

    private String serialise(MessageMapping mapping) throws IOException {
        StringWriter stringWriter = new StringWriter();
        mapper.writeValue(stringWriter, mapping);
        return stringWriter.toString();
    }
}
