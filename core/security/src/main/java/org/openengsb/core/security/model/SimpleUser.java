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

package org.openengsb.core.security.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.openengsb.core.common.util.Users;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

@Table(name = "SIMPLEUSER")
@Entity
public class SimpleUser {

    @Id
    @Column(name = "USER", length = 100)
    private String username;

    @Column(name = "PASSWORD", length = 100)
    private String password;

    @Column(name = "ROLES")
    @ElementCollection
    private Collection<String> roles;

    public SimpleUser(String username) {
        this.username = username;
    }

    public SimpleUser(String username, String password, Collection<String> roles) {
        this.username = username;
        this.password = password;
    }

    public SimpleUser(String username, String password) {
        this(username, password, null);
    }

    public SimpleUser() {
    }

    public SimpleUser(UserDetails user) {
        this(user.getUsername(), user.getPassword());
        roles = convertAuthorityList(user.getAuthorities());
    }

    public static Collection<String> convertAuthorityList(Collection<GrantedAuthority> authorities) {
        if (authorities == null) {
            return null;
        }
        return Collections2.transform(authorities, new Function<GrantedAuthority, String>() {
            @Override
            public String apply(GrantedAuthority input) {
                return input.getAuthority();
            };
        });
    }

    private static Collection<GrantedAuthority> convertToAuthorities(Collection<String> authorities) {
        if (authorities == null) {
            return null;
        }
        return Collections2.transform(authorities, new Function<String, GrantedAuthority>() {
            @Override
            public GrantedAuthority apply(String input) {
                return new GrantedAuthorityImpl(input);
            };
        });
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

    public Collection<String> getRoles() {
        return roles;
    }

    public void setRoles(Collection<String> roles) {
        this.roles = roles;
    }

    public UserDetails toSpringUser() {
        if (roles == null) {
            return Users.create(username, password);
        }
        Collection<GrantedAuthority> authorities = convertToAuthorities(roles);
        return Users.create(username, password, new ArrayList<GrantedAuthority>(authorities));
    }

}
