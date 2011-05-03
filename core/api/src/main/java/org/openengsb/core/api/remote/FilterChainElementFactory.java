package org.openengsb.core.api.remote;

public interface FilterChainElementFactory<InputType, OutputType> {

    FilterChainElement<InputType, OutputType> newInstance();

}
