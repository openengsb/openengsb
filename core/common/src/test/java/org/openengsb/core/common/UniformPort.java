package org.openengsb.core.common;

import java.util.Arrays;
import java.util.List;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfig;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodReturn;

public class UniformPort<ContentType> extends FilterConfig<ContentType, ContentType> {
    public static class Builder<ContentType> {

        protected FilterAction<ContentType, MethodCall> requestUnmarshaller;
        protected FilterAction<MethodCall, MethodReturn> requestHandler;
        protected FilterAction<MethodReturn, ContentType> responseMarshaller;

        protected Builder() {
        }

        public Builder<ContentType> requestUnmarshaller(
                FilterAction<ContentType, MethodCall> requestUnmarshaller) {
            this.requestUnmarshaller = requestUnmarshaller;
            return this;
        }

        public Builder<ContentType> requestHandler(
                FilterAction<MethodCall, MethodReturn> requestHandler) {
            this.requestHandler = requestHandler;
            return this;
        }

        public Builder<ContentType> responseMarshaller(
                FilterAction<MethodReturn, ContentType> responseMarshaller) {
            this.responseMarshaller = responseMarshaller;
            return this;
        }

        public UniformPort<ContentType> build() {
            @SuppressWarnings("unchecked")
            List<FilterAction<?, ? extends Object>> asList =
                Arrays.asList(requestUnmarshaller, requestHandler, responseMarshaller);
            return new UniformPort<ContentType>(asList);
        }
    }

    public UniformPort(List<FilterAction<?, ?>> filterChain) {
        super(filterChain);
    }

    public static <ContentType> Builder<ContentType> builder() {
        return new Builder<ContentType>();
    }

}
