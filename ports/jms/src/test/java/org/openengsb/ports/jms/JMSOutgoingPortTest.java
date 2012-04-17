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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
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
import org.openengsb.core.api.remote.GenericObjectSerializer;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.JsonObjectSerializer;
import org.openengsb.core.common.OutgoingPortImpl;
import org.openengsb.core.common.remote.FilterChainFactory;
import org.openengsb.core.security.filter.OutgoingJsonSecureMethodCallMarshalFilterFactory;
import org.openengsb.core.security.filter.OutgoingWrapperFilter;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

public class JMSOutgoingPortTest extends AbstractOsgiMockServiceTest {

    private static final String METHOD_RESULT_MESSAGE = ""
            + "{"
            + "   \"message\":{"
            + "      \"result\":{"
            + "         \"type\":\"Object\","
            + "         \"arg\": \"42\","
            + "         \"metaData\":{"
            + ""
            + "         }"
            + "      },"
            + "      \"callId\":\"12345\""
            + "   },"
            + "   \"timestamp\":1321314411697"
            + "}"
            + "";

    private MethodCallRequest call;
    private JmsTemplate jmsTemplate;
    private JMSTemplateFactory jmsTemplateFactory;

    private Map<String, String> metaData;

    private SimpleMessageListenerContainer simpleMessageListenerContainer;

    private OutgoingPort outgoingPort;

    private GenericObjectSerializer objectSerializer;

    @Before
    public void setup() throws Exception {
        MethodResult methodResult = new MethodResult("42");
        MethodResultMessage methodResultMessage = new MethodResultMessage(methodResult, "12345");
        String writeValueAsString = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(
            SecureResponse.create(methodResultMessage));

        System.out.println(writeValueAsString);

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
        call = new MethodCallRequest(methodCall, "123");
        call.setDestination("host?receive");

        JMSOutgoingPort jmsOutgoingPort = new JMSOutgoingPort();
        jmsOutgoingPort.setFactory(jmsTemplateFactory);

        JsonObjectSerializer jsonObjectSerializer = new JsonObjectSerializer();
        jsonObjectSerializer.setBundleContext(bundleContext);
        jsonObjectSerializer.init();
        objectSerializer = jsonObjectSerializer;

        FilterChainFactory<MethodCallRequest, MethodResultMessage> factory =
            new FilterChainFactory<MethodCallRequest, MethodResultMessage>(MethodCallRequest.class,
                MethodResultMessage.class);
        factory.setFilters(Arrays.asList(OutgoingWrapperFilter.class,
            new OutgoingJsonSecureMethodCallMarshalFilterFactory(objectSerializer),
            jmsOutgoingPort));

        OutgoingPortImpl outgoingPort = new OutgoingPortImpl();
        outgoingPort.setFilterChain(factory.create());

        this.outgoingPort = outgoingPort;
    }

    @Test
    public void callSend_shouldSendMessageViaJMS() throws URISyntaxException, IOException {
        outgoingPort.send(call);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(jmsTemplate).convertAndSend(captor.capture());
        Mockito.verifyNoMoreInteractions(jmsTemplate);
        JsonNode requestMessage = new ObjectMapper().readTree(captor.getValue());
        JsonNode readTree = requestMessage.get("message").get("methodCall");

        assertThat(readTree.get("methodName").toString(), Matchers.equalTo("\"method\""));
        System.out.println(readTree.get("args"));
        JsonNode argNode1 = readTree.get("args").get(0);
        assertThat(argNode1.getTextValue(), is("123"));
        JsonNode argNode2 = readTree.get("args").get(1);
        assertThat(argNode2.toString(), is("5"));
        JsonNode argNode3 = readTree.get("args").get(2);
        assertThat(argNode3.get("test").getTextValue(), is("test"));
        assertThat(argNode3.get("@type").getTextValue(), is("TestClass"));
        assertThat(readTree.get("metaData").toString(), Matchers.equalTo("{\"serviceId\":\"test\"}"));
    }

    @Test
    public void callSendSync_shouldSendMessageListenToReturnQueueAndSerialize() throws URISyntaxException, IOException {
        ArgumentCaptor<String> sendIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.when(jmsTemplate.receiveAndConvert(destinationCaptor.capture())).thenReturn(METHOD_RESULT_MESSAGE);
        outgoingPort.sendSync(call);
        Mockito.verify(jmsTemplate).convertAndSend(sendIdCaptor.capture());

        String destination =
            new ObjectMapper().readValue(new StringReader(sendIdCaptor.getValue()), JsonNode.class).get("message")
                .get("callId").asText();
        assertThat(destinationCaptor.getValue(), Matchers.equalTo(destination));
    }

}
