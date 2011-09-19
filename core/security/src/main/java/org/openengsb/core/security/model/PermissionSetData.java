package org.openengsb.core.security.model;

import java.util.Collection;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;

import com.google.common.collect.Sets;

@Entity
public class PermissionSetData {

    @Id
    private String id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PERMISSIONSET_METADATA")
    @MapKeyColumn(name = "PS_METADATA_KEY")
    @Column(name = "PS_METADATA_VALUE")
    private Map<String, String> metadata;

    @ManyToMany(cascade = CascadeType.ALL)
    private Collection<PermissionData> permissions = Sets.newHashSet();

    @ManyToMany(cascade = CascadeType.ALL)
    private Collection<PermissionSetData> permissionSets = Sets.newHashSet();

    public PermissionSetData() {
    }

    public PermissionSetData(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
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
