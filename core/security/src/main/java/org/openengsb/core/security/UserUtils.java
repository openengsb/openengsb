package org.openengsb.core.security;

import java.util.ArrayList;
import java.util.Collection;

import org.openengsb.core.common.util.Users;
import org.openengsb.core.security.model.Permission;
import org.openengsb.core.security.model.PermissionAuthority;
import org.openengsb.core.security.model.Role;
import org.openengsb.core.security.model.RoleAuthority;
import org.openengsb.core.security.model.SimpleUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

public final class UserUtils {

    public static UserDetails toSpringUser(SimpleUser user) {
        if (user.getRoles() == null) {
            return Users.create(user.getUsername(), user.getPassword());
        }
        Collection<GrantedAuthority> authorities = convertToAuthorities(user.getRoles());
        return Users.create(user.getUsername(), user.getPassword(), new ArrayList<GrantedAuthority>(authorities));
    }

    public static SimpleUser toSimpleUser(UserDetails user) {
        SimpleUser simpleUser = new SimpleUser(user.getUsername(), user.getPassword());
        simpleUser.setRoles(getRolesFromSpringUser(user));
        simpleUser.setPermissions(getPermissionsFromSpringUser(user));
        return simpleUser;
    }

    public static Collection<Permission> getPermissionsFromSpringUser(UserDetails user) {
        Collection<PermissionAuthority> permissionAuthorities =
            filterCollectionByType(user.getAuthorities(), PermissionAuthority.class);
        Collection<Permission> permissions =
            Collections2.transform(permissionAuthorities, new Function<PermissionAuthority, Permission>() {
                @Override
                public Permission apply(PermissionAuthority input) {
                    return input.getPermission();
                }
            });
        return permissions;
    }

    public static Collection<Role> getRolesFromSpringUser(UserDetails user) {
        Collection<RoleAuthority> roleAuthorities =
            filterCollectionByType(user.getAuthorities(), RoleAuthority.class);
        Collection<Role> roles = Collections2.transform(roleAuthorities, new Function<RoleAuthority, Role>() {
            @Override
            public Role apply(RoleAuthority input) {
                return input.getRole();
            }
        });
        return roles;
    }

    private static Collection<GrantedAuthority> convertToAuthorities(Collection<Role> authorities) {
        if (authorities == null) {
            return null;
        }
        return Collections2.transform(authorities, new Function<Role, GrantedAuthority>() {
            @Override
            public GrantedAuthority apply(Role input) {
                return new RoleAuthority(input);
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
