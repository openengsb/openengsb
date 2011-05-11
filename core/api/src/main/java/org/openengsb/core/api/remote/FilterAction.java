package org.openengsb.core.api.remote;

import java.util.Map;

/**
 * Represents an Action in a filter-chain. Implementing this interface ensures that the class can be used in a
 * filterchain configuration.
 *
 */
public interface FilterAction {

    /**
     * Transforms the input-value received from the previous filter to an output value that is then returned.
     *
     * @throws FilterException if some Exception occurs. All Exception should be wrapped in a {@link FilterException} so
     *         that they can be treated at the end of the chain.
     */
    Object filter(Object input, Map<String, Object> metaData) throws FilterException;

    /**
     * This is required to check the compatibility of filters at runtime (since the type-parameters are not available at
     * runtime)
     */
    Class<?> getSupportedInputType();

    /**
     * This is required to check the compatibility of filters at runtime (since the type-parameters are not available at
     * runtime)
     */
    Class<?> getSupportedOutputType();

}
