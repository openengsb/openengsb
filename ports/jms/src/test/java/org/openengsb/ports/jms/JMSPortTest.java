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
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.remote.FilterChainFactory;
import org.openengsb.core.common.remote.JsonMethodCallMarshalFilter;
import org.openengsb.core.common.remote.RequestMapperFilter;
import org.openengsb.core.common.remote.XmlEncoderFilter;
import org.openengsb.core.common.remote.XmlMethodCallMarshalFilter;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.services.internal.RequestHandlerImpl;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.osgi.framework.BundleContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class JMSPortTest extends AbstractOsgiMockServiceTest {

    private static final String METHOD_RESULT_MESSAGE = ""
            + "{"
            + "  \"result\":{"
            + "    \"type\":\"Object\","
            + "    \"className\":\"org.openengsb.ports.jms.TestClass\","
            + "    \"metaData\":{\"test\":\"test\"},"
            + "    \"arg\":{\"test\":\"test\"}"
            + "  },"
            + "  \"callId\":\"12345\""
            + "}";

    private static final String METHOD_CALL = ""
            + "{"
            + "  \"classes\":[\"java.lang.String\",\"java.lang.Integer\",\"org.openengsb.ports.jms.TestClass\"],"
            + "  \"methodName\":\"method\","
            + "  \"args\":[\"123\",5,{\"test\":\"test\"}],"
            + "  \"metaData\":{\"serviceId\":\"test\"}"
            + "}";

    private static final String METHOD_CALL_REQUEST = ""
            + "{"
            + "  \"callId\":\"12345\","
            + "  \"answer\":true,"
            + "  \"methodCall\":" + METHOD_CALL
            + "}";

    private final String xmlText = ""
            + "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
            + "<MethodCallRequest>"
            + "  <callId>123</callId>"
            + "  <answer>true</answer>"
            + "  <methodCall>"
            + "    <args xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
            + "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:string\">123</args>"
            + "    <args xmlns:xs=\"http://www.w3.org/2001/XMLSchema\""
            + "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"xs:int\">5</args>"
            + "    <args xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"testClass\">"
            + "      <test>test</test>"
            + "    </args>"
            + "    <classes>java.lang.String</classes>"
            + "    <classes>java.lang.Integer</classes>"
            + "    <classes>org.openengsb.ports.jms.TestClass</classes>"
            + "    <metaData>"
            + "      <entry>"
            + "        <key>serviceId</key>"
            + "        <value>test</value>"
            + "      </entry>"
            + "    </metaData>"
            + "    <methodName>method</methodName>"
            + "  </methodCall>"
            + "</MethodCallRequest>";

    private MethodCallRequest call;
    private MethodResultMessage methodReturn;
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
        handler = new RequestHandlerImpl();

        TestInterface mock2 = mock(TestInterface.class);
        registerServiceViaId(mock2, "test", TestInterface.class);
        when(mock2.method(Mockito.anyString(), Mockito.anyInt(), Mockito.any(TestClass.class))).thenReturn(
            new TestClass("test"));
        metaData = new HashMap<String, String>();
        metaData.put("serviceId", "test");
        MethodCall methodCall = new MethodCall("method", new Object[]{ "123", 5, new TestClass("test"), }, metaData);
        call = new MethodCallRequest(methodCall, "123");
        MethodResult result = new MethodResult(new TestClass("test"));
        result.setMetaData(metaData);
        methodReturn = new MethodResultMessage(result, "123");
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
        JsonNode requestMessage = new ObjectMapper().readTree(captor.getValue());
        JsonNode readTree = requestMessage.get("methodCall");

        assertThat(readTree.get("classes").toString(), Matchers.equalTo("[\"java.lang.String\","
                + "\"java.lang.Integer\"," + "\"org.openengsb.ports.jms.TestClass\"]"));
        assertThat(readTree.get("methodName").toString(), Matchers.equalTo("\"method\""));
        assertThat(readTree.get("args").toString(), Matchers.equalTo("[\"123\",5,{\"test\":\"test\"}]"));
        assertThat(readTree.get("metaData").toString(), Matchers.equalTo("{\"serviceId\":\"test\"}"));
    }

    @Test
    public void callSendSync_shouldSendMessageListenToReturnQueueAndSerialize() throws URISyntaxException, IOException {
        ArgumentCaptor<String> sendIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.when(jmsTemplate.receiveAndConvert(destinationCaptor.capture())).thenReturn(METHOD_RESULT_MESSAGE);
        port.sendSync("host", call);
        Mockito.verify(jmsTemplate).convertAndSend(org.mockito.Matchers.eq("receive"), sendIdCaptor.capture());

        String destination =
            new ObjectMapper().readValue(new StringReader(sendIdCaptor.getValue()), JsonNode.class).get("callId")
                .getValueAsText();
        assertThat(destinationCaptor.getValue(), Matchers.equalTo(destination));
    }

    @Test(timeout = 10000)
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
        FilterChainFactory<String, String> factory = new FilterChainFactory<String, String>(String.class, String.class);
        factory.setFilters(Arrays.asList(JsonMethodCallMarshalFilter.class, new RequestMapperFilter(handler)));
        port.setFilterChain(factory.create());
        port.start();

        new JmsTemplate(cf).convertAndSend("receive", METHOD_CALL_REQUEST);
        String receiveAndConvert = (String) jmsTemplate.receiveAndConvert("12345");
        JsonNode resultMessage = new ObjectMapper().readTree(receiveAndConvert);
        JsonNode readTree = resultMessage.get("result");
        assertThat(readTree.get("className").toString(),
            Matchers.equalTo("\"org.openengsb.ports.jms.TestClass\""));
        assertThat(readTree.get("metaData").toString(), Matchers.equalTo("{\"serviceId\":\"test\"}"));
        assertThat(readTree.get("type").toString(), Matchers.equalTo("\"Object\""));
        assertThat(readTree.get("arg").toString(), Matchers.equalTo("{\"test\":\"test\"}"));
    }

    @Test(timeout = 5000)
    public void testPortWithXmlFormat_shouldWorkWithXmlFilterChain() throws InterruptedException, IOException {
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("vm://localhost1");
        final JmsTemplate jmsTemplate = new JmsTemplate(cf);
        port = new JMSPort();
        port.setFactory(new JMSTemplateFactory() {
            @Override
            public JmsTemplate createJMSTemplate(DestinationUrl destination) {
                return jmsTemplate;
            }

            @Override
            public SimpleMessageListenerContainer createMessageListenerContainer() {
                return new SimpleMessageListenerContainer();
            }


        });
        port.setConnectionFactory(cf);
        FilterChainFactory<String, String> factory = new FilterChainFactory<String, String>(String.class, String.class);
        factory.setFilters(Arrays.asList(XmlEncoderFilter.class, XmlMethodCallMarshalFilter.class,
            new RequestMapperFilter(handler)));
        port.setFilterChain(factory.create());
        port.start();
        Thread.sleep(1000);
        new JmsTemplate(cf).convertAndSend("receive", xmlText);
        String receiveAndConvert = (String) jmsTemplate.receiveAndConvert("123");

        assertThat(receiveAndConvert, containsString("<callId>123</callId>"));
        assertThat(receiveAndConvert, containsString("<type>Object</type>"));
        assertThat(receiveAndConvert, containsString("<test>test</test>"));
    }

    @Test
    public void stop_ShouldNotReactToIncomingCalls() {
        port.start();
        port.stop();
        Mockito.verify(simpleMessageListenerContainer).stop();
    }

    @Test
    public void requestMapping_shouldDeserialiseRequest() throws IOException {
        new ObjectMapper().readValue(METHOD_CALL_REQUEST, MethodCallRequest.class);
    }

    @Test
    public void methodReturn_DeserialiseResponse() throws IOException {
        StringWriter writer = new StringWriter();
        new ObjectMapper().writeValue(writer, methodReturn);
        JsonNode resultMessage = new ObjectMapper().readTree(writer.toString());
        JsonNode readTree = resultMessage.get("result");
        assertThat(readTree.get("className").toString(),
            Matchers.equalTo("\"org.openengsb.ports.jms.TestClass\""));
        assertThat(readTree.get("metaData").toString(), Matchers.equalTo("{\"serviceId\":\"test\"}"));
        assertThat(readTree.get("type").toString(), Matchers.equalTo("\"Object\""));
        assertThat(readTree.get("arg").toString(), Matchers.equalTo("{\"test\":\"test\"}"));

    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        DefaultOsgiUtilsService serviceUtils = new DefaultOsgiUtilsService();
        serviceUtils.setBundleContext(bundleContext);
        registerService(serviceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
        OpenEngSBCoreServices.setOsgiServiceUtils(serviceUtils);
    }
}
