package org.openengsb.core.common.remote;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;

public class JsonOutgoingMethodCallMarshalFilter extends
        AbstractFilterChainElement<MethodCallRequest, MethodResultMessage> {

    private FilterAction next;

    public JsonOutgoingMethodCallMarshalFilter() {
        super(MethodCallRequest.class, MethodResultMessage.class);
    }

    @Override
    public MethodResultMessage doFilter(MethodCallRequest input, Map<String, Object> metadata) throws FilterException {
        ObjectMapper objectMapper = createObjectMapper();
        MethodResultMessage resultMessage;
        try {
            String jsonString = objectMapper.writeValueAsString(input);
            String resultString = (String) next.filter(jsonString, metadata);
            if (resultString == null) {
                return null;
            }
            resultMessage = objectMapper.readValue(resultString, MethodResultMessage.class);
        } catch (IOException e) {
            throw new FilterException(e);
        }
        MethodResult result = resultMessage.getResult();
        Class<?> className;
        try {
            className = Class.forName(result.getClassName());
        } catch (ClassNotFoundException e) {
            throw new FilterException(e);
        }
        Object convertedValue = objectMapper.convertValue(result.getArg(), className);
        result.setArg(convertedValue);
        return resultMessage;
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, String.class, String.class);
        this.next = next;
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector primaryIntrospector = new JacksonAnnotationIntrospector();
        AnnotationIntrospector secondaryIntrospector = new JaxbAnnotationIntrospector();
        AnnotationIntrospector introspector =
            new AnnotationIntrospector.Pair(primaryIntrospector, secondaryIntrospector);
        mapper.getDeserializationConfig().withAnnotationIntrospector(introspector);
        mapper.getSerializationConfig().withAnnotationIntrospector(introspector);
        return mapper;
    }

}
