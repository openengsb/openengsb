package org.openengsb.core.common;

import org.openengsb.core.api.remote.AbstractFilterAction;
import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterChainElement;
import org.openengsb.core.api.remote.FilterException;

import com.google.common.base.Preconditions;

public final class FilterChainFactory {

    private static final class FilterChain<InputType, OutputType> extends AbstractFilterAction<InputType, OutputType> {
        private FilterChainElement<InputType, OutputType> first;

        private FilterChain(Class<InputType> inputType, Class<OutputType> outputType,
                FilterChainElement<InputType, OutputType> first) {
            super(inputType, outputType);
            this.first = first;
        }

        @Override
        public OutputType apply(InputType input) throws FilterException {
            return first.apply(input);
        }
    }

    public static <InputType, OutputType> FilterAction<InputType, OutputType> build(Class<InputType> inputType,
            Class<OutputType> outputType, FilterAction<?, ?>... filters) {
        Preconditions.checkNotNull(filters);
        Preconditions.checkArgument(filters.length > 0);
        @SuppressWarnings("unchecked")
        FilterChainElement<InputType, OutputType> first = (FilterChainElement<InputType, OutputType>) filters[0];
        Preconditions.checkArgument(first.getSupportedInputType().isAssignableFrom(inputType));
        Preconditions.checkArgument(first.getSupportedOutputType().isAssignableFrom(outputType));
        for (int i = 1; i < filters.length; i++) {
            FilterChainElement<?, ?> previous = (FilterChainElement<?, ?>) filters[i - 1];
            previous.setNext(filters[i]);
        }
        return new FilterChain<InputType, OutputType>(inputType, outputType, first);
    }

    private FilterChainFactory() {
    }
}
