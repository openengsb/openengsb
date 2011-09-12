package org.openengsb.core.common;

import org.openengsb.core.api.security.SecurityContext;
import org.openengsb.core.api.security.model.Authentication;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class SpringSecurityContext implements SecurityContext {

    private class OpenEngSBAuthentication extends AbstractAuthenticationToken {

        private static final long serialVersionUID = -3293065282606838713L;

        private Authentication authentication;

        public OpenEngSBAuthentication(Authentication authentication) {
            super(null);
            this.authentication = authentication;
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return authentication.getUsername();
        }

        public Authentication getAuthentication() {
            return authentication;
        }

    }

    private static SpringSecurityContext instance;

    @Override
    public Authentication getAuthentication() {
        org.springframework.security.core.Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        return ((OpenEngSBAuthentication) authentication).getAuthentication();
    }

    @Override
    public void setAuthentication(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(new OpenEngSBAuthentication(authentication));
    }

    protected SpringSecurityContext() {
    }

    public static SpringSecurityContext getInstance() {
        if (instance == null) {
            instance = new SpringSecurityContext();
        }
        return instance;
    }
}
