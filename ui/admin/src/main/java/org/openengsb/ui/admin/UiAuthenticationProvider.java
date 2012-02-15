package org.openengsb.ui.admin;

import org.apache.shiro.authc.AbstractAuthenticator;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.security.model.Authentication;
import org.openengsb.domain.authentication.AuthenticationDomain;
import org.ops4j.pax.wicket.api.PaxWicketBean;

public class UiAuthenticationProvider extends AbstractAuthenticator {

    @PaxWicketBean(name = "authenticator")
    private AuthenticationDomain authenticator;

    @Override
    protected AuthenticationInfo doAuthenticate(AuthenticationToken token) throws AuthenticationException {
        try {
            Authentication authenticate =
                authenticator.authenticate(token.getPrincipal().toString(),
                    new Password(new String((char[]) token.getCredentials())
                        .toString()));
            return new SimpleAuthenticationInfo(authenticate.getUsername(), authenticate.getCredentials(),
                "default");
        } catch (org.openengsb.domain.authentication.AuthenticationException e) {
            throw new AuthenticationException(e);
        }
    }

    public void setAuthenticator(AuthenticationDomain authenticator) {
        this.authenticator = authenticator;
    }
}
