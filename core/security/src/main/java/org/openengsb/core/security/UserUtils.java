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
package org.openengsb.core.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openengsb.core.common.util.Users;
import org.openengsb.core.security.model.AbstractPermission;
import org.openengsb.core.security.model.PermissionAuthority;
import org.openengsb.core.security.model.RoleAuthority;
import org.openengsb.core.security.model.RoleImpl;
import org.openengsb.core.security.model.UserImpl;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

public final class UserUtils {

    public static UserDetails toSpringUser(UserImpl user) {
        if (user.getRoles() == null) {
            return Users.create(user.getUsername(), user.getPassword());
        }
        List<GrantedAuthority> authorities =
            new ArrayList<GrantedAuthority>(user.getRoles().size() + user.getPermissions().size());
        authorities.addAll(convertRolesToAuthorities(user.getRoles()));
        authorities.addAll(convertPermissionsToAuthorities(user.getPermissions()));
        return Users.create(user.getUsername(), user.getPassword(), new ArrayList<GrantedAuthority>(authorities));
    }

    public static UserImpl toSimpleUser(UserDetails user) {
        UserImpl simpleUser = new UserImpl(user.getUsername(), user.getPassword());
        simpleUser.setRoles(getRolesFromSpringUser(user.getAuthorities()));
        simpleUser.setPermissions(getPermissionsFromSpringUser(user.getAuthorities()));
        return simpleUser;
    }

    public static Collection<AbstractPermission> getPermissionsFromSpringUser(
            Collection<GrantedAuthority> authorities) {
        Collection<PermissionAuthority> permissionAuthorities =
            filterCollectionByType(authorities, PermissionAuthority.class);
        Collection<AbstractPermission> permissions =
            Collections2.transform(permissionAuthorities, new Function<PermissionAuthority, AbstractPermission>() {
                @Override
                public AbstractPermission apply(PermissionAuthority input) {
                    return (AbstractPermission) input.getPermission();
                }
            });
        return permissions;
    }

    public static Collection<RoleImpl> getRolesFromSpringUser(Collection<GrantedAuthority> authorities) {
        Collection<RoleAuthority> roleAuthorities = filterCollectionByType(authorities, RoleAuthority.class);
        Collection<RoleImpl> roles = Collections2.transform(roleAuthorities, new Function<RoleAuthority, RoleImpl>() {
            @Override
            public RoleImpl apply(RoleAuthority input) {
                return (RoleImpl) input.getRole();
            }
        });
        return roles;
    }

    private static Collection<GrantedAuthority> convertRolesToAuthorities(Collection<RoleImpl> authorities) {
        if (authorities == null) {
            return null;
        }
        return Collections2.transform(authorities, new Function<RoleImpl, GrantedAuthority>() {
            @Override
            public GrantedAuthority apply(RoleImpl input) {
                return new RoleAuthority(input);
            };
        });
    }

    private static Collection<GrantedAuthority> convertPermissionsToAuthorities(
            Collection<AbstractPermission> authorities) {
        if (authorities == null) {
            return null;
        }
        return Collections2.transform(authorities, new Function<AbstractPermission, GrantedAuthority>() {
            @Override
            public GrantedAuthority apply(AbstractPermission input) {
                return new PermissionAuthority(input);
            };
        });
    }

    @SuppressWarnings("unchecked")
    public static <T> Collection<T> filterCollectionByType(Collection<? super T> input, Class<T> clazz) {
        Collection<? super T> filteredAuthorities = Collections2.filter(input, Predicates.instanceOf(clazz));
        return (Collection<T>) filteredAuthorities;
    }

    private UserUtils() {
    }

}
