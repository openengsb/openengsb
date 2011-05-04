package org.openengsb.core.common.security.filter;

import java.security.PrivateKey;

import org.openengsb.core.api.remote.FilterChainElement;
import org.openengsb.core.api.remote.FilterChainElementFactory;
import org.openengsb.core.api.remote.FilterConfigurationException;

public class MessageCryptoFilterFactory implements FilterChainElementFactory {

    private PrivateKey privateKey;

    public MessageCryptoFilterFactory(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public FilterChainElement newInstance() throws FilterConfigurationException {
        MessageCryptoFilter result = new MessageCryptoFilter();
        result.setPrivateKey(privateKey);
        return result;
    }

}
