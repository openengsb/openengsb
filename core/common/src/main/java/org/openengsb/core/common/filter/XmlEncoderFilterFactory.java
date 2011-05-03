package org.openengsb.core.common.filter;

import org.openengsb.core.api.remote.FilterChainElement;
import org.openengsb.core.api.remote.FilterChainElementFactory;

public class XmlEncoderFilterFactory implements FilterChainElementFactory<String, String> {

    @Override
    public FilterChainElement<String, String> newInstance() {
        return new XmlEncoderFilter();
    }

}
