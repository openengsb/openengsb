package org.openengsb.core.security.model;

import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.Table;

import com.google.common.collect.Maps;

@Entity
@Table(name = "USERDATA")
public class UserData {

    @Id
    private String username;

    @MapKey
    private Map<String, String> credentials = Maps.newHashMap();

    @MapKey(name = "type")
    private Map<String, PermissionData> permissions = Maps.newHashMap();

    @MapKey
    private Map<String, String> attributes = Maps.newHashMap();

    public UserData() {
    }

    public UserData(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Map<String, String> getCredentials() {
        return credentials;
    }

    public void setCredentials(Map<String, String> credentials) {
        this.credentials = credentials;
    }

    public Map<String, PermissionData> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, PermissionData> permissions) {
        this.permissions = permissions;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

}
