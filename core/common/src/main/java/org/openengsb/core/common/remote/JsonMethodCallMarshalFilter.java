package org.openengsb.core.common.remote;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodReturn;

public class JsonMethodCallMarshalFilter extends AbstractFilterChainElement<String, String> {

    private FilterAction next;

    public JsonMethodCallMarshalFilter() {
        super(String.class, String.class);
    }

    @Override
    public String doFilter(String input) throws FilterException {
        ObjectMapper objectMapper = new ObjectMapper();
        MethodCall call;
        try {
            call = objectMapper.readValue(input, MethodCall.class);
            MethodReturn returnValue = (MethodReturn) next.filter(call);
            return objectMapper.writeValueAsString(returnValue);
        } catch (IOException e) {
            throw new FilterException(e);
        }
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, MethodCall.class, MethodReturn.class);
        this.next = next;
    }

}
