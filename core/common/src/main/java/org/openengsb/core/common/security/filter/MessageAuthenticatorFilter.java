package org.openengsb.core.common.security.filter;

import java.util.Map;

import org.openengsb.core.api.remote.FilterAction;
import org.openengsb.core.api.remote.FilterConfigurationException;
import org.openengsb.core.api.security.model.SecureRequest;
import org.openengsb.core.api.security.model.SecureResponse;
import org.openengsb.core.common.remote.AbstractFilterChainElement;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class MessageAuthenticatorFilter extends AbstractFilterChainElement<SecureRequest, SecureResponse> {

    private FilterAction next;
    private AuthenticationManager authenticationManager;

    public MessageAuthenticatorFilter(AuthenticationManager authenticationManager) {
        super(SecureRequest.class, SecureResponse.class);
        this.authenticationManager = authenticationManager;
    }

    @Override
    protected SecureResponse doFilter(SecureRequest input, Map<String, Object> metaData) {
        Authentication authentication = input.retrieveAuthenticationInfo().toSpringSecurityAuthentication();
        Authentication authenticated = authenticationManager.authenticate(authentication);
        SecurityContextHolder.getContext().setAuthentication(authenticated);
        return (SecureResponse) next.filter(input, metaData);
    }

    @Override
    public void setNext(FilterAction next) throws FilterConfigurationException {
        checkNextInputAndOutputTypes(next, SecureRequest.class, SecureResponse.class);
        this.next = next;
    }
}
