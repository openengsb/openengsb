package org.openengsb.core.api.security.model;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

public class UsernamePasswordAuthenticationInfo implements AuthenticationInfo {

    private String username;
    private String password;

    public UsernamePasswordAuthenticationInfo(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public UsernamePasswordAuthenticationInfo() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public Authentication toSpringSecurityAuthentication() {
        return new UsernamePasswordAuthenticationToken(username, password);
    }

}
