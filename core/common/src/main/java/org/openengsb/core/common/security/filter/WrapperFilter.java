package org.openengsb.core.common.security.filter;

import java.util.Map;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.remote.AbstractFilterChainElement;

public class WrapperFilter extends AbstractFilterChainElement<SecureRequest, SecureResponse> {

    private FilterAction next;

    public WrapperFilter() {
        super(SecureRequest.class, SecureResponse.class);
    }

    @Override
    protected SecureResponse doFilter(SecureRequest input, Map<String, Object> metaData) {
        MethodResultMessage result = (MethodResultMessage) next.filter(input.getMessage(), metaData);
        return SecureResponse.create(result);
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, MethodCall.class, MethodResultMessage.class);
        this.next = next;
    }
}
