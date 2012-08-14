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

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallMessage;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.common.OutgoingPortImpl;
import org.openengsb.core.common.remote.FilterChainFactory;
import org.openengsb.core.services.filter.OutgoingJsonSecureMethodCallMarshalFilter;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

public class JMSOutgoingPortTest extends AbstractOsgiMockServiceTest {

    private static final String METHOD_RESULT_MESSAGE = ""
            + "{"
            + "   \"result\":{"
            + "       \"type\":\"Object\","
            + "       \"className\":\"java.lang.String\","
            + "       \"arg\":\"42\","
            + "       \"metaData\":{"
            + "       }"
            + "    },"
            + "   \"callId\":\"12345\","
            + "   \"timestamp\":1321314411697"
            + "}"
            + "";

    private MethodCallMessage call;
    private JmsTemplate jmsTemplate;
    private JMSTemplateFactory jmsTemplateFactory;

    private Map<String, String> metaData;

    private SimpleMessageListenerContainer simpleMessageListenerContainer;

    private OutgoingPort outgoingPort;

    @Before
    public void setup() throws Exception {
        jmsTemplate = Mockito.mock(JmsTemplate.class);
        jmsTemplateFactory = Mockito.mock(JMSTemplateFactory.class);
        Mockito.when(jmsTemplateFactory.createJMSTemplate(any(DestinationUrl.class))).thenReturn(jmsTemplate);
        simpleMessageListenerContainer = Mockito.mock(SimpleMessageListenerContainer.class);
        Mockito.when(jmsTemplateFactory.createMessageListenerContainer()).thenReturn(simpleMessageListenerContainer);

        TestInterface mock2 = mock(TestInterface.class);
        registerServiceViaId(mock2, "test", TestInterface.class);
        when(mock2.method(Mockito.anyString(), Mockito.anyInt(), Mockito.any(TestClass.class))).thenReturn(
            new TestClass("test"));
        metaData = new HashMap<String, String>();
        metaData.put("serviceId", "test");
        MethodCall methodCall = new MethodCall("method", new Object[]{ "123", 5, new TestClass("test"), }, metaData);
        call = new MethodCallMessage(methodCall, "123");
        call.setDestination("host?receive");

        JMSOutgoingPort jmsOutgoingPort = new JMSOutgoingPort();
        jmsOutgoingPort.setFactory(jmsTemplateFactory);

        FilterChainFactory<MethodCallMessage, MethodResultMessage> factory =
            new FilterChainFactory<MethodCallMessage, MethodResultMessage>(MethodCallMessage.class,
                MethodResultMessage.class);
        factory.setFilters(Arrays.asList(OutgoingJsonSecureMethodCallMarshalFilter.class,
            jmsOutgoingPort));

        OutgoingPortImpl outgoingPort = new OutgoingPortImpl();
        outgoingPort.setFilterChain(factory.create());

        this.outgoingPort = outgoingPort;
    }

    @Test
    public void testCallSend_shouldSendMessageViaJMS() throws Exception {
        outgoingPort.send(call);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(jmsTemplate).convertAndSend(captor.capture());
        Mockito.verifyNoMoreInteractions(jmsTemplate);
        JsonNode requestMessage = new ObjectMapper().readTree(captor.getValue());
        JsonNode readTree = requestMessage.get("methodCall");

        assertThat(readTree.get("classes").toString(), Matchers.equalTo("[\"java.lang.String\","
                + "\"java.lang.Integer\"," + "\"org.openengsb.ports.jms.TestClass\"]"));
        assertThat(readTree.get("methodName").toString(), Matchers.equalTo("\"method\""));
        assertThat(readTree.get("args").toString(), Matchers.equalTo("[\"123\",5,{\"test\":\"test\"}]"));
        assertThat(readTree.get("metaData").toString(), Matchers.equalTo("{\"serviceId\":\"test\"}"));
    }

    @Test
    public void testCallSendSync_shouldSendMessageListenToReturnQueueAndSerialize() throws Exception {
        ArgumentCaptor<String> sendIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.when(jmsTemplate.receiveAndConvert(destinationCaptor.capture())).thenReturn(METHOD_RESULT_MESSAGE);
        outgoingPort.sendSync(call);
        Mockito.verify(jmsTemplate).convertAndSend(sendIdCaptor.capture());

        String destination =
            new ObjectMapper().readValue(new StringReader(sendIdCaptor.getValue()), JsonNode.class).get("callId")
                .asText();
        assertThat(destinationCaptor.getValue(), Matchers.equalTo(destination));
    }
}
