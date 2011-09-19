package org.openengsb.core.security.model;

import java.util.Collection;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;

import com.google.common.collect.Maps;

@Entity
public class PermissionSetData {

    private String type;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PERMISSIONSET_ATTRIBUTES")
    @MapKeyColumn(name = "PSET_ATTRIBUTE_KEY")
    @Column(name = "PSET_ATTRIBUTE_VALUE")
    private Map<String, String> attributes = Maps.newHashMap();

    @ManyToMany
    private Collection<PermissionData> permissions;

    @ManyToMany
    private Collection<PermissionSetData> permissionSets;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Collection<PermissionData> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<PermissionData> permissions) {
        this.permissions = permissions;
    }

    public Collection<PermissionSetData> getPermissionSets() {
        return permissionSets;
    }

    public void setPermissionSets(Collection<PermissionSetData> permissionSets) {
        this.permissionSets = permissionSets;
    }

}
