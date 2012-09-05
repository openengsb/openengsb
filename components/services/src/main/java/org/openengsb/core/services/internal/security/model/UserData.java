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
package org.openengsb.core.services.internal.security.model;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

/**
 * represents the data of a user (or other principal) identified with a unique username.
 */
@SuppressWarnings("serial")
@Entity
@Table(name = "USERDATA")
public class UserData {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(name = "USERNAME", unique = true)
    private String username;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "USER_CREDENTIALS")
    @MapKeyColumn(name = "USER_CREDENTIAL_KEY")
    @Column(name = "USER_CREDENTIAL_VALUE")
    private Map<String, String> credentials = Maps.newHashMap();

    @OneToOne(cascade = CascadeType.ALL)
    private UserPermissionSetData permissionSet;

    @OneToMany(cascade = CascadeType.ALL)
    @MapKey(name = "key")
    @JoinTable(name = "USERDATA_ATTRIBUTES")
    private Map<String, EntryValue> attributes = Maps.newHashMap();

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

    public UserPermissionSetData getPermissionSet() {
        return permissionSet;
    }

    public void setPermissionSet(UserPermissionSetData permissionSet) {
        this.permissionSet = permissionSet;
    }

    public Map<String, EntryValue> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, EntryValue> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String toString() {
        return String.format("%s:%s:(%s permissions)", username, credentials.isEmpty() ? "none" : "****",
                permissionSet == null ? "No" : permissionSet.getPermissions().size());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username, credentials, permissionSet, attributes);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserData)) {
            return false;
        }
        final UserData other = (UserData) obj;
        return Objects.equal(username, other.username) && Objects.equal(credentials, other.credentials)
                && Objects.equal(permissionSet, other.permissionSet) && Objects.equal(attributes, other.attributes);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
