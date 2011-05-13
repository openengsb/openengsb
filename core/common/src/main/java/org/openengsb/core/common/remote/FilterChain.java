package org.openengsb.core.common.remote;

import java.util.Map;

import org.openengsb.core.api.remote.FilterAction;

public class FilterChain<InputType, OutputType> extends AbstractFilterAction<InputType, OutputType> {
    private FilterAction firstElement;

    FilterChain(Class<InputType> inputType, Class<OutputType> outputType, FilterAction firstElement) {
        super(inputType, outputType);
        this.firstElement = firstElement;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected OutputType doFilter(InputType input, Map<String, Object> metadata) {
        return (OutputType) firstElement.filter(input, metadata);
    };
}
