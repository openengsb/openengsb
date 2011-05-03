package org.openengsb.core.api.remote;

public interface FilterAction<InputType, OutputType> {

    OutputType apply(InputType input) throws FilterException;

    Class<InputType> getSupportedInputType();

    Class<OutputType> getSupportedOutputType();

}
