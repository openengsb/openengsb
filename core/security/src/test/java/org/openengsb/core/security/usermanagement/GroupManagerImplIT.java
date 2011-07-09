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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

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
import org.openengsb.core.security.model.SimpleUser;
import org.springframework.security.core.userdetails.UserDetails;

public class GroupManagerImplIT extends AbstractJPATest {

    private UserManager userManager;
    private RoleManager groupManager;

    private SimpleUser testUser2;
    private SimpleUser testUser3;

    @Before
    public void setUp() throws Exception {
        setupPersistence();
        setupUserManager();
        testUser2 = new SimpleUser("testUser2", "testPass");
        entityManager.persist(testUser2);
        testUser3 = new SimpleUser("testUser3", "testPass");
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
        List findAllGroups = groupManager.findAllRoles();
        assertThat(findAllGroups, hasItem(hasProperty("name", is("test"))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCreateGroup_shouldShowUpInGroupList() throws Exception {
        groupManager.createRole("test");
        @SuppressWarnings("rawtypes")
        List findAllGroups = groupManager.findAllRoles();
        assertThat(findAllGroups, hasItem(hasProperty("name", is("test"))));
    }

    @Test
    public void testDeleteGroup_shouldNotShowUpInGroupList() throws Exception {
        groupManager.createRole("test");
        groupManager.deleteRole("test");
        List<Role> findAllGroups = groupManager.findAllRoles();
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
    public void findUsersInGroup_shouldReturnMembers() throws Exception {
        RoleImpl role = new RoleImpl("test");
        testUser2.addRole(role);
        entityManager.getTransaction().begin();
        entityManager.persist(role);
        entityManager.merge(testUser2);
        entityManager.getTransaction().commit();
        List<String> findUsersInGroup = groupManager.findAllUsersWithRole("test");
        assertThat(findUsersInGroup.size(), is(1));
        assertThat(findUsersInGroup, hasItem("testUser2"));
    }

    @Test
    public void addUserToGroup_shouldShowGroupWithUserAndViceVersa() throws Exception {
        groupManager.createRole("testrole");
        userManager.createUser(Users.create("user", "password"));
        groupManager.addRoleToUser("user", "testrole");
        List<String> usersInGroup = groupManager.findAllUsersWithRole("testrole");
        assertThat(usersInGroup, hasItem("user"));
        UserDetails user = userManager.loadUserByUsername("user");
        assertFalse(user.getAuthorities().isEmpty());
    }

    @Override
    public String getPersistenceUnitName() {
        return "security-test";
    }

}
