package org.openengsb.core.common.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.JacksonAnnotationIntrospector;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResultMessage;

public class JsonMethodCallMarshalFilter extends AbstractFilterChainElement<String, String> {

    private FilterAction next;

    public JsonMethodCallMarshalFilter() {
        super(String.class, String.class);
    }

    @Override
    public String doFilter(String input, Map<String, Object> metadata) throws FilterException {
        ObjectMapper objectMapper = createObjectMapper();
        MethodCallRequest call;
        try {
            call = objectMapper.readValue(input, MethodCallRequest.class);
            resetArgs(call);
            MethodResultMessage returnValue = (MethodResultMessage) next.filter(call, metadata);
            return objectMapper.writeValueAsString(returnValue);
        } catch (IOException e) {
            throw new FilterException(e);
        }
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, MethodCallRequest.class, MethodResultMessage.class);
        this.next = next;
    }

    /**
     * Converts the Args read by Jackson into the correct classes that have to be used for calling the method.
     */
    private static void resetArgs(MethodCallRequest request) {
        MethodCall call = request.getMethodCall();
        if (call.getClasses().size() != call.getArgs().length) {
            throw new IllegalStateException("Classes and Args have to be the same");
        }
        ObjectMapper mapper = createObjectMapper();
        Iterator<String> iterator = call.getClasses().iterator();

        List<Object> values = new ArrayList<Object>();

        for (Object arg : call.getArgs()) {
            Class<?> class1;
            try {
                class1 = Class.forName(iterator.next());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            values.add(mapper.convertValue(arg, class1));
        }
        call.setArgs(values.toArray());
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
