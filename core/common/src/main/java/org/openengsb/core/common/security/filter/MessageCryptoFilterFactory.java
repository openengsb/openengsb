package org.openengsb.core.common.security.filter;

import org.openengsb.core.api.remote.FilterChainElement;
import org.openengsb.core.api.remote.FilterChainElementFactory;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.common.security.PrivateKeySource;

public class MessageCryptoFilterFactory implements FilterChainElementFactory {

    private PrivateKeySource privateKeySource;
    private String secretKeyAlgorithm;

    public MessageCryptoFilterFactory() {
    }

    public MessageCryptoFilterFactory(PrivateKeySource privateKeySource, String secretKeyAlgorithm) {
        this.privateKeySource = privateKeySource;
        this.secretKeyAlgorithm = secretKeyAlgorithm;
    }

    @Override
    public FilterChainElement newInstance() throws FilterConfigurationException {
        return new MessageCryptoFilter(privateKeySource, secretKeyAlgorithm);
    }

    public void setPrivateKeySource(PrivateKeySource privateKeySource) {
        this.privateKeySource = privateKeySource;
    }

    public void setSecretKeyAlgorithm(String secretKeyAlgorithm) {
        this.secretKeyAlgorithm = secretKeyAlgorithm;
    }

}
