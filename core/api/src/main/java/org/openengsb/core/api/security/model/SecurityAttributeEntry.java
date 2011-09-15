package org.openengsb.core.api.security.model;

public class SecurityAttributeEntry {

    private String componentName;
    private String action;

    public SecurityAttributeEntry() {
    }

    public SecurityAttributeEntry(String componentName) {
        this.componentName = componentName;
    }

    public SecurityAttributeEntry(String componentName, String action) {
        this.componentName = componentName;
        this.action = action;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", componentName, action);
    }

}
