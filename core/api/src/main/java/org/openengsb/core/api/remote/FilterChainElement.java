package org.openengsb.core.api.remote;

/**
 * {@link FilterAction}s can always be used as last element of a filter-chain. {@link FilterAction} that are designed to
 * use other filters to calculate their result, must implement this interface. This way the filters can be exchanged
 * easily if they support the same Input and Output types.
 *
 * A separate interface is used because the last element in a filterchain would not need a setNext-method
 *
 * {@link FilterChainElement}s should never be instantiated directly. A {@link FilterChainElementFactory} should be used
 * for that purpose.
 */
public interface FilterChainElement<InputType, OutputType> extends FilterAction<InputType, OutputType> {

    /**
     * used to integrate the filter in the chain. The implementation of this method should first check if the given
     * filter is compatible and throw a {@link FilterConfigurationException} if the types do not match.
     *
     * @throws FilterConfigurationException if the {@link FilterAction}s are not compatible
     */
    void setNext(FilterAction<?, ?> next) throws FilterConfigurationException;

}
