package org.openengsb.core.common.remote;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterChainElement;
import org.openengsb.core.api.remote.FilterChainElementFactory;
import org.openengsb.core.api.remote.FilterConfigurationException;

/**
 * This Factory can be used for {@link FilterChainElement}-classes that do not require any configuration-attributes or
 * special setup. Only {@link FilterAction}s with a public default-constructor may be used with this Factory.
 *
 */
public class DefaultFilterChainElementFactory<InputType, OutputType> implements
        FilterChainElementFactory<InputType, OutputType> {

    private Class<? extends FilterChainElement<InputType, OutputType>> type;

    public DefaultFilterChainElementFactory(Class<? extends FilterChainElement<InputType, OutputType>> type) {
        this.setType(type);
    }

    public DefaultFilterChainElementFactory() {
    }

    @Override
    public FilterChainElement<InputType, OutputType> newInstance() throws FilterConfigurationException {
        try {
            return type.newInstance();
        } catch (InstantiationException e) {
            throw new FilterConfigurationException(e);
        } catch (IllegalAccessException e) {
            throw new FilterConfigurationException(e);
        }
    }

    public void setType(Class<? extends FilterChainElement<InputType, OutputType>> type) {
        try {
            type.getConstructor();
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(
                "FilterChainElements must contain a default-constructor to be able to use this factory." +
                        "Please consult the java-doc for this class", e);
        }

        this.type = type;
    }
}
