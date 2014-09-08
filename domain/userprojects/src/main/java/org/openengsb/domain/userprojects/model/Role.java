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

package org.openengsb.domain.userprojects.model;

import java.util.List;
import java.util.Objects;

import org.openengsb.core.api.Constants;
import org.openengsb.core.api.model.annotation.Model;
import org.openengsb.core.api.model.annotation.OpenEngSBModelId;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.labs.delegation.service.Provide;

import com.google.common.collect.Lists;

@Provide(context = { Constants.DELEGATION_CONTEXT_MODELS })
@Model
public class Role {

    @OpenEngSBModelId
    private String name;

    private List<String> roles = Lists.newArrayList();

    private List<Permission> permissions = Lists.newArrayList();

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, roles, permissions);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Role)) {
            return false;
        }
        final Role other = (Role) obj;
        return Objects.equals(name, other.name) && Objects.equals(roles, other.roles)
            && Objects.equals(permissions, other.permissions);
    }
}
