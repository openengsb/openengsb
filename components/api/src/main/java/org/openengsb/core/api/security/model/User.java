/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.api.security.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;

public class User implements CredentialsContainer, UserDetails {

    private static final long serialVersionUID = -4422039385778330929L;

    private String password;
    private String username;
    private List<GrantedAuthority> authorities;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    /**
     * Default User
     */
    public User(String username, String password) {

        this.username = username;
        this.password = password;
        enabled = true;
        accountNonExpired = true;
        credentialsNonExpired = true;
        accountNonLocked = true;
        ArrayList<GrantedAuthority> authorities1 = new ArrayList<GrantedAuthority>();
        authorities1.add(new GrantedAuthorityImpl("ROLE_USER"));
        authorities = Collections.unmodifiableList(authorities1);
    }

    public User(String username, String password, List<GrantedAuthority> auth) {
        this.username = username;
        this.password = password;
        enabled = true;
        accountNonExpired = true;
        credentialsNonExpired = true;
        accountNonLocked = true;
        authorities = Collections.unmodifiableList(auth);
    }

    // User for searching in Database

    public User(String username) {
        this.username = username;
        enabled = true;
        accountNonExpired = true;
        credentialsNonExpired = true;
        accountNonLocked = true;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public void eraseCredentials() {
        password = null;
    }

    /**
     * Returns {@code true} if the supplied object is a {@code User} instance with the same {@code username} value.
     * <p/>
     * In other words, the objects are equal if they have the same username, representing the same principal.
     */
    @Override
    public boolean equals(Object rhs) {
        if (rhs instanceof User) {
            if (username == null) {
                return true;
            }
            return username.equals(((User) rhs).username);
        }
        return false;
    }

    /**
     * Returns the hashcode of the {@code username}.
     */
    @Override
    public int hashCode() {
        return username.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()).append(": ");
        sb.append("Username: ").append(username).append("; ");
        sb.append("Password: [PROTECTED]; ");
        sb.append("Enabled: ").append(enabled).append("; ");
        sb.append("AccountNonExpired: ").append(accountNonExpired).append("; ");
        sb.append("credentialsNonExpired: ").append(credentialsNonExpired).append("; ");
        sb.append("AccountNonLocked: ").append(accountNonLocked).append("; ");

        if (!authorities.isEmpty()) {
            sb.append("Granted Authorities: ");

            boolean first = true;
            for (GrantedAuthority auth : authorities) {
                if (!first) {
                    sb.append(",");
                }
                first = false;

                sb.append(auth);
            }
        } else {
            sb.append("Not granted any authorities");
        }

        return sb.toString();
    }
}
