package org.openengsb.core.security.model;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

import com.google.common.collect.Maps;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class BeanData {

    private String type;

    @OneToMany(cascade = CascadeType.ALL)
    @MapKey(name = "key")
    private Map<String, EntryValue> attributes = Maps.newHashMap();

    public BeanData(String type, Map<String, EntryValue> attributes) {
        this.type = type;
        this.attributes = attributes;
    }

    public BeanData() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, EntryValue> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, EntryValue> attributes) {
        this.attributes = attributes;
    }

}
