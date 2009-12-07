/**
 * 
 */
package org.openengsb.core;

public class MethodCall {
    private final String methodName;
    private final Object[] args;
    private final Class<?>[] types;

    public MethodCall(String methodName, Object[] args, Class<?>[] types) {
        this.methodName = methodName;
        this.args = args;
        this.types = types;
    }

    public Class<?>[] getTypes() {
        return types;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArgs() {
        return args;
    }
}