package org.openengsb.core.common.security.filter;

import java.util.LinkedList;
import java.util.List;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.remote.FilterChainFactory;
import org.springframework.security.authentication.AuthenticationManager;

public class DefaultSecureMethodCallFilterFactory {

    private AuthenticationManager authenticationManager;
    private FilterAction handler;

    public DefaultSecureMethodCallFilterFactory() {
    }

    public FilterAction create() throws FilterConfigurationException {
        FilterChainFactory<SecureRequest, SecureResponse> factory =
            new FilterChainFactory<SecureRequest, SecureResponse>(SecureRequest.class, SecureResponse.class);
        List<Object> filterFactories = new LinkedList<Object>();

        filterFactories.add(MessageVerifierFilter.class);

        MessageAuthenticatorFactory messageAuthenticatorFactory = new MessageAuthenticatorFactory();
        messageAuthenticatorFactory.setAuthenticationManager(authenticationManager);
        filterFactories.add(messageAuthenticatorFactory);

        filterFactories.add(WrapperFilter.class);
        filterFactories.add(handler);
        factory.setFilters(filterFactories);
        return factory.create();
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setHandler(FilterAction handler) {
        this.handler = handler;
    }

}
