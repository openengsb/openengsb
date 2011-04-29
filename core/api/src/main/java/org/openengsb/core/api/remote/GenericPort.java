package org.openengsb.core.api.remote;


/**
 * maybe we want to convert from String or byte[] to Document
 */
public class GenericPort<ContentEncoding, ContentType> {
    protected TransformingAction<ContentEncoding, ContentType> decoder;

    // unmarshal container (separate encrypted sessionkey from encrypted content)
    // decrypt session-key
    // decrypt message
    protected TransformingAction<ContentType, MethodCall> requestUnmarshaller;
    // verify
    // private Action<MethodCall> authenticationAction;
    protected TransformingAction<MethodCall, MethodReturn> requestHandler;
    // private Function<MethodReturn, MethodReturn> add verification information
    // private Function<MethodReturn, MethodReturn> sign response
    protected TransformingAction<MethodReturn, ContentType> responseMarshaller;

    // encrypt using old session-key
    protected TransformingAction<ContentType, ContentEncoding> encoder;

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

    public TransformingAction<ContentEncoding, ContentType> getDecoder() {
        return decoder;
    }

    public void setDecoder(TransformingAction<ContentEncoding, ContentType> decoder) {
        this.decoder = decoder;
    }

    public TransformingAction<ContentType, MethodCall> getRequestUnmarshaller() {
        return requestUnmarshaller;
    }

    public void setRequestUnmarshaller(TransformingAction<ContentType, MethodCall> requestUnmarshaller) {
        this.requestUnmarshaller = requestUnmarshaller;
    }

    public TransformingAction<MethodCall, MethodReturn> getRequestHandler() {
        return requestHandler;
    }

    public void setRequestHandler(TransformingAction<MethodCall, MethodReturn> requestHandler) {
        this.requestHandler = requestHandler;
    }

    public TransformingAction<MethodReturn, ContentType> getResponseMarshaller() {
        return responseMarshaller;
    }

    public void setResponseMarshaller(TransformingAction<MethodReturn, ContentType> responseMarshaller) {
        this.responseMarshaller = responseMarshaller;
    }

    public TransformingAction<ContentType, ContentEncoding> getEncoder() {
        return encoder;
    }

    public void setEncoder(TransformingAction<ContentType, ContentEncoding> encoder) {
        this.encoder = encoder;
    }

}
