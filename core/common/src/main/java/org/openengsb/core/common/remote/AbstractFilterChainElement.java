package org.openengsb.core.common.remote;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterChainElement;
import org.openengsb.core.api.remote.FilterConfigurationException;

/**
 * Abstract Baseclass that aids when implementing new {@link FilterChainElement}s. This class is supposed to be used for
 * implementing {@link FilterAction}s which use other filters (next) for calculating their result.
 *
 */
public abstract class AbstractFilterChainElement<InputType, OutputType> extends
        AbstractFilterAction<InputType, OutputType>
        implements FilterChainElement<InputType, OutputType> {

    protected AbstractFilterChainElement(Class<InputType> inputType, Class<OutputType> outputType) {
        super(inputType, outputType);
    }

    protected static final void checkNextInputAndOutputTypes(FilterAction<?, ?> next,
            Class<?> inputType, Class<?> outputType) throws FilterConfigurationException {
        if (!next.getSupportedInputType().isAssignableFrom(inputType)) {
            throw new FilterConfigurationException(String.format("inputTypes are not compatible (%s - %s)",
                next.getSupportedInputType(), inputType));
        }
        if (!next.getSupportedOutputType().isAssignableFrom(outputType)) {
            throw new FilterConfigurationException(String.format("outputTypes are not compatible (%s - %s)",
                next.getSupportedOutputType(), outputType));
        }
    }
}
