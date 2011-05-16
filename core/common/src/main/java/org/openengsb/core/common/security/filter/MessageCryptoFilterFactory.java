package org.openengsb.core.common.security.filter;

import org.openengsb.core.api.remote.FilterChainElement;
import org.openengsb.core.api.remote.FilterChainElementFactory;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.common.security.PrivateKeySource;

public class MessageCryptoFilterFactory implements FilterChainElementFactory {

    private PrivateKeySource privateKeySource;

    public MessageCryptoFilterFactory() {
    }

    public MessageCryptoFilterFactory(PrivateKeySource privateKeySource) {
        this.privateKeySource = privateKeySource;
    }

    @Override
    public FilterChainElement newInstance() throws FilterConfigurationException {
        return new MessageCryptoFilter(privateKeySource);
    }

    public void setPrivateKeySource(PrivateKeySource privateKeySource) {
        this.privateKeySource = privateKeySource;
    }

}
