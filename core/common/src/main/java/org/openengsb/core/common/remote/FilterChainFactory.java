package org.openengsb.core.common.remote;

import java.util.Iterator;
import java.util.List;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterChainElement;
import org.openengsb.core.api.remote.FilterChainElementFactory;
import org.openengsb.core.api.remote.FilterConfigurationException;

/**
 * This class makes it possible to configure a FilterChain as a bean (e.g. via blueprint).
 */
public class FilterChainFactory<InputType, OutputType> {

    private List<FilterChainElementFactory<?, ?>> filters;
    private FilterAction<?, ?> last;

    private Class<InputType> inputType;
    private Class<OutputType> outputType;

    /**
     * The filters-list must be set. If the last-element is not set, the last element of the filters-list will be used.
     *
     * @throws FilterConfigurationException if the filters in the filter-list are not compatible with each other
     */
    @SuppressWarnings("unchecked")
    public FilterAction<InputType, OutputType> create() throws FilterConfigurationException {
        Iterator<FilterChainElementFactory<?, ?>> iterator = filters.iterator();
        if (!iterator.hasNext()) {
            throw new IllegalStateException("Need at least one filter");
        }
        FilterChainElementFactory<?, ?> firstFactory = iterator.next();
        FilterChainElement<?, ?> firstInstance = firstFactory.newInstance();
        if (!firstInstance.getSupportedInputType().isAssignableFrom(inputType)
                || !firstInstance.getSupportedOutputType().isAssignableFrom(outputType)) {
            throw new FilterConfigurationException("incompatible Filtertype");
        }
        FilterChainElement<?, ?> current = firstInstance;
        while (iterator.hasNext()) {
            FilterChainElement<?, ?> next = iterator.next().newInstance();
            current.setNext(next);
            current = next;
        }
        if (last != null) {
            current.setNext(last);
        }
        return (FilterAction<InputType, OutputType>) firstInstance;
    }

    public void setFilters(List<FilterChainElementFactory<?, ?>> filters) {
        this.filters = filters;
    }

    public void setLast(FilterAction<?, ?> last) {
        this.last = last;
    }

    public void setInputType(Class<InputType> inputType) {
        this.inputType = inputType;
    }

    public void setOutputType(Class<OutputType> outputType) {
        this.outputType = outputType;
    }

    public FilterChainFactory() {
    }

    public FilterChainFactory(Class<InputType> inputType, Class<OutputType> outputType) {
        this.inputType = inputType;
        this.outputType = outputType;
    }

}
