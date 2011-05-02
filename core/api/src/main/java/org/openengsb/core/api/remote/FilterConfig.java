package org.openengsb.core.api.remote;

import java.util.LinkedList;
import java.util.List;

public class FilterConfig<InputType, ResultType> implements FilterAction<InputType, ResultType> {

    private List<FilterAction<?, ?>> filterChain = new LinkedList<FilterAction<?, ?>>();

    public FilterConfig() {
    }

    public FilterConfig(List<FilterAction<?, ?>> filterChain) {
        this.filterChain = filterChain;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResultType apply(InputType input) throws FilterException {
        Object currentResult = input;
        for (FilterAction<?, ?> a : filterChain) {
            currentResult = ((FilterAction<Object, Object>) a).apply(currentResult);
        }
        return (ResultType) currentResult;
    };

    public void setFilterChain(List<FilterAction<?, ?>> filterChain) {
        this.filterChain = filterChain;
    }

}
