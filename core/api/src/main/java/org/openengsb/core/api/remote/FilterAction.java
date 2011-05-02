package org.openengsb.core.api.remote;

public interface FilterAction<InputType, ResultType> {

    ResultType apply(InputType input) throws FilterException;

}
