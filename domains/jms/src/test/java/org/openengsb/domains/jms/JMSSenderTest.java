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

import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

public class JMSSenderTest {
    private static final String EXCEPTION = "{\"type\":\"Exception\",\"message\":\"12345\"}";
    private static final String CALL = "{\"type\":\"Call\",\"name\":\"log\",\"message\":\"12345\"}";
    private static final String QUEUE_ID = "12345";
    private static final String RETURN = "{\"type\":\"Return\",\"message\":\"" + QUEUE_ID + "\"}";

    @Test
    public void sendJMSMethod_shouldCallSpringJMSWithCorrectDestination() throws JMSException {
        JmsTemplate template = mock(JmsTemplate.class);
        ArgumentCaptor<MessageCreator> creator = ArgumentCaptor.forClass(MessageCreator.class);
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn(RETURN);
        when(template.receive(QUEUE_ID + "_method_return")).thenReturn(message);

        JMSSender sender = new JMSSender(QUEUE_ID, template);
        String send = sender.send("log", "12345");

        verify(template).send(Mockito.eq(QUEUE_ID + "_method_send"), creator.capture());

        MessageCreator value = creator.getValue();
        Session session = mock(Session.class);
        value.createMessage(session);
        verify(session).createTextMessage(CALL);
        assertThat(send, equalTo(QUEUE_ID));
    }

    @Test
    public void returnJMSException_shouldThrowJMSConnectorExceptionWithMessage() throws JMSException {
        JmsTemplate template = mock(JmsTemplate.class);
        TextMessage message = mock(TextMessage.class);
        when(message.getText()).thenReturn(EXCEPTION);
        when(template.receive(QUEUE_ID + "_method_return")).thenReturn(message);

        JMSSender sender = new JMSSender(QUEUE_ID, template);
        try {
            sender.send("log", "12345");
            fail();
        } catch (JMSConnectorException e) {
            assertThat("12345", equalTo(e.getMessage()));
        }
    }

    @Test
    public void startJMSServerAndConnectWithJMSSender_shouldGiveCorrectMessageBack() throws Exception {
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=true");
        JmsTemplate jmsTemplate = new JmsTemplate(cf);

        jmsTemplate.convertAndSend(QUEUE_ID + JMSSender.METHOD_RETURN, RETURN);
        JMSSender sender = new JMSSender(QUEUE_ID, jmsTemplate);
        String send = sender.send("log", "12345");
        assertThat(send, equalTo(QUEUE_ID));
        TextMessage receive = (TextMessage) jmsTemplate.receive(QUEUE_ID + JMSSender.METHOD_SEND);
        assertThat(receive.getText(), equalTo(CALL));
    }
}
