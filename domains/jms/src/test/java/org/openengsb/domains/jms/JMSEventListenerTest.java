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

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openengsb.core.common.Event;
import org.osgi.framework.InvalidSyntaxException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

public class JMSEventListenerTest {

    private static final String RETURN = "{\"name\":null,\"message\":\"OK\",\"type\":\"Return\"}";
    private static final String EXCEPTION = "{\"name\":null,\"message\":\"message\",\"type\":\"Exception\"}";
    private static final String ID = "12345";
    private static final String SEND =
        "{\"type\":\"org.openengsb.domains.jms.JMSEventListenerTest$TestEvent\",\"event\":{\"name\":\"" + ID
                + "\"}}";

    @Test
    public void handNotTextMessage_shouldThrowException() {

        JMSEventListener listener = new JMSEventListener("12345", mock(EventCaller.class), mock(JmsTemplate.class));
        try {
            listener.onMessage(mock(Message.class));
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testIntegrationWithActiveMQConnectionFactory_shouldSendOKReturn() {
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=true");
        JmsTemplate jmsTemplate = new JmsTemplate(cf);
        EventCaller caller = mock(EventCaller.class);
        JMSEventListener listener = new JMSEventListener("12345", caller, jmsTemplate);
        SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
        simpleMessageListenerContainer.setConnectionFactory(cf);
        simpleMessageListenerContainer.setDestinationName("12345_event_send");
        simpleMessageListenerContainer.setMessageListener(listener);
        simpleMessageListenerContainer.start();
        jmsTemplate.convertAndSend("12345_event_send", SEND);
        Object receiveAndConvert = jmsTemplate.receiveAndConvert("12345_event_return");
        assertThat(receiveAndConvert.toString(), equalTo(RETURN));
    }

    @Test
    public void callWithTextMessageWithoutParameters_shouldLoadFromBundleContextAndCallRaiseEvent()
        throws JMSException, InvalidSyntaxException {
        EventCaller caller = mock(EventCaller.class);
        JmsTemplate jmsTemplateMock = mock(JmsTemplate.class);
        JMSEventListener listener = new JMSEventListener(ID, caller, jmsTemplateMock);
        TextMessage mock = mock(TextMessage.class);
        when(mock.getText()).thenReturn(SEND);
        listener.onMessage(mock);
        ArgumentCaptor<TestEvent> captor = ArgumentCaptor.forClass(TestEvent.class);
        verify(caller).raiseEvent(captor.capture());
        assertThat(captor.getValue().getName(), equalTo(ID));
        verify(jmsTemplateMock).convertAndSend(ID + "_event_return", RETURN);
    }

    @Test
    public void throwExceptionInRaiseEvent_shouldSendExceptionEvent()
        throws JMSException, InvalidSyntaxException {
        EventCaller caller = mock(EventCaller.class);
        doThrow(new RuntimeException("message")).when(caller).raiseEvent(Mockito.any(Event.class));
        JmsTemplate jmsTemplateMock = mock(JmsTemplate.class);
        JMSEventListener listener = new JMSEventListener(ID, caller, jmsTemplateMock);
        TextMessage mock = mock(TextMessage.class);
        when(mock.getText()).thenReturn(SEND);
        listener.onMessage(mock);
        ArgumentCaptor<TestEvent> captor = ArgumentCaptor.forClass(TestEvent.class);
        verify(caller).raiseEvent(captor.capture());
        assertThat(captor.getValue().getName(), equalTo(ID));
        verify(jmsTemplateMock).convertAndSend(ID + "_event_return", EXCEPTION);
    }

    public interface TestInterface {
        void raiseEvent();
    }

    public static class TestEvent extends Event {
    }
}
