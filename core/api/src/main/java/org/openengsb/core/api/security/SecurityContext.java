package org.openengsb.core.api.security;

import org.openengsb.core.api.security.model.Authentication;

public interface SecurityContext {
    Authentication getAuthentication();

    void setAuthentication(Authentication auth);
}
