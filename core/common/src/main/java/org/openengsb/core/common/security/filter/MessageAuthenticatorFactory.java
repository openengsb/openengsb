package org.openengsb.core.common.security.filter;

import org.openengsb.core.api.remote.FilterChainElement;
import org.openengsb.core.api.remote.FilterChainElementFactory;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.springframework.security.authentication.AuthenticationManager;

public class MessageAuthenticatorFactory implements FilterChainElementFactory {

    private AuthenticationManager authenticationManager;

    @Override
    public FilterChainElement newInstance() throws FilterConfigurationException {
        return new MessageAuthenticatorFilter(authenticationManager);
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }
}
