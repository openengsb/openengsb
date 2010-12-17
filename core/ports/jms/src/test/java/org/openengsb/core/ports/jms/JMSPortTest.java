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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openengsb.core.common.communication.MethodCall;
import org.openengsb.core.common.communication.MethodReturn;
import org.openengsb.core.common.communication.MethodReturn.ReturnType;
import org.openengsb.core.common.communication.RequestHandler;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

public class JMSPortTest {

    private final String begin = "{";

    private final String sendText =
        "\"classes\":[\"java.lang.String\",\"java.lang.Integer\",\"org.openengsb.core.ports.jms.JMSPortTest$TestClass\"],\"methodName\":\"method\","
                + "\"metaData\":{\"test\":\"test\"},\"args\":[\"123\",5,{\"test\":\"test\"}]}";

    private final String sendTextWithReturn = begin + "\"callId\":\"12345\",\"answer\":true," + sendText;
    private final String sendTextWithoutId = begin + sendText;

    private final String returnText =
        "{\"type\":\"Object\",\"className\":\"org.openengsb.core.ports.jms.JMSPortTest$TestClass\","
                + "\"arg\":{\"test\":\"test\"},\"metaData\":{\"test\":\"test\"}}";

    private MethodCall call;
    private MethodReturn methodReturn;
    private JmsTemplate jmsTemplate;
    private JMSTemplateFactory jmsTemplateFactory;
    private JMSPort port;
    private RequestHandler handler;

    private Map<String, String> metaData;

    private SimpleMessageListenerContainer simpleMessageListenerContainer;

    @Before
    public void setup() {
        jmsTemplate = mock(JmsTemplate.class);
        jmsTemplateFactory = mock(JMSTemplateFactory.class);
        when(jmsTemplateFactory.createJMSTemplate("host")).thenReturn(jmsTemplate);
        simpleMessageListenerContainer = mock(SimpleMessageListenerContainer.class);
        when(jmsTemplateFactory.createMessageListenerContainer()).thenReturn(simpleMessageListenerContainer);
        port = new JMSPort(jmsTemplateFactory, mock(ConnectionFactory.class));
        handler = mock(RequestHandler.class);
        metaData = new HashMap<String, String>();
        metaData.put("test", "test");
        call = new MethodCall("method", new Object[]{"123", 5, new TestClass("test")}, metaData);
        methodReturn = new MethodReturn(ReturnType.Object, new TestClass("test"), metaData);
    }

    @Test
    public void callSend_shouldSendMessageViaJMS() throws URISyntaxException {
        port.send(new URI("jms-json", "host", "example"), call);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(jmsTemplate).convertAndSend(Mockito.eq("example"), captor.capture());
        verifyNoMoreInteractions(jmsTemplate);
        System.out.println(captor.getValue());
        System.out.println(sendTextWithoutId);
        assertThat(captor.getValue(), equalTo(sendTextWithoutId));
    }

    @Test
    public void callSendSync_shouldSendMessageListenToReturnQueueAndSerialize() throws URISyntaxException,
        JsonParseException, JsonMappingException, IOException {
        ArgumentCaptor<String> sendIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        when(jmsTemplate.receiveAndConvert(destinationCaptor.capture())).thenReturn(returnText);
        MethodReturn sendSync = port.sendSync(new URI("jms-json", "host", "example"), call);
        verify(jmsTemplate).convertAndSend(Mockito.eq("example"), sendIdCaptor.capture());
        String destination =
            new ObjectMapper().readValue(new StringReader(sendIdCaptor.getValue()), JsonNode.class).get("callId")
                .getValueAsText();
        assertThat(destinationCaptor.getValue(), equalTo(destination));
        assertMethodReturn(sendSync);
    }

    private void assertMethodReturn(MethodReturn sendSync) {
        assertThat(sendSync.getType(), equalTo(ReturnType.Object));
        Assert.assertTrue(sendSync.getArg() instanceof TestClass);
        TestClass test = (TestClass) sendSync.getArg();
        assertThat(test.getTest(), equalTo("test"));
        assertThat(sendSync.getMetaData().size(), equalTo(1));
        assertThat(sendSync.getMetaData().get("test"), equalTo("test"));
    }

    @Test
    public void start_ShouldListenToIncomingCallsAndCallSetRequestHandler() {
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=true");
        final JmsTemplate jmsTemplate = new JmsTemplate(cf);
        port = new JMSPort(new JMSTemplateFactory() {
            @Override
            public JmsTemplate createJMSTemplate(String host) {
                return jmsTemplate;
            }

            @Override
            public SimpleMessageListenerContainer createMessageListenerContainer() {
                return new SimpleMessageListenerContainer();
            }
        }, cf);
        port.setRequestHandler(handler);
        port.start();

        ArgumentCaptor<MethodCall> captor = ArgumentCaptor.forClass(MethodCall.class);
        when(handler.handleCall(captor.capture())).thenReturn(
            new MethodReturn(ReturnType.Object, new TestClass("test"), metaData));
        new JmsTemplate(cf).convertAndSend("receive", sendTextWithReturn);
        String receiveAndConvert = (String) jmsTemplate.receiveAndConvert("12345");
        assertThat(receiveAndConvert, equalTo(returnText));
        MethodCall call = captor.getValue();
        assertThat(call.getMethodName(), equalTo("method"));
        System.out.println(call.getArgs() == new Object[]{"123", 5, new TestClass("test")});
        System.out.println(call.getArgs()[2].getClass());
        assertThat(call.getArgs(), equalTo(new Object[]{"123", 5, new TestClass("test")}));
        assertThat(call.getMetaData(), equalTo(this.metaData));
    }

    @Test
    public void stop_ShouldNotReactToIncomingCalls() {
        port.start();
        port.stop();
        verify(simpleMessageListenerContainer).stop();
    }

    @Test
    public void requestMapping_shouldDeserialiseRequest() throws JsonParseException, JsonMappingException, IOException {
        new ObjectMapper().readValue(this.sendTextWithReturn, RequestMapping.class);
    }

    @Test
    public void methodReturn_DeserialiseResponse() throws JsonParseException, JsonMappingException, IOException {
        StringWriter writer = new StringWriter();
        new ObjectMapper().writeValue(writer, this.methodReturn);
        assertThat(writer.toString(), equalTo(this.returnText));
    }

    public static class TestClass {
        String test;

        public TestClass() {
        }

        public TestClass(String test) {
            this.test = test;
        }

        public void setTest(String test) {
            this.test = test;
        }

        public String getTest() {
            return test;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((test == null) ? 0 : test.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TestClass other = (TestClass) obj;
            if (test == null) {
                if (other.test != null)
                    return false;
            } else if (!test.equals(other.test))
                return false;
            return true;
        }

    }
}
