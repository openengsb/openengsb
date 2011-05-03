package org.openengsb.core.api.remote;

public interface FilterAction<InputType, OutputType> {

    OutputType filter(InputType input) throws FilterException;

    /**
     * This is required to check the compatibility of filters at runtime (since the type-parameters are not available at
     * runtime)
     */
    Class<InputType> getSupportedInputType();

    /**
     * This is required to check the compatibility of filters at runtime (since the type-parameters are not available at
     * runtime)
     */
    Class<OutputType> getSupportedOutputType();

}
