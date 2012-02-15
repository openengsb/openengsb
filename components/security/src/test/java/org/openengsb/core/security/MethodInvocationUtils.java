package org.openengsb.core.security;

import org.aopalliance.intercept.MethodInvocation;
import org.openengsb.core.security.internal.SimpleMethodInvocation;

public class MethodInvocationUtils {

    public static MethodInvocation create(Object object, String methodName) {
        return new SimpleMethodInvocation(object, methodName);
    }

    public static MethodInvocation create(Object object, String methodName, Object... objects) {
        return new SimpleMethodInvocation(object, methodName, objects);
    }
}
