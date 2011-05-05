package org.openengsb.core.api.remote;

/**
 * Represents an Action in a filter-chain. Implementing this interface ensures that the class can be used in a
 * filterchain configuration.
 *
 */
public interface FilterAction<InputType, OutputType> {

    /**
     * Transforms the input-value received from the previous filter to an output value that is then returned.
     *
     * @throws FilterException if some Exception occurs. All Exception should be wrapped in a {@link FilterException} so
     *         that they can be treated at the end of the chain.
     */
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
