package org.openengsb.core.api.remote;

/**
 * Instances of {@link FilterChainElement}s are not reusable because the next-attribute ties it to a specific chain. So
 * when configuring Filterchains a factory should be used to combine filters.
 */
public interface FilterChainElementFactory<InputType, OutputType> {

    FilterChainElement<InputType, OutputType> newInstance() throws FilterConfigurationException;

}
