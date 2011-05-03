package org.openengsb.core.common.filter;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.remote.AbstractFilterChainElement;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodReturn;

import com.google.common.base.Preconditions;

public class JsonMethodCallMarshalFilter extends AbstractFilterChainElement<String, String> {

    private FilterAction<MethodCall, MethodReturn> next;

    JsonMethodCallMarshalFilter() {
        super(String.class, String.class);
    }

    @Override
    public String apply(String input) throws FilterException {
        ObjectMapper objectMapper = new ObjectMapper();
        MethodCall call;
        try {
            call = objectMapper.readValue(input, MethodCall.class);
            MethodReturn returnValue = next.apply(call);
            return objectMapper.writeValueAsString(returnValue);
        } catch (IOException e) {
            throw new FilterException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setNext(FilterAction<?, ?> next) {
        Preconditions.checkArgument(next.getSupportedInputType().isAssignableFrom(MethodCall.class));
        Preconditions.checkArgument(next.getSupportedOutputType().isAssignableFrom(MethodReturn.class));
        this.next = (FilterAction<MethodCall, MethodReturn>) next;
    }

}
