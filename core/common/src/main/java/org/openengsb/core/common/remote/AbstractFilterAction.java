package org.openengsb.core.common.remote;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterException;

import com.google.common.base.Preconditions;

/**
 * Abstract Baseclass that aids when implementing new {@link FilterAction}s. If the implemented filter is designed to
 * use other filters to calculate the result, the {@link AbstractFilterChainElement}-class should be used.
 */
public abstract class AbstractFilterAction<InputType, OutputType> implements FilterAction {

    private Class<InputType> inputType;
    private Class<OutputType> outputType;

    protected AbstractFilterAction(Class<InputType> inputType, Class<OutputType> outputType) {
        this.inputType = inputType;
        this.outputType = outputType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object filter(Object input) throws FilterException {
        Preconditions.checkArgument(inputType.isAssignableFrom(input.getClass()));
        return doFilter((InputType) input);
    }

    protected abstract OutputType doFilter(InputType input);

    @Override
    public Class<InputType> getSupportedInputType() {
        return inputType;
    }

    @Override
    public Class<OutputType> getSupportedOutputType() {
        return outputType;
    }

}
