package org.openengsb.core.ekb.api;


public class QueryProjection {
    private final Class<?> resultClass;
    private final String[] properties;

    public QueryProjection(Class<?> resultClass, String... properties) {
        this.resultClass = resultClass;
        this.properties = properties;
    }

    public String[] getProperties() {
        return properties;
    }

    public Class<?> getResultClass() {
        return resultClass;
    }
}
