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

package org.openengsb.core.security.usermanagement;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.security.RoleManager;
import org.openengsb.core.api.security.UserManager;
import org.openengsb.core.api.security.model.Role;
import org.openengsb.core.common.util.Users;
import org.openengsb.core.security.internal.RoleManagerImpl;
import org.openengsb.core.security.internal.UserManagerImpl;
import org.openengsb.core.security.model.AbstractPermission;
import org.openengsb.core.security.model.RoleImpl;
import org.openengsb.core.security.model.ServicePermission;
import org.openengsb.core.security.model.UserImpl;
import org.springframework.security.core.userdetails.UserDetails;

public class GroupManagerImplIT extends AbstractJPATest {

    private UserManager userManager;
    private RoleManager groupManager;

    private UserImpl testUser2;
    private UserImpl testUser3;

    @Before
    public void setUp() throws Exception {
        setupPersistence();
        setupUserManager();
        testUser2 = new UserImpl("testUser2", "testPass");
        entityManager.persist(testUser2);
        testUser3 = new UserImpl("testUser3", "testPass");
        entityManager.persist(testUser3);
    }

    private void setupUserManager() {
        final UserManagerImpl userManager = new UserManagerImpl();
        userManager.setEntityManager(entityManager);
        this.userManager = createWrapInTransactionProxy(userManager, UserManager.class);

        RoleManagerImpl groupManager = new RoleManagerImpl();
        groupManager.setEntityManager(entityManager);
        this.groupManager = createWrapInTransactionProxy(groupManager, RoleManager.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testListAllGroups_shouldContainTest() throws Exception {
        persist(new RoleImpl("test"));
        @SuppressWarnings("rawtypes")
        Iterable<Object> findAllGroups = (Iterable) groupManager.findAllRoles();
        assertThat(findAllGroups, hasItem(hasProperty("name", is("test"))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateGroup_shouldShowUpInGroupList() throws Exception {
        groupManager.createRole("test");
        @SuppressWarnings("rawtypes")
        List<Object> findAllGroups = (List) groupManager.findAllRoles();
        assertThat(findAllGroups, hasItem(hasProperty("name", is("test"))));
    }

    @Test
    public void testDeleteGroup_shouldNotShowUpInGroupList() throws Exception {
        groupManager.createRole("test");
        groupManager.deleteRole("test");
        Collection<Role> findAllGroups = groupManager.findAllRoles();
        assertTrue(findAllGroups.isEmpty());
    }

    @Test
    public void createGroupWithAuthorities_shouldContainAuthorities() throws Exception {
        AbstractPermission permission = new ServicePermission("asdf");
        groupManager.createRole("test", permission);
        RoleImpl role = entityManager.find(RoleImpl.class, "test");
        assertThat(role.getPermissions(), hasItem(permission));
    }

    @Test
    public void removePermissionFromRole_shouldNotHavePermissionAnymore() throws Exception {
        AbstractPermission permission = new ServicePermission("asdf");
        groupManager.createRole("test", permission);
        groupManager.removePermissionsFromRole("test", permission);
        RoleImpl role = entityManager.find(RoleImpl.class, "test");
        assertThat(role.getPermissions(), not(hasItem(permission)));
    }

    @Test
    public void addPermissionsToUser_shouldContainPermissions() throws Exception {
        AbstractPermission permission = new ServicePermission("asdf");
        userManager.createUser(Users.create("user", "password"));
        groupManager.addPermissionToUser("user", permission);
        UserImpl user = entityManager.find(UserImpl.class, "user");
        assertThat(user.getPermissions(), hasItem(permission));
    }

    @Test
    public void removePermissionsFromUser_shouldNotContainPermissions() throws Exception {
        AbstractPermission permission = new ServicePermission("asdf");
        userManager.createUser(Users.create("user", "password"));
        groupManager.addPermissionToUser("user", permission);
        groupManager.removePermissionsFromUser("user", permission);
        UserImpl user = entityManager.find(UserImpl.class, "user");
        assertThat(user.getPermissions(), not(hasItem(permission)));
    }

    @Test
    public void findUsersInGroup_shouldReturnMembers() throws Exception {
        RoleImpl role = new RoleImpl("test");
        testUser2.addRole(role);
        entityManager.getTransaction().begin();
        entityManager.persist(role);
        entityManager.merge(testUser2);
        entityManager.getTransaction().commit();
        Collection<String> findUsersInGroup = groupManager.findAllUsersWithRole("test");
        assertThat(findUsersInGroup.size(), is(1));
        assertThat(findUsersInGroup, hasItem("testUser2"));
    }

    @Test
    public void addUserToGroup_shouldShowGroupWithUserAndViceVersa() throws Exception {
        groupManager.createRole("testrole");
        userManager.createUser(Users.create("user", "password"));
        groupManager.addRoleToUser("user", "testrole");
        Collection<String> usersInGroup = groupManager.findAllUsersWithRole("testrole");
        assertThat(usersInGroup, hasItem("user"));
        UserDetails user = userManager.loadUserByUsername("user");
        assertFalse(user.getAuthorities().isEmpty());
    }

    @Test
    public void removeUserFromGroup_shouldNotBeShownInListAnymore() throws Exception {
        groupManager.createRole("testrole");
        userManager.createUser(Users.create("user", "password"));
        groupManager.addRoleToUser("user", "testrole");
        groupManager.removeRoleFromuser("user", "testrole");
        Collection<String> usersInGroup = groupManager.findAllUsersWithRole("testrole");
        assertTrue(usersInGroup.isEmpty());
    }

    @Override
    public String getPersistenceUnitName() {
        return "security-test";
    }

}
