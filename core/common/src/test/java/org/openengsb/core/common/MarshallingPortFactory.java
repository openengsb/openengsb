package org.openengsb.core.common;

import java.util.Arrays;
import java.util.List;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfig;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodReturn;

public class MarshallingPortFactory<ContentType> {

    protected FilterAction<ContentType, MethodCall> requestUnmarshaller;
    protected FilterAction<MethodCall, MethodReturn> executionHandler;
    protected FilterAction<MethodReturn, ContentType> responseMarshaller;

    public MarshallingPortFactory() {
    }

    public FilterAction<ContentType, MethodCall> getRequestUnmarshaller() {
        return requestUnmarshaller;
    }

    public void setRequestUnmarshaller(FilterAction<ContentType, MethodCall> requestUnmarshaller) {
        this.requestUnmarshaller = requestUnmarshaller;
    }

    public FilterAction<MethodCall, MethodReturn> getExecutionHandler() {
        return executionHandler;
    }

    public void setExecutionHandler(FilterAction<MethodCall, MethodReturn> executionHandler) {
        this.executionHandler = executionHandler;
    }

    public FilterAction<MethodReturn, ContentType> getResponseMarshaller() {
        return responseMarshaller;
    }

    public void setResponseMarshaller(FilterAction<MethodReturn, ContentType> responseMarshaller) {
        this.responseMarshaller = responseMarshaller;
    }

    @SuppressWarnings("unchecked")
    public FilterConfig<ContentType, ContentType> createNewInstance() {
        List<FilterAction<? extends Object, ? extends Object>> filters =
            Arrays.asList(requestUnmarshaller, executionHandler, responseMarshaller);
        return new FilterConfig<ContentType, ContentType>(filters);
    }

}
