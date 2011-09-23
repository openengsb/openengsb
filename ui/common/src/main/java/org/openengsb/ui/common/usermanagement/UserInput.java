package org.openengsb.ui.common.usermanagement;

import java.io.Serializable;
import java.util.Collection;

import com.google.common.collect.Lists;

class UserInput implements Serializable {
    private static final long serialVersionUID = 8089287572532176946L;

    private String passwordVerification;
    private String password;
    private String username;
    private Collection<PermissionInput> newPermissions = Lists.newArrayList();

    public String getPasswordVerification() {
        return passwordVerification;
    }

    public void setPasswordVerification(String passwordVerification) {
        this.passwordVerification = passwordVerification;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Collection<PermissionInput> getNewPermissions() {
        return newPermissions;
    }

    public void setNewPermissions(Collection<PermissionInput> newPermissions) {
        this.newPermissions = newPermissions;
    }

}
