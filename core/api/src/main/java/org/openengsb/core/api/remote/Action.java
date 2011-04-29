package org.openengsb.core.api.remote;

public interface Action<ContentType> {

    void apply(ContentType input) throws FilterException;

}
