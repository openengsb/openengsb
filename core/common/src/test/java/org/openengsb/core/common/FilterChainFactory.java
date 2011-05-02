package org.openengsb.core.common;

import org.openengsb.core.api.remote.AbstractFilterAction;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterChainElement;
import org.openengsb.core.api.remote.FilterException;

import com.google.common.base.Preconditions;

public final class FilterChainFactory {

    public static <InputType, OutputType> FilterAction<InputType, OutputType> build(Class<InputType> inputType,
            Class<OutputType> outputType, FilterChainElement<?, ?>... filters) {
        Preconditions.checkNotNull(filters);
        Preconditions.checkArgument(filters.length > 0);
        @SuppressWarnings("unchecked")
        final FilterChainElement<InputType, OutputType> first = (FilterChainElement<InputType, OutputType>) filters[0];
        Preconditions.checkArgument(first.getSupportedInputType().isAssignableFrom(inputType));
        Preconditions.checkArgument(first.getSupportedOutputType().isAssignableFrom(outputType));
        for (int i = 1; i < filters.length; i++) {
            filters[i - 1].setNext(filters[i]);
        }
        return new AbstractFilterAction<InputType, OutputType>(inputType, outputType) {
            @Override
            public OutputType apply(InputType input) throws FilterException {
                return first.apply(input);
            };
        };
    }

    private FilterChainFactory() {
    }
}
