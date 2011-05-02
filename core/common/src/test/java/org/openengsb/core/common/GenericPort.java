package org.openengsb.core.common;

import java.util.Arrays;
import java.util.List;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfig;
import org.openengsb.core.api.remote.MethodCall;
import org.openengsb.core.api.remote.MethodReturn;

/**
 * maybe we want to convert from String or byte[] to Document
 */
public class GenericPort<ContentEncoding, ContentType> extends FilterConfig<ContentEncoding, ContentEncoding> {

    public static class Builder<ContentEncoding, ContentType> {

        protected FilterAction<ContentEncoding, ContentType> decoder;
        protected FilterAction<ContentType, MethodCall> requestUnmarshaller;
        protected FilterAction<MethodCall, MethodReturn> requestHandler;
        protected FilterAction<MethodReturn, ContentType> responseMarshaller;
        protected FilterAction<ContentType, ContentEncoding> encoder;

        public Builder<ContentEncoding, ContentType> decoder(FilterAction<ContentEncoding, ContentType> decoder) {
            this.decoder = decoder;
            return this;
        }

        public Builder<ContentEncoding, ContentType> requestUnmarshaller(
                FilterAction<ContentType, MethodCall> requestUnmarshaller) {
            this.requestUnmarshaller = requestUnmarshaller;
            return this;
        }

        public Builder<ContentEncoding, ContentType> requestHandler(
                FilterAction<MethodCall, MethodReturn> requestHandler) {
            this.requestHandler = requestHandler;
            return this;
        }

        public Builder<ContentEncoding, ContentType> responseMarshaller(
                FilterAction<MethodReturn, ContentType> responseMarshaller) {
            this.responseMarshaller = responseMarshaller;
            return this;
        }

        public Builder<ContentEncoding, ContentType> encoder(FilterAction<ContentType, ContentEncoding> encoder) {
            this.encoder = encoder;
            return this;
        }

        public GenericPort<ContentEncoding, ContentType> build() {
            @SuppressWarnings("unchecked")
            List<FilterAction<?, ? extends Object>> asList =
                Arrays.asList(decoder, requestUnmarshaller, requestHandler, responseMarshaller, encoder);
            return new GenericPort<ContentEncoding, ContentType>(asList);
        }
    }

    public GenericPort(List<FilterAction<?, ?>> filterChain) {
        super(filterChain);
    }

    public static <ContentEncoding, ContentType> Builder<ContentEncoding, ContentType> builder() {
        return new Builder<ContentEncoding, ContentType>();
    }

}
