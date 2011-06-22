package org.openengsb.core.api.model;

/**
 * Simple Model Entry class. Every model entry has three fields: key, value and type. Key defines the id, value is
 * the value for the key and type defines the type of the value.
 */
public class OpenEngSBModelEntry {
    private String key;
    private Object value;
    private Class<?> type;
    
    public OpenEngSBModelEntry(String key, Object value, Class<?> type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }
    
    public String getKey() {
        return key;
    }
    
    public Object getValue() {
        return value;
    }
    
    public Class<?> getType() {
        return type;
    }

}
