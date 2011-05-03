package org.openengsb.core.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodReturn;
import org.openengsb.core.api.remote.MethodReturn.ReturnType;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.common.filter.JsonMethodCallMarshalFilter;
import org.openengsb.core.common.filter.RequestHandlerFilter;
import org.openengsb.core.common.filter.XmlEncoderFilter;
import org.openengsb.core.common.filter.XmlMethodCallMarshalFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class PipelineTest {

    private RequestHandler requestHandlerMock;

    @Before
    public void setUp() {
        requestHandlerMock = mock(RequestHandler.class);
        when(requestHandlerMock.handleCall(any(MethodCall.class))).thenAnswer(new Answer<MethodReturn>() {
            @Override
            public MethodReturn answer(InvocationOnMock invocation) throws Throwable {
                MethodCall input = (MethodCall) invocation.getArguments()[0];
                return new MethodReturn(ReturnType.Object, input.getArgs()[0], new HashMap<String, String>(), input
                        .getCallId());
            }
        });
    }

    @Test
    public void testArchWithJson() throws Exception {
        FilterAction<String, String> filterChain =
            FilterChainFactory.build(String.class, String.class, new JsonMethodCallMarshalFilter(),
                new RequestHandlerFilter(requestHandlerMock));

        ObjectMapper objectMapper = new ObjectMapper();
        MethodCall methodCall = new MethodCall();
        methodCall.setArgs(new Object[] { "foo" });
        methodCall.setCallId("bar");
        String input = objectMapper.writeValueAsString(methodCall);
        String result = filterChain.apply(input);
        MethodReturn returnValue = objectMapper.readValue(result, MethodReturn.class);
        assertThat((String) returnValue.getArg(), is("foo"));
        assertThat(returnValue.getCallId(), is("bar"));
    }

    @Test
    public void testArchWithXml() throws Exception {
        FilterAction<String, String> filterChain =
            FilterChainFactory.build(String.class, String.class, new XmlEncoderFilter(),
                new XmlMethodCallMarshalFilter(), new RequestHandlerFilter(requestHandlerMock));

        MethodCall call = new MethodCall();
        call.setArgs(new Object[] { "foo" });
        call.setClasses(Arrays.asList(String.class.getName()));
        call.setCallId("bar");

        JAXBContext jaxbContext = JAXBContext.newInstance(MethodCall.class, MethodReturn.class);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        final Marshaller marshaller = jaxbContext.createMarshaller();

        DOMResult domResult = new DOMResult();
        marshaller.marshal(new JAXBElement<MethodCall>(new QName(MethodCall.class.getSimpleName()), MethodCall.class,
            call), domResult);
        String input = XmlEncoderFilter.writeDocument(domResult.getNode());
        String result = filterChain.apply(input);

        Document parseDocument = XmlEncoderFilter.parseDocument(result);
        MethodReturn value = unmarshaller.unmarshal(parseDocument, MethodReturn.class).getValue();
        String value2 = unmarshaller.unmarshal((Node) value.getArg(), String.class).getValue();
        value.setArg(value2);
        assertThat((String) value.getArg(), is("foo"));
        assertThat(value.getCallId(), is("bar"));
    }
}
