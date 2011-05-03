package org.openengsb.core.common.filter;

import org.openengsb.core.api.remote.AbstractFilterChainElement;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterException;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodReturn;
import org.openengsb.core.api.remote.RequestHandler;

public class RequestHandlerFilter extends AbstractFilterChainElement<MethodCall, MethodReturn> {

    private RequestHandler handler;

    public RequestHandlerFilter(RequestHandler handler) {
        this();
        this.handler = handler;
    }

    public RequestHandlerFilter() {
        super(MethodCall.class, MethodReturn.class);
    }

    @Override
    public MethodReturn apply(MethodCall input) throws FilterException {
        return handler.handleCall(input);
    }

    public void setHandler(RequestHandler handler) {
        this.handler = handler;
    }

    @Override
    public void setNext(FilterAction<?, ?> next) {
        throw new UnsupportedOperationException("this is the end of the line");
    }

}
