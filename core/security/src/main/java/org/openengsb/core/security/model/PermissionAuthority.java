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
        // TODO Auto-generated method stub
        return null;
    }

}
