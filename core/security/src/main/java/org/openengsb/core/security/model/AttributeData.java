package org.openengsb.core.security.model;

import javax.persistence.Embeddable;
import javax.persistence.Entity;

@Entity
@Embeddable
public class AttributeData {

    private String key;
    private String value;

    public AttributeData() {
    }

    public AttributeData(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
