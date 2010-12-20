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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
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

    private final String sendText = "\"classes\":[\"java.lang.String\",\"java.lang.Integer\","
            + "\"org.openengsb.core.ports.jms.JMSPortTest$TestClass\"],"
            + "\"methodName\":\"method\",\"metaData\":{\"test\":\"test\"},"
            + "\"args\":[\"123\",5,{\"test\":\"test\"}]}";

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
        jmsTemplate = Mockito.mock(JmsTemplate.class);
        jmsTemplateFactory = Mockito.mock(JMSTemplateFactory.class);
        Mockito.when(jmsTemplateFactory.createJMSTemplate("host")).thenReturn(jmsTemplate);
        simpleMessageListenerContainer = Mockito.mock(SimpleMessageListenerContainer.class);
        Mockito.when(jmsTemplateFactory.createMessageListenerContainer()).thenReturn(simpleMessageListenerContainer);
        port = new JMSPort();
        port.setFactory(jmsTemplateFactory);
        port.setConnectionFactory(Mockito.mock(ConnectionFactory.class));
        handler = Mockito.mock(RequestHandler.class);
        metaData = new HashMap<String, String>();
        metaData.put("test", "test");
        call = new MethodCall("method", new Object[]{"123", 5, new TestClass("test")}, metaData);
        methodReturn = new MethodReturn(ReturnType.Object, new TestClass("test"), metaData);
    }

    @Test
    public void callSend_shouldSendMessageViaJMS() throws URISyntaxException {
        port.send(new URI("jms-json", "host", "example"), call);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(jmsTemplate).convertAndSend(org.mockito.Matchers.eq("example"), captor.capture());
        Mockito.verifyNoMoreInteractions(jmsTemplate);
        System.out.println(captor.getValue());
        System.out.println(sendTextWithoutId);
        MatcherAssert.assertThat(captor.getValue(), Matchers.equalTo(sendTextWithoutId));
    }

    @Test
    public void callSendSync_shouldSendMessageListenToReturnQueueAndSerialize() throws URISyntaxException, IOException {
        ArgumentCaptor<String> sendIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.when(jmsTemplate.receiveAndConvert(destinationCaptor.capture())).thenReturn(returnText);
        MethodReturn sendSync = port.sendSync(new URI("jms-json", "host", "example"), call);
        Mockito.verify(jmsTemplate).convertAndSend(org.mockito.Matchers.eq("example"), sendIdCaptor.capture());
        String destination =
            new ObjectMapper().readValue(new StringReader(sendIdCaptor.getValue()), JsonNode.class).get("callId")
                .getValueAsText();
        MatcherAssert.assertThat(destinationCaptor.getValue(), Matchers.equalTo(destination));
        assertMethodReturn(sendSync);
    }

    private void assertMethodReturn(MethodReturn sendSync) {
        MatcherAssert.assertThat(sendSync.getType(), Matchers.equalTo(ReturnType.Object));
        Assert.assertTrue(sendSync.getArg() instanceof TestClass);
        TestClass test = (TestClass) sendSync.getArg();
        MatcherAssert.assertThat(test.getTest(), Matchers.equalTo("test"));
        MatcherAssert.assertThat(sendSync.getMetaData().size(), Matchers.equalTo(1));
        MatcherAssert.assertThat(sendSync.getMetaData().get("test"), Matchers.equalTo("test"));
    }

    @Test
    public void start_ShouldListenToIncomingCallsAndCallSetRequestHandler() throws InterruptedException {
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("vm://localhost");
        final JmsTemplate jmsTemplate = new JmsTemplate(cf);
        port = new JMSPort();
        port.setFactory(new JMSTemplateFactory() {
            @Override
            public JmsTemplate createJMSTemplate(String host) {
                return jmsTemplate;
            }

            @Override
            public SimpleMessageListenerContainer createMessageListenerContainer() {
                return new SimpleMessageListenerContainer();
            }
        });
        port.setConnectionFactory(cf);
        port.setRequestHandler(handler);
        port.start();

        ArgumentCaptor<MethodCall> captor = ArgumentCaptor.forClass(MethodCall.class);
        Mockito.when(handler.handleCall(captor.capture())).thenReturn(
            new MethodReturn(ReturnType.Object, new TestClass("test"), metaData));
        new JmsTemplate(cf).convertAndSend("receive", sendTextWithReturn);
        String receiveAndConvert = (String) jmsTemplate.receiveAndConvert("12345");
        MatcherAssert.assertThat(receiveAndConvert, Matchers.equalTo(returnText));
        MethodCall call = captor.getValue();
        MatcherAssert.assertThat(call.getMethodName(), Matchers.equalTo("method"));
        System.out.println(call.getArgs() == new Object[]{"123", 5, new TestClass("test")});
        System.out.println(call.getArgs()[2].getClass());
        MatcherAssert.assertThat(call.getArgs(), Matchers.equalTo(new Object[]{"123", 5, new TestClass("test")}));
        MatcherAssert.assertThat(call.getMetaData(), Matchers.equalTo(this.metaData));
    }

    @Test
    public void stop_ShouldNotReactToIncomingCalls() {
        port.start();
        port.stop();
        Mockito.verify(simpleMessageListenerContainer).stop();
    }

    @Test
    public void requestMapping_shouldDeserialiseRequest() throws IOException {
        new ObjectMapper().readValue(this.sendTextWithReturn, RequestMapping.class);
    }

    @Test
    public void methodReturn_DeserialiseResponse() throws IOException {
        StringWriter writer = new StringWriter();
        new ObjectMapper().writeValue(writer, this.methodReturn);
        MatcherAssert.assertThat(writer.toString(), Matchers.equalTo(this.returnText));
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
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            TestClass other = (TestClass) obj;
            if (test == null) {
                if (other.test != null) {
                    return false;
                }
            } else if (!test.equals(other.test)) {
                return false;
            }
            return true;
        }
    }

    public static void main(String[] args) {
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:6549");
        JmsTemplate template = new JmsTemplate(cf);
        String request =
            "{\"callId\":\"12345\",\"answer\":true,\"classes\":[\"java.lang.String\"],"
                    + "\"methodName\":\"doSomething\",\"metaData\":{\"serviceId\":\"12345\"},"
                    + "\"args\":[\"Audit\"]}";
        template.convertAndSend("receive", request);
        System.out.println(template.receiveAndConvert("12345"));
    }
}
