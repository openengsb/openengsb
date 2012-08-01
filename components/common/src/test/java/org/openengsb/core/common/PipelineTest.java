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

package org.openengsb.core.common;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.RequestHandler;
import org.openengsb.core.common.remote.FilterChainFactory;
import org.openengsb.core.common.remote.JsonMethodCallMarshalFilter;
import org.openengsb.core.common.remote.RequestMapperFilter;
import org.openengsb.core.common.remote.XmlDecoderFilter;
import org.openengsb.core.common.remote.XmlMethodCallMarshalFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class PipelineTest {

    private RequestMapperFilter requestMapperFilter;

    @Before
    public void setUp() {
        RequestHandler requestHandlerMock = mock(RequestHandler.class);
        when(requestHandlerMock.handleCall(any(MethodCall.class))).thenAnswer(new Answer<MethodResult>() {
            @Override
            public MethodResult answer(InvocationOnMock invocation) throws Throwable {
                MethodCall input = (MethodCall) invocation.getArguments()[0];
                return new MethodResult(input.getArgs()[0]);
            }
        });
        requestMapperFilter = new RequestMapperFilter(requestHandlerMock);
    }

    @Test
    public void testArchWithJson_shouldWork() throws Exception {
        FilterChainFactory<String, String> filterChainFactory =
            new FilterChainFactory<String, String>(String.class, String.class);

        List<Object> filters = Arrays.asList(new Object[]{ JsonMethodCallMarshalFilter.class, requestMapperFilter });
        filterChainFactory.setFilters(filters);

        FilterAction filterChain = filterChainFactory.create();

        ObjectMapper objectMapper = new ObjectMapper();
        MethodCall methodCall = new MethodCall("test", new Object[]{ "foo" });
        MethodCallRequest request = new MethodCallRequest(methodCall, "bar");
        String input = objectMapper.writeValueAsString(request);
        String result = (String) filterChain.filter(input, new HashMap<String, Object>());
        MethodResultMessage returnValue = objectMapper.readValue(result, MethodResultMessage.class);
        assertThat((String) returnValue.getResult().getArg(), is("foo"));
        assertThat(returnValue.getCallId(), is("bar"));
    }

    @Test
    public void testArchWithXml_shouldWork() throws Exception {
        FilterChainFactory<String, String> filterChainFactory =
            new FilterChainFactory<String, String>(String.class, String.class);
        List<Object> filters =
            Arrays
                .asList(new Object[]{ XmlDecoderFilter.class, XmlMethodCallMarshalFilter.class, requestMapperFilter, });
        filterChainFactory.setFilters(filters);

        FilterAction filterChain = filterChainFactory.create();

        MethodCall methodCall = new MethodCall("test", new Object[]{ "foo" }, Arrays.asList(String.class.getName()));
        MethodCallRequest request = new MethodCallRequest(methodCall, "bar");

        JAXBContext jaxbContext = JAXBContext.newInstance(MethodCallRequest.class, MethodResultMessage.class);
        final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        final Marshaller marshaller = jaxbContext.createMarshaller();

        DOMResult domResult = new DOMResult();
        marshaller.marshal(new JAXBElement<MethodCallRequest>(new QName(MethodCallRequest.class.getSimpleName()),
            MethodCallRequest.class, request), domResult);
        String input = XmlDecoderFilter.writeDocument(domResult.getNode());
        String result = (String) filterChain.filter(input, new HashMap<String, Object>());

        Document parseDocument = XmlDecoderFilter.parseDocument(result);
        MethodResultMessage value = unmarshaller.unmarshal(parseDocument, MethodResultMessage.class).getValue();
        String value2 = unmarshaller.unmarshal((Node) value.getResult().getArg(), String.class).getValue();
        value.getResult().setArg(value2);
        assertThat((String) value.getResult().getArg(), is("foo"));
        assertThat(value.getCallId(), is("bar"));
    }

    @Test
    public void testFilterStorage_shouldWork() throws Exception {
        FilterChainFactory<String, String> filterChainFactory =
            new FilterChainFactory<String, String>(String.class, String.class);

        List<Object> filters = Arrays.asList(new Object[]{ JsonMethodCallMarshalFilter.class, requestMapperFilter, });
        filterChainFactory.setFilters(filters);

        FilterAction filterChain = filterChainFactory.create();

        ObjectMapper objectMapper = new ObjectMapper();
        MethodCall methodCall = new MethodCall("test", new Object[]{ "foo" });
        MethodCallRequest request = new MethodCallRequest(methodCall, "bar");
        String input = objectMapper.writeValueAsString(request);
        HashMap<String, Object> metaData = new HashMap<String, Object>();
        filterChain.filter(input, metaData);
        assertThat((String) metaData.get("callId"), is("bar"));
    }

    @Test(expected = FilterConfigurationException.class)
    public void testCreateFilterWithIncompatibleFirst_shouldThrowFilterConfigurationException() {
        FilterChainFactory<String, String> filterChainFactory =
            new FilterChainFactory<String, String>(String.class, String.class);

        List<Object> filters =
            Arrays
                .asList(new Object[]{ XmlMethodCallMarshalFilter.class, XmlDecoderFilter.class });
        filterChainFactory.setFilters(filters);
        filterChainFactory.create();
    }

    @Test(expected = FilterConfigurationException.class)
    public void testCreateFilterWithIncompatibleElements_shouldThrowFilterConfigurationException() {
        FilterChainFactory<String, String> filterChainFactory =
            new FilterChainFactory<String, String>(String.class, String.class);
        List<Object> filters =
            Arrays.asList(new Object[]{ XmlDecoderFilter.class, XmlMethodCallMarshalFilter.class,
                XmlDecoderFilter.class });
        filterChainFactory.setFilters(filters);
        filterChainFactory.create();
    }
}
