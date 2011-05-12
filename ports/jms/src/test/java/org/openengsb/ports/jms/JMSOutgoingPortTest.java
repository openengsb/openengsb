package org.openengsb.ports.jms;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

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
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.OutgoingPort;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.OutgoingPortImpl;
import org.openengsb.core.common.remote.FilterChainFactory;
import org.openengsb.core.common.remote.JsonOutgoingMethodCallMarshalFilter;
import org.openengsb.core.common.util.DefaultOsgiUtilsService;
import org.openengsb.core.test.AbstractOsgiMockServiceTest;
import org.osgi.framework.BundleContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.SimpleMessageListenerContainer;

public class JMSOutgoingPortTest extends AbstractOsgiMockServiceTest {

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

    private MethodCallRequest call;
    private JmsTemplate jmsTemplate;
    private JMSTemplateFactory jmsTemplateFactory;

    private Map<String, String> metaData;

    private SimpleMessageListenerContainer simpleMessageListenerContainer;

    private OutgoingPort outgoingPort;

    @Before
    public void setup() {
        jmsTemplate = Mockito.mock(JmsTemplate.class);
        jmsTemplateFactory = Mockito.mock(JMSTemplateFactory.class);

        Mockito.when(jmsTemplateFactory.createJMSTemplate("host")).thenReturn(jmsTemplate);
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
        call.setDestination("host");

        JMSOutgoingPort jmsOutgoingPort = new JMSOutgoingPort();
        jmsOutgoingPort.setFactory(jmsTemplateFactory);

        FilterChainFactory<MethodCallRequest, MethodResultMessage> factory =
            new FilterChainFactory<MethodCallRequest, MethodResultMessage>(MethodCallRequest.class,
                MethodResultMessage.class);
        factory.setFilters(Arrays.asList(JsonOutgoingMethodCallMarshalFilter.class, jmsOutgoingPort));

        OutgoingPortImpl outgoingPort = new OutgoingPortImpl();
        outgoingPort.setFilterChain(factory.create());

        this.outgoingPort = outgoingPort;
    }

    @Test
    public void callSend_shouldSendMessageViaJMS() throws URISyntaxException, IOException {
        outgoingPort.send(call);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(jmsTemplate).convertAndSend(org.mockito.Matchers.eq("receive"), captor.capture());
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
        outgoingPort.sendSync(call);
        Mockito.verify(jmsTemplate).convertAndSend(org.mockito.Matchers.eq("receive"), sendIdCaptor.capture());

        String destination =
            new ObjectMapper().readValue(new StringReader(sendIdCaptor.getValue()), JsonNode.class).get("callId")
                .getValueAsText();
        assertThat(destinationCaptor.getValue(), Matchers.equalTo(destination));
    }

    @Override
    protected void setBundleContext(BundleContext bundleContext) {
        DefaultOsgiUtilsService serviceUtils = new DefaultOsgiUtilsService();
        serviceUtils.setBundleContext(bundleContext);
        registerService(serviceUtils, new Hashtable<String, Object>(), OsgiUtilsService.class);
        OpenEngSBCoreServices.setOsgiServiceUtils(serviceUtils);
    }
}
