package org.openengsb.domain.contact.models;

public class KeyValuePair<T> {
    private String key;
    private T value;

    public KeyValuePair(String key, T value) {
        this.setKey(key);
        this.setValue(value);
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
