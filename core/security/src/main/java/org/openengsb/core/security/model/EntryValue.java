package org.openengsb.core.security.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

@Entity
public class EntryValue {

    private String key;

    @OneToMany(cascade = CascadeType.ALL)
    private List<EntryElement> value;

    public EntryValue() {
    }

    public EntryValue(String key, List<EntryElement> value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<EntryElement> getValue() {
        return value;
    }

    public void setValue(List<EntryElement> value) {
        this.value = value;
    }

}
