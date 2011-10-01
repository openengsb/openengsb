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
package org.openengsb.core.security.internal.model;

import java.util.Collection;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToMany;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

/**
 * entity that represents a permission-set. Contains the associated permissions and other permission-sets along with
 * additional metadata
 */
@Entity
@Table(name = "PERMISSIONSETDATA")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class PermissionSetData {

    @Id
    private String id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PERMISSIONSET_METADATA")
    @MapKeyColumn(name = "PS_METADATA_KEY")
    @Column(name = "PS_METADATA_VALUE")
    private Map<String, String> metadata;

    @OneToMany(cascade = CascadeType.ALL)
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

    @Override
    public String toString() {
        Collection<String> children = Collections2.transform(permissionSets, new Function<PermissionSetData, String>() {
            @Override
            public String apply(PermissionSetData input) {
                return input.getId();
            };
        });
        return String.format("Set %s: %s, %s", id, children, permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, metadata, permissions, permissionSets);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PermissionSetData)) {
            return false;
        }
        final PermissionSetData other = (PermissionSetData) obj;
        return Objects.equal(id, other.id) && Objects.equal(metadata, other.metadata) &&
                Objects.equal(permissions, other.permissions) && Objects.equal(permissionSets, other.permissionSets);
    }
}
