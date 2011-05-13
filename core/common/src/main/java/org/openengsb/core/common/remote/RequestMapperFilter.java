package org.openengsb.core.common.remote;

import java.util.Map;

import org.openengsb.core.api.remote.MethodCallRequest;
import org.openengsb.core.api.remote.MethodResult;
import org.openengsb.core.api.remote.MethodResultMessage;
import org.openengsb.core.api.remote.RequestHandler;

/**
 * This filter takes a {@link MethodCallRequest} and handles it using a {@link RequestHandler}. The result is then
 * wrapped to a {@link MethodResultMessage} and returned.
 *
 * <code>
 * <pre>
 *      [MethodCallRequest]   > Filter > [MethodCall]
 *                                             |
 *                                             v
 *                                       RequestHandler
 *                                             |
 *                                             v
 *      [MethodResultMessage] < Filter < [MethodResult]
 * </pre>
 * </code>
 */
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
    protected MethodResultMessage doFilter(MethodCallRequest input, Map<String, Object> metadata) {
        metadata.put("callId", input.getCallId());
        metadata.put("answer", input.isAnswer());
        MethodResult result = requestHandler.handleCall(input.getMethodCall());
        return new MethodResultMessage(result, input.getCallId());
    }

    public void setRequestHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }
}
