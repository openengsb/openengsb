package org.openengsb.core.api.remote;

public interface FilterChainElement<InputType, OutputType> extends FilterAction<InputType, OutputType> {
    void setNext(FilterAction<?, ?> next);
}
