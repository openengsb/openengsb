package org.openengsb.core.common.remote;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterChainElement;
import org.openengsb.core.api.remote.FilterChainElementFactory;
import org.openengsb.core.api.remote.FilterConfigurationException;

import com.google.common.base.Preconditions;

/**
 * This class makes it possible to configure a FilterChain as a bean (e.g. via blueprint).
 */
public class FilterChainFactory<InputType, OutputType> {
    private List<Object> filters;

    private Class<InputType> inputType;
    private Class<OutputType> outputType;

    private final class FilterChain extends AbstractFilterAction<InputType, OutputType> {
        private FilterAction firstElement;

        protected FilterChain(FilterAction lastElement) {
            super(inputType, outputType);
            this.firstElement = lastElement;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected OutputType doFilter(InputType input, Map<String, Object> metadata) {
            return (OutputType) firstElement.filter(input, metadata);
        };
    }

    /**
     * The filters-list must be set. If the last-element is not set, the last element of the filters-list will be used.
     *
     * @throws FilterConfigurationException if the filters in the filter-list are not compatible with each other
     */
    public FilterAction create() throws FilterConfigurationException {
        Preconditions.checkState(filters != null, "list of filters must be set");
        Preconditions.checkState(inputType != null, "inputType must be set");
        Preconditions.checkState(outputType != null, "outputType must be set");

        Preconditions.checkState(filters.size() > 0, "need at least one filter");
        validateFiltersList();

        Iterator<Object> iterator = filters.iterator();

        FilterChainElement firstInstance = getInstanceFromListElement(iterator.next());
        if (!firstInstance.getSupportedInputType().isAssignableFrom(inputType)
                || !firstInstance.getSupportedOutputType().isAssignableFrom(outputType)) {
            throw new FilterConfigurationException(String.format("incompatible Filtertype (%s->%s) - (%s->%s)",
                inputType, outputType, firstInstance.getSupportedInputType(), firstInstance.getSupportedOutputType()));
        }
        FilterChainElement current = firstInstance;
        while (iterator.hasNext()) {
            Object next = iterator.next();
            FilterChainElement nextFilterElement = getInstanceFromListElement(next);
            if (nextFilterElement == null) {
                current.setNext((FilterAction) next);
                break;
            }
            current.setNext(nextFilterElement);
            current = nextFilterElement;
        }
        return new FilterChain(firstInstance);
    }

    private FilterChainElement getInstanceFromListElement(Object next) throws FilterConfigurationException {
        if (next instanceof Class) {
            try {
                return (FilterChainElement) ((Class<?>) next).newInstance();
            } catch (InstantiationException e) {
                throw new FilterConfigurationException("Exception when instantiating FilterAction", e);
            } catch (IllegalAccessException e) {
                throw new FilterConfigurationException("Exception when instantiating FilterAction", e);
            }
        }
        if (next instanceof FilterChainElementFactory) {
            return ((FilterChainElementFactory) next).newInstance();
        }
        return null;
    }

    public void setFilters(List<Object> filters) {
        this.filters = filters;
    }

    public void setInputType(Class<InputType> inputType) {
        this.inputType = inputType;
    }

    public void setOutputType(Class<OutputType> outputType) {
        this.outputType = outputType;
    }

    private void validateFiltersList() {
        Iterator<Object> iterator = filters.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            if (element instanceof FilterAction && !(element instanceof FilterChainElement)) {
                // must be final action
                break;
            }
            validateElement(element);
        }
        if (iterator.hasNext()) {
            throw new FilterConfigurationException("Cannot add more filter-actions after final element");
        }
    }

    private void validateElement(Object object) {

        Class<? extends Object> objClass = object.getClass();
        // Allow instances of Factories and filters
        if (object instanceof FilterChainElementFactory) {
            return;
        }
        // Allow FilterAction-classes with proper default-constructors
        if (Class.class.isAssignableFrom(objClass)) {
            Class<?> filterClass = (Class<?>) object;
            if (!FilterAction.class.isAssignableFrom(filterClass)) {
                throw new FilterConfigurationException(String.format(
                    "Incompatible type: %s, Class must be derived from one of the following: %s", filterClass,
                    FilterAction.class.getName()));
            }
            try {
                filterClass.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new FilterConfigurationException("Filter-class must have a visible default constructor", e);
            }
            return;
        }
        throw new FilterConfigurationException(String.format("Element %s is not a valid FilterElement",
            object.toString()));
    }

    public FilterChainFactory() {
    }

    public FilterChainFactory(Class<InputType> inputType, Class<OutputType> outputType) {
        this.inputType = inputType;
        this.outputType = outputType;
    }

}
