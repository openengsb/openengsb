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

package org.openengsb.ports.jms;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodReturn;
import org.openengsb.core.api.remote.MethodReturn.ReturnType;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.common.marshaling.RequestMapping;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class JMSPortTest {

    private final String begin = "{";

    private final String sendText = "\"classes\":[\"java.lang.String\",\"java.lang.Integer\","
            + "\"org.openengsb.ports.jms.JMSPortTest$TestClass\"],"
            + "\"methodName\":\"method\",\"args\":[\"123\",5,{\"test\":\"test\"}],"
            + "\"metaData\":{\"test\":\"test\"}}";

    private final String sendTextWithReturn = begin + "\"callId\":\"12345\",\"answer\":true," + sendText;

    private final String returnText =
        "{\"type\":\"Object\",\"className\":\"org.openengsb.ports.jms.JMSPortTest$TestClass\","
                + "\"metaData\":{\"test\":\"test\"},\"arg\":{\"test\":\"test\"}}";

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
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        jmsTemplate = Mockito.mock(JmsTemplate.class);
        jmsTemplateFactory = Mockito.mock(JMSTemplateFactory.class);

        Mockito.when(jmsTemplateFactory.createJMSTemplate(Mockito.any(DestinationUrl.class))).thenReturn(jmsTemplate);
        simpleMessageListenerContainer = Mockito.mock(SimpleMessageListenerContainer.class);
        Mockito.when(jmsTemplateFactory.createMessageListenerContainer()).thenReturn(simpleMessageListenerContainer);
        port = new JMSPort();
        port.setFactory(jmsTemplateFactory);
        port.setConnectionFactory(Mockito.mock(ConnectionFactory.class));
        handler = Mockito.mock(RequestHandler.class);
        metaData = new HashMap<String, String>();
        metaData.put("test", "test");
        call = new MethodCall("method", new Object[]{ "123", 5, new TestClass("test") }, metaData, "123", true);
        methodReturn = new MethodReturn(ReturnType.Object, new TestClass("test"), metaData, "123");
    }

    @Test(expected = IllegalArgumentException.class)
    public void callingSendWithInvalidEndpoint_shouldThrowIllegalArgumentException() throws Exception {
        port.send("host", call);
    }

    @Test
    public void callSend_shouldSendMessageViaJMS() throws URISyntaxException, IOException {
        port.send("host?endpoint", call);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(jmsTemplate).convertAndSend(captor.capture());
        Mockito.verifyNoMoreInteractions(jmsTemplate);
        JsonNode readTree = new ObjectMapper().readTree(captor.getValue());
        assertThat(readTree.get("classes").toString(), Matchers.equalTo("[\"java.lang.String\","
                + "\"java.lang.Integer\"," + "\"org.openengsb.ports.jms.JMSPortTest$TestClass\"]"));
        assertThat(readTree.get("methodName").toString(), Matchers.equalTo("\"method\""));
        assertThat(readTree.get("args").toString(), Matchers.equalTo("[\"123\",5,{\"test\":\"test\"}]"));
        assertThat(readTree.get("metaData").toString(), Matchers.equalTo("{\"test\":\"test\"}"));
    }

    @Test
    public void callSendSync_shouldSendMessageListenToReturnQueueAndSerialize() throws URISyntaxException, IOException {
        ArgumentCaptor<String> sendIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.when(jmsTemplate.receiveAndConvert(destinationCaptor.capture())).thenReturn(returnText);
        MethodReturn sendSync = port.sendSync("host?endpoint", call);
        Mockito.verify(jmsTemplate).convertAndSend(sendIdCaptor.capture());
        RequestMapping mapping = new ObjectMapper().readValue(sendIdCaptor.getValue(), RequestMapping.class);
        mapping.resetArgs();
        assertThat(mapping.getClasses(), equalTo(call.getClasses()));
        assertThat(mapping.getArgs(), equalTo(call.getArgs()));
        assertThat(mapping.getMetaData(), equalTo(call.getMetaData()));
        assertThat(mapping.getMethodName(), equalTo(call.getMethodName()));
        assertNotNull(mapping.getCallId());

        String destination =
            new ObjectMapper().readValue(new StringReader(sendIdCaptor.getValue()), JsonNode.class).get("callId")
                .getValueAsText();
        assertThat(destinationCaptor.getValue(), Matchers.equalTo(destination));
        assertMethodReturn(sendSync);
    }

    private void assertMethodReturn(MethodReturn sendSync) {
        assertThat(sendSync.getType(), Matchers.equalTo(ReturnType.Object));
        Assert.assertTrue(sendSync.getArg() instanceof HashMap);
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, String> test = (LinkedHashMap<String, String>) sendSync.getArg();
        assertThat(test.get("test"), Matchers.equalTo("test"));
        assertThat(sendSync.getMetaData().size(), Matchers.equalTo(1));
        assertThat(sendSync.getMetaData().get("test"), Matchers.equalTo("test"));
    }

    @Test(timeout = 5000)
    public void start_ShouldListenToIncomingCallsAndCallSetRequestHandler() throws InterruptedException, IOException {
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("vm://localhost");
        final JmsTemplate jmsTemplate = new JmsTemplate(cf);
        port = new JMSPort();
        port.setFactory(new JMSTemplateFactory() {
            @Override
            public JmsTemplate createJMSTemplate(DestinationUrl destinationUrl) {
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
            new MethodReturn(ReturnType.Object, new TestClass("test"), metaData, "123"));
        new JmsTemplate(cf).convertAndSend("receive", sendTextWithReturn);
        String receiveAndConvert = (String) jmsTemplate.receiveAndConvert("12345");
        JsonNode readTree = new ObjectMapper().readTree(receiveAndConvert);
        assertThat(readTree.get("className").toString(),
            Matchers.equalTo("\"org.openengsb.ports.jms.JMSPortTest$TestClass\""));
        assertThat(readTree.get("metaData").toString(), Matchers.equalTo("{\"test\":\"test\"}"));
        assertThat(readTree.get("type").toString(), Matchers.equalTo("\"Object\""));
        assertThat(readTree.get("arg").toString(), Matchers.equalTo("{\"test\":\"test\"}"));
        MethodCall call = captor.getValue();
        assertThat(call.getMethodName(), Matchers.equalTo("method"));
        assertThat(call.getArgs(), Matchers.equalTo(new Object[]{ "123", 5, new TestClass("test") }));
        assertThat(call.getMetaData(), Matchers.equalTo(metaData));
    }

    @Test
    public void stop_ShouldNotReactToIncomingCalls() {
        port.start();
        port.stop();
        Mockito.verify(simpleMessageListenerContainer).stop();
    }

    @Test
    public void requestMapping_shouldDeserialiseRequest() throws IOException {
        new ObjectMapper().readValue(sendTextWithReturn, RequestMapping.class);
    }

    @Test
    public void methodReturn_DeserialiseResponse() throws IOException {
        StringWriter writer = new StringWriter();
        new ObjectMapper().writeValue(writer, methodReturn);
        JsonNode readTree = new ObjectMapper().readTree(writer.toString());
        assertThat(readTree.get("className").toString(),
            Matchers.equalTo("\"org.openengsb.ports.jms.JMSPortTest$TestClass\""));
        assertThat(readTree.get("metaData").toString(), Matchers.equalTo("{\"test\":\"test\"}"));
        assertThat(readTree.get("type").toString(), Matchers.equalTo("\"Object\""));
        assertThat(readTree.get("arg").toString(), Matchers.equalTo("{\"test\":\"test\"}"));

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
            result = prime * result + (test == null ? 0 : test.hashCode());
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
                    + "\"methodName\":\"audit\",\"metaData\":{\"serviceId\":\"auditing\"}," + "\"args\":[\"Audit\"]}";
        template.convertAndSend("receive", request);
        System.out.println(template.receiveAndConvert("12345"));
    }
}
