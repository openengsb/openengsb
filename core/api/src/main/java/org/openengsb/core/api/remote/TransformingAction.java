package org.openengsb.core.api.remote;

public interface TransformingAction<InputType, ResultType> {

    ResultType apply(InputType input) throws FilterException;

}
