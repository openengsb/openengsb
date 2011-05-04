package org.openengsb.core.api.remote;

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
