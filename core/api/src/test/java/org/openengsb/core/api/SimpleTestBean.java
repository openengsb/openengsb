package org.openengsb.core.api;

public class SimpleTestBean {
    String stringValue;
    Long longValue;

    public SimpleTestBean() {
    }

    public SimpleTestBean(String stringValue, Long longValue) {
        super();
        this.stringValue = stringValue;
        this.longValue = longValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }
}