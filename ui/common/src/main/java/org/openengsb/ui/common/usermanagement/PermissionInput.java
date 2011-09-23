package org.openengsb.ui.common.usermanagement;

import java.io.Serializable;
import java.util.Map;

public class PermissionInput implements Serializable {
    private static final long serialVersionUID = 1769481908418793294L;

    private Class<?> type;

    private Map<String, String> values;

    public PermissionInput(Class<?> type, Map<String, String> values) {
        this.values = values;
        this.type = type;
    }

    public PermissionInput() {
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }

}
