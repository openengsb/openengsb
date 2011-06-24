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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
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
import org.openengsb.core.common.remote.XmlDecoderFilter;
import org.openengsb.core.common.remote.XmlMethodCallMarshalFilter;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.services.internal.RequestHandlerImpl;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.osgi.framework.BundleContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class JMSPortTest extends AbstractOsgiMockServiceTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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

    private static final String XML_METHOD_CALL_REQUEST = ""
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
    private JMSTemplateFactory jmsTemplateFactory;
    private JMSIncomingPort incomingPort;
    private RequestHandler handler;

    private SimpleMessageListenerContainer simpleMessageListenerConainer;

    private JmsTemplate jmsTemplate;

    @Before
    public void setup() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String num = UUID.randomUUID().toString();
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost" + num);
        jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplateFactory = new JMSTemplateFactory() {
            @Override
            public SimpleMessageListenerContainer createMessageListenerContainer() {
                return simpleMessageListenerConainer;
            }

            @Override
            public JmsTemplate createJMSTemplate(DestinationUrl destination) {
                return jmsTemplate;
            }

        };
        simpleMessageListenerConainer = new SimpleMessageListenerContainer();

        incomingPort = new JMSIncomingPort();
        incomingPort.setFactory(jmsTemplateFactory);
        incomingPort.setConnectionFactory(connectionFactory);
        handler = new RequestHandlerImpl();

        TestInterface mock2 = mock(TestInterface.class);
        registerServiceViaId(mock2, "test", TestInterface.class);
        when(mock2.method(anyString(), anyInt(), any(TestClass.class))).thenReturn(new TestClass("test"));

        Map<String, String> metaData = Maps.newHashMap(ImmutableMap.of("serviceId", "test"));
        MethodCall methodCall = new MethodCall("method", new Object[]{ "123", 5, new TestClass("test"), }, metaData);
        call = new MethodCallRequest(methodCall, "123");
        call.setDestination("host");

        MethodResult result = new MethodResult(new TestClass("test"));
        result.setMetaData(metaData);
        methodReturn = new MethodResultMessage(result, "123");
    }

    @Test(timeout = 5000)
    public void start_ShouldListenToIncomingCallsAndCallSetRequestHandler() throws InterruptedException, IOException {
        FilterChainFactory<String, String> factory = new FilterChainFactory<String, String>(String.class, String.class);
        factory.setFilters(Arrays.asList(
            JsonMethodCallMarshalFilter.class,
            new RequestMapperFilter(handler)));
        incomingPort.setFilterChain(factory.create());
        incomingPort.start();

        jmsTemplate.convertAndSend("receive", METHOD_CALL_REQUEST);
        String resultString = (String) jmsTemplate.receiveAndConvert("12345");
        JsonNode resultMessage = OBJECT_MAPPER.readTree(resultString);
        JsonNode readTree = resultMessage.get("result");
        assertThat(readTree.get("className").toString(), equalTo("\"org.openengsb.ports.jms.TestClass\""));
        assertThat(readTree.get("metaData").toString(), equalTo("{\"serviceId\":\"test\"}"));
        assertThat(readTree.get("type").toString(), equalTo("\"Object\""));
        assertThat(readTree.get("arg").toString(), equalTo("{\"test\":\"test\"}"));
    }

    @Test(timeout = 5000)
    public void testPortWithXmlFormat_shouldWorkWithXmlFilterChain() throws InterruptedException, IOException {
        FilterChainFactory<String, String> factory = new FilterChainFactory<String, String>(String.class, String.class);
        factory.setFilters(Arrays.asList(
            XmlDecoderFilter.class,
            XmlMethodCallMarshalFilter.class,
            new RequestMapperFilter(handler)));
        incomingPort.setFilterChain(factory.create());
        incomingPort.start();

        jmsTemplate.convertAndSend("receive", XML_METHOD_CALL_REQUEST);
        String resultString = (String) jmsTemplate.receiveAndConvert("123");

        assertThat(resultString, containsString("<callId>123</callId>"));
        assertThat(resultString, containsString("<type>Object</type>"));
        assertThat(resultString, containsString("<test>test</test>"));
    }

    @Test
    public void stop_ShouldNotReactToIncomingCalls() {
        SimpleMessageListenerContainer orig = simpleMessageListenerConainer;
        SimpleMessageListenerContainer containerSpy = spy(orig);
        simpleMessageListenerConainer = containerSpy;

        ConnectionFactory cf = new ActiveMQConnectionFactory("vm://localhost2");
        incomingPort.setConnectionFactory(cf);
        incomingPort.start();
        incomingPort.stop();
        verify(containerSpy).stop();
    }

    @Test
    public void requestMapping_shouldDeserialiseRequest() throws IOException {
        OBJECT_MAPPER.readValue(METHOD_CALL_REQUEST, MethodCallRequest.class);
    }

    @Test
    public void methodReturn_DeserialiseResponse() throws IOException {
        StringWriter writer = new StringWriter();
        OBJECT_MAPPER.writeValue(writer, methodReturn);
        JsonNode resultMessage = OBJECT_MAPPER.readTree(writer.toString());
        JsonNode readTree = resultMessage.get("result");
        assertThat(readTree.get("className").toString(), equalTo("\"org.openengsb.ports.jms.TestClass\""));
        assertThat(readTree.get("metaData").toString(), equalTo("{\"serviceId\":\"test\"}"));
        assertThat(readTree.get("type").toString(), equalTo("\"Object\""));
        assertThat(readTree.get("arg").toString(), equalTo("{\"test\":\"test\"}"));

    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        DefaultOsgiUtilsService serviceUtils = new DefaultOsgiUtilsService();
        serviceUtils.setBundleContext(bundleContext);
        registerService(serviceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
        OpenEngSBCoreServices.setOsgiServiceUtils(serviceUtils);
    }
}
