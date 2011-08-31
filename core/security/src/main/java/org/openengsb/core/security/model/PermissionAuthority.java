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

import org.openengsb.core.api.security.model.Permission;

import com.google.common.collect.Lists;

public class PermissionAuthority implements OpenEngSBGrantedAuthority {

    private static final long serialVersionUID = -8184393786529026862L;

    private Permission permission;

    public PermissionAuthority() {
    }

    public PermissionAuthority(Permission permission) {
        this.permission = permission;
    }

    @Override
    public Collection<Permission> getPermissions() {
        return Lists.newArrayList(permission);
    }

    public Permission getPermission() {
        return permission;
    }

    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    @Override
    public String getAuthority() {
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((permission == null) ? 0 : permission.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PermissionAuthority other = (PermissionAuthority) obj;
        if (permission == null) {
            if (other.permission != null) {
                return false;
            }
        } else if (!permission.equals(other.permission)) {
            return false;
        }
        return true;
    }

}
