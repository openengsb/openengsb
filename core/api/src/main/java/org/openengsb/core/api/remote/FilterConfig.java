package org.openengsb.core.api.remote;

import java.util.LinkedList;
import java.util.List;

public class FilterConfig<InputType, ResultType> {

    private List<FilterAction<?, ?>> filterChain = new LinkedList<FilterAction<?, ?>>();

    public FilterConfig(List<FilterAction<?, ?>> filterChain) {
        this.filterChain = filterChain;
    }

    @SuppressWarnings("unchecked")
    public ResultType execute(InputType input) {
        Object currentResult = input;
        for (FilterAction<?, ?> a : filterChain) {
            currentResult = ((FilterAction<Object, Object>) a).apply(currentResult);
        }
        return (ResultType) currentResult;
    }
}
