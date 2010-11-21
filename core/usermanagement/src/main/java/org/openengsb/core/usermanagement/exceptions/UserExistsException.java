package org.openengsb.core.usermanagement.exceptions;

import org.springframework.security.core.AuthenticationException;

public class UserExistsException extends AuthenticationException {
    public UserExistsException(String msg, Throwable t) {
        super(msg, t);
    }

    public UserExistsException(String msg) {
        super(msg);
    }

    public UserExistsException(String msg, Object extraInformation) {
        super(msg, extraInformation);
    }
}