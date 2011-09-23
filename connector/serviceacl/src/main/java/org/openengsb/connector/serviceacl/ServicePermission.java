package org.openengsb.connector.serviceacl;

import org.openengsb.core.api.security.model.Permission;

public class ServicePermission implements Permission {

    private String type;
    private String instance;
    private String operation;
    private String context;

    public ServicePermission() {
    }

    public ServicePermission(String type) {
        this.type = type;
    }

    public ServicePermission(String type, String operation) {
        this.type = type;
        this.operation = operation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public String describe() {
        return null;
    }
}
