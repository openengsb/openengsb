package org.openengsb.core.security.internal;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;

public class SimpleMethodInvocation implements MethodInvocation {

    private Object target;
    private Method method;
    private Object[] arguments;

    public SimpleMethodInvocation(Object object, String methodName, Class<?>... parameterTypes) {
        this.target = object;
        try {
            method = target.getClass().getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
        arguments = new Object[0];
    }

    public SimpleMethodInvocation(Object object, String methodName, Object... arguments) {
        this.target = object;
        Class<?>[] parameterTypes = new Class<?>[arguments.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = arguments[i].getClass();
        }
        try {
            method = target.getClass().getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
        this.arguments = arguments;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Object proceed() throws Throwable {
        return method.invoke(target, arguments);
    }

    @Override
    public Object getThis() {
        return target;
    }

    @Override
    public AccessibleObject getStaticPart() {
        return method;
    }

    @Override
    public Method getMethod() {
        return method;
    }

}
