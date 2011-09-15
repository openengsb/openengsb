package org.openengsb.core.api;

import java.util.Collection;
import java.util.Map;

import org.openengsb.core.api.security.model.SecurityAttributeEntry;

public class GenericControlledObject {

    private Collection<SecurityAttributeEntry> securityAttributes;

    private String action;

    private Map<String, Object> metaData;

    public GenericControlledObject() {
    }

    public GenericControlledObject(Collection<SecurityAttributeEntry> securityAttributes) {
        this.securityAttributes = securityAttributes;
    }

    public GenericControlledObject(Collection<SecurityAttributeEntry> securityAttributes, String action) {
        this.securityAttributes = securityAttributes;
        this.action = action;
    }

    public GenericControlledObject(Collection<SecurityAttributeEntry> securityAttributes, Map<String, Object> metaData) {
        this.securityAttributes = securityAttributes;
        this.metaData = metaData;
    }

    public GenericControlledObject(Collection<SecurityAttributeEntry> securityAttributes, String action,
            Map<String, Object> metaData) {
        this.securityAttributes = securityAttributes;
        this.action = action;
        this.metaData = metaData;
    }

    public Collection<SecurityAttributeEntry> getSecurityAttributes() {
        return securityAttributes;
    }

    public void setSecurityAttributes(Collection<SecurityAttributeEntry> securityAttributes) {
        this.securityAttributes = securityAttributes;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }

}
