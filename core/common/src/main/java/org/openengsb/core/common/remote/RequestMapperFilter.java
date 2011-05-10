package org.openengsb.core.common.remote;

import java.util.Map;

import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.RequestHandler;

public class RequestMapperFilter extends AbstractFilterAction<MethodCallRequest, MethodResultMessage> {

    private RequestHandler requestHandler;

    public RequestMapperFilter() {
        super(MethodCallRequest.class, MethodResultMessage.class);
    }

    public RequestMapperFilter(RequestHandler requestHandler) {
        this();
        this.requestHandler = requestHandler;
    }

    @Override
    protected MethodResultMessage doFilter(MethodCallRequest input) {
        Map<String, Object> storage = FilterStorage.getStorage();
        storage.put("callId", input.getCallId());
        storage.put("answer", input.isAnswer());
        MethodResult result = requestHandler.handleCall(input.getMethodCall());
        return new MethodResultMessage(result, input.getCallId());
    }
}
