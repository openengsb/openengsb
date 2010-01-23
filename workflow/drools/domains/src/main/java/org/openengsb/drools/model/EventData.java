package org.openengsb.drools.model;

public class EventData {

    private String key;

    private Object value;

    @SuppressWarnings("unused")
    private EventData() {
        // used by rpc framework
    }

    public EventData(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

}
