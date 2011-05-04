package org.openengsb.core.common.security.filter;

import java.util.Map;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodResult;
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
        MethodResult result = (MethodResult) next.filter(input.getMessage(), metaData);
        return SecureResponse.create(result);
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, MethodCall.class, MethodResult.class);
        this.next = next;
    }
}
