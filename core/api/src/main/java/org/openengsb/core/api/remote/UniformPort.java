package org.openengsb.core.api.remote;


public class UniformPort<ContentType> extends GenericPort<ContentType, ContentType> {

    public UniformPort() {
        this.encoder = new TransformingAction<ContentType, ContentType>() {
            @Override
            public ContentType apply(ContentType input) throws FilterException {
                return input;
            };
        };
        this.decoder = new TransformingAction<ContentType, ContentType>() {
            @Override
            public ContentType apply(ContentType input) throws FilterException {
                return input;
            };
        };
    }

}
