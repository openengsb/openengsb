package org.openengsb.core.security.model;

import javax.persistence.Entity;

@Entity
public class EntryElement {

    private String type;
    private String value;

    public EntryElement(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public EntryElement() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
