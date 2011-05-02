package org.openengsb.core.api.security.model;

import org.springframework.security.core.Authentication;

public interface AuthenticationInfo {

    Authentication toSpringSecurityAuthentication();

}
