package org.openengsb.core.api.security.model;

import java.lang.reflect.Method;

public interface Permission {
    boolean permits(Object service, Method operation, Object[] args);
}
