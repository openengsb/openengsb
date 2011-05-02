package org.openengsb.core.api.remote;


/**
 * maybe we want to convert from String or byte[] to Document
 */
public class GenericPort<ContentEncoding, ContentType> {
    protected FilterAction<ContentEncoding, ContentType> decoder;

    // unmarshal container (separate encrypted sessionkey from encrypted content)
    // decrypt session-key
    // decrypt message
    protected FilterAction<ContentType, MethodCall> requestUnmarshaller;
    // verify
    // private Action<MethodCall> authenticationAction;
    protected FilterAction<MethodCall, MethodReturn> requestHandler;
    // private Function<MethodReturn, MethodReturn> add verification information
    // private Function<MethodReturn, MethodReturn> sign response
    protected FilterAction<MethodReturn, ContentType> responseMarshaller;

    // encrypt using old session-key
    protected FilterAction<ContentType, ContentEncoding> encoder;

    public ContentEncoding handle(ContentEncoding input) {
        ContentType content = decoder.apply(input);
        ContentType resultContent = doHandle(content);
        return encoder.apply(resultContent);
    }

    private ContentType doHandle(ContentType input) {
        MethodCall call = requestUnmarshaller.apply(input);
        MethodReturn result = requestHandler.apply(call);
        return responseMarshaller.apply(result);
    }

    public FilterAction<ContentEncoding, ContentType> getDecoder() {
        return decoder;
    }

    public void setDecoder(FilterAction<ContentEncoding, ContentType> decoder) {
        this.decoder = decoder;
    }

    public FilterAction<ContentType, MethodCall> getRequestUnmarshaller() {
        return requestUnmarshaller;
    }

    public void setRequestUnmarshaller(FilterAction<ContentType, MethodCall> requestUnmarshaller) {
        this.requestUnmarshaller = requestUnmarshaller;
    }

    public FilterAction<MethodCall, MethodReturn> getRequestHandler() {
        return requestHandler;
    }

    public void setRequestHandler(FilterAction<MethodCall, MethodReturn> requestHandler) {
        this.requestHandler = requestHandler;
    }

    public FilterAction<MethodReturn, ContentType> getResponseMarshaller() {
        return responseMarshaller;
    }

    public void setResponseMarshaller(FilterAction<MethodReturn, ContentType> responseMarshaller) {
        this.responseMarshaller = responseMarshaller;
    }

    public FilterAction<ContentType, ContentEncoding> getEncoder() {
        return encoder;
    }

    public void setEncoder(FilterAction<ContentType, ContentEncoding> encoder) {
        this.encoder = encoder;
    }

}
