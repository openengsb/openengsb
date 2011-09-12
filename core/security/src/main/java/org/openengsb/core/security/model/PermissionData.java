package org.openengsb.core.security.model;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.Table;

import com.google.common.collect.Maps;

@Entity
@Table(name = "PERMISSION")
public class PermissionData {

    @Id
    @GeneratedValue
    private Integer id;

    @MapKey
    private Map<String, String> attributes = Maps.newHashMap();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

}
