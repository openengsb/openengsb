package org.openengsb.core.common;

import java.util.Arrays;
import java.util.List;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfig;

/**
 * maybe we want to convert from String or byte[] to Document
 */
public class EncodingPortFactory<ContentEncoding, ContentType> {

    protected FilterAction<ContentEncoding, ContentType> decoder;
    protected FilterAction<ContentType, ContentType> marshallingPort;
    protected FilterAction<ContentType, ContentEncoding> encoder;

    public FilterAction<ContentEncoding, ContentType> getDecoder() {
        return decoder;
    }

    public void setDecoder(FilterAction<ContentEncoding, ContentType> decoder) {
        this.decoder = decoder;
    }

    public FilterAction<ContentType, ContentType> getMarshallingPort() {
        return marshallingPort;
    }

    public void setMarshallingPort(FilterAction<ContentType, ContentType> marshallingPort) {
        this.marshallingPort = marshallingPort;
    }

    public FilterAction<ContentType, ContentEncoding> getEncoder() {
        return encoder;
    }

    public void setEncoder(FilterAction<ContentType, ContentEncoding> encoder) {
        this.encoder = encoder;
    }

    public EncodingPortFactory() {
        // TODO Auto-generated constructor stub
    }

    public FilterConfig<ContentEncoding, ContentEncoding> createNewInstance() {
        @SuppressWarnings("unchecked")
        List<FilterAction<?, ? extends Object>> asList =
                Arrays.asList(decoder, marshallingPort, encoder);
        return new FilterConfig<ContentEncoding, ContentEncoding>(asList);
    }

}
