package org.openengsb.core.security;

import java.util.Collection;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;

public class AdminRoleVoter extends AbstractAccessDecisionVoter {

    private static final GrantedAuthority ADMIN_ROLE = new GrantedAuthorityImpl("ROLE_ADMIN");

    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        Object user = authentication.getPrincipal();
        if (!(user instanceof UserDetails)) {
            return ACCESS_ABSTAIN;
        }
        if (((UserDetails) user).getAuthorities().contains(ADMIN_ROLE)) {
            return ACCESS_GRANTED;
        }
        return ACCESS_ABSTAIN;
    }
}
