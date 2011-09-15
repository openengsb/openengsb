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

import java.util.Collection;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.common.collect.Sets;

@Entity
@Table(name = "USERDATA")
public class UserData {

    @Id
    private String username;

    // @MapKey
    // private Map<String, String> credentials;

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<CredentialData> credentials = Sets.newHashSet();

    @OneToMany(cascade = CascadeType.ALL)
    private Collection<PermissionData> permissions;

    @MapKey
    private Map<String, String> attributes;

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

    public Collection<CredentialData> getCredentials() {
        return credentials;
    }

    public void setCredentials(Collection<CredentialData> credentials) {
        this.credentials = credentials;
    }

    public Collection<PermissionData> getPermissions() {
        return permissions;
    }

    public void setPermissions(Collection<PermissionData> permissions) {
        this.permissions = permissions;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return String.format("%s:%s", username, credentials.isEmpty() ? "none" : "****");
    }

}
