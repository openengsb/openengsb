package org.openengsb.core.common.remote;

import java.util.Map;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterException;

/**
 * represents a chain of filters. This class basically represents just a FilterAction and can be used as final action of
 * another FilterChain.
 */
public class FilterChain implements FilterAction {
    private FilterAction firstElement;

    FilterChain(FilterAction firstElement) {
        this.firstElement = firstElement;
    }

    @Override
    public Object filter(Object input, Map<String, Object> metaData) throws FilterException {
        return firstElement.filter(input, metaData);
    }

    @Override
    public Class<?> getSupportedInputType() {
        return Object.class;
    }

    @Override
    public Class<?> getSupportedOutputType() {
        return Object.class;
    }
}
