package org.openengsb.core.api.remote;

public abstract class AbstractFilterChainElement<InputType, OutputType> extends
        AbstractFilterAction<InputType, OutputType>
        implements FilterChainElement<InputType, OutputType> {

    protected AbstractFilterChainElement(Class<InputType> inputType, Class<OutputType> outputType) {
        super(inputType, outputType);
    }

}
