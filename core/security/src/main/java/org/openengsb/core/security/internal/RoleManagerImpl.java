package org.openengsb.core.security.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.openengsb.core.api.security.RoleManager;
import org.openengsb.core.api.security.UserManagementException;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.model.Role;
import org.openengsb.core.security.model.AbstractPermission;
import org.openengsb.core.security.model.RoleAuthority;
import org.openengsb.core.security.model.RoleImpl;
import org.openengsb.core.security.model.SimpleUser;
import org.springframework.security.core.GrantedAuthority;

public class RoleManagerImpl implements RoleManager {

    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    @Override
    public List<Role> findAllRoles() {
        Query query = entityManager.createNamedQuery("listAllRoles");
        return query.getResultList();
    }

    @Override
    public List<Role> findAllGlobalRoles() {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public List<String> findAllUsersWithRole(String roleName) {
        TypedQuery<String> query = entityManager.createNamedQuery("listUsersWithRole", String.class);
        query.setParameter("groupname", roleName);
        return query.getResultList();
    }

    @Override
    public void createRole(String name, Permission... permissions) {
        RoleImpl role = new RoleImpl(name);
        Collection<AbstractPermission> convertedPermissions = convertPermissions(permissions);
        role.setPermissions(convertedPermissions);
        entityManager.persist(role);
    }

    @Override
    public void deleteRole(String name) {
        RoleImpl role = entityManager.find(RoleImpl.class, name);
        if (role == null) {
            throw new UserManagementException("group with name " + name + " not found");
        }
        entityManager.remove(role);
    }

    @Override
    public void addRoleToUser(String username, String rolename) {
        SimpleUser user = entityManager.find(SimpleUser.class, username);
        RoleImpl role = entityManager.find(RoleImpl.class, rolename);
        user.addRole(role);
        entityManager.merge(user);
    }

    @Override
    public void removeRoleFromuser(String username, String rolename) {
        // TODO Auto-generated method stub

    }

    @Override
    public List<Permission> getAllPermissions(String rolename) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void addPermissionsToRole(String rolename, Permission... permission) {
        RoleImpl r = entityManager.find(RoleImpl.class, rolename);
        for (Permission p : permission) {
            r.getPermissions().add((AbstractPermission) p);
        }
    }

    @Override
    public void addPermissionToUser(String username, Permission... permission) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePermissionsFromRole(String rolename, Permission... permissions) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePermissionsFromUser(String username, Permission... permissions) {
        // TODO Auto-generated method stub

    }

    @Override
    public GrantedAuthority createRoleAuthority(String rolename) {
        Role r = entityManager.find(RoleImpl.class, rolename);
        return new RoleAuthority(r);
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    private Collection<AbstractPermission> convertPermissions(Permission[] permissions) {
        HashSet<AbstractPermission> result = new HashSet<AbstractPermission>();
        if (permissions == null) {
            return result;
        }
        for (Permission p : permissions) {
            result.add((AbstractPermission) p);
        }
        return result;
    }
}
