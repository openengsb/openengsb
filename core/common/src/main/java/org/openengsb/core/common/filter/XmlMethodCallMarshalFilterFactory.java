package org.openengsb.core.common.filter;

import org.openengsb.core.api.remote.FilterChainElement;
import org.openengsb.core.api.remote.FilterChainElementFactory;
import org.w3c.dom.Document;

public class XmlMethodCallMarshalFilterFactory implements FilterChainElementFactory<Document, Document> {

    @Override
    public FilterChainElement<Document, Document> newInstance() {
        return new XmlMethodCallMarshalFilter();
    }

}
