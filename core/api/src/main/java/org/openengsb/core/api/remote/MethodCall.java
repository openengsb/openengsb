package org.openengsb.core.api.remote;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodCall {

    private String methodName;
    private Object[] args;
    private Map<String, String> metaData;
    private List<String> classes;

    public MethodCall() {
    }

    public MethodCall(String methodName, Object[] args) {
        this(methodName, args, new HashMap<String, String>());
    }

    public MethodCall(String methodName, Object[] args, List<String> classes) {
        this(methodName, args, new HashMap<String, String>(), classes);
    }

    public MethodCall(String methodName, Object[] args, Map<String, String> metaData, List<String> classes) {
        super();
        this.methodName = methodName;
        this.args = args;
        this.metaData = metaData;
        this.classes = classes;
    }

    public MethodCall(String methodName, Object[] args, Map<String, String> metaData) {
        this(methodName, args, metaData, null);
        classes = getRealClassImplementation();
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Map<String, String> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, String> metaData) {
        this.metaData = metaData;
    }

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    public List<String> getRealClassImplementation() {
        List<String> argsClasses = new ArrayList<String>();
        if (getArgs() != null) {
            for (Object object : getArgs()) {
                if (object instanceof List<?>) {
                    argsClasses.add(List.class.getName());
                } else {
                    argsClasses.add(object.getClass().getName());
                }
            }
        }
        return argsClasses;
    }

}
