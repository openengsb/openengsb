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
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openengsb.core.api.security.UserManager;
import org.openengsb.core.security.internal.GroupManagerImpl;
import org.openengsb.core.security.internal.UserManagerImpl;
import org.openengsb.core.security.model.Permission;
import org.openengsb.core.security.model.PermissionAuthority;
import org.openengsb.core.security.model.Role;
import org.openengsb.core.security.model.ServicePermission;
import org.openengsb.core.security.model.SimpleUser;
import org.openengsb.core.test.AbstractOpenEngSBTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.provisioning.GroupManager;

public class GroupManagerImplIT extends AbstractOpenEngSBTest {

    private final class WrapInTransactionHandler implements InvocationHandler {
        private final EntityManager entityManger;
        private final Object original;

        private WrapInTransactionHandler(EntityManager entityManager, Object original) {
            this.entityManger = entityManager;
            this.original = original;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            entityManger.getTransaction().begin();
            Object result;
            try {
                result = method.invoke(original, args);
            } catch (InvocationTargetException e) {
                entityManager.getTransaction().rollback();
                throw e.getCause();
            }
            entityManager.getTransaction().commit();
            return result;
        }
    }

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private EntityManager entityManager;

    private UserManager userManager;
    private GroupManager groupManager;

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

    private void setupPersistence() {
        Map<String, String> props = new HashMap<String, String>();
        props.put("openjpa.ConnectionURL", "jdbc:h2:" + tmpFolder.getRoot().getAbsolutePath() + "/TEST");

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("security-test", props);
        final EntityManager entityManager = emf.createEntityManager();
        this.entityManager = entityManager;
    }

    private void setupUserManager() {
        final UserManagerImpl userManager = new UserManagerImpl();
        userManager.setEntityManager(entityManager);
        this.userManager = createWrapInTransactionProxy(userManager, UserManager.class);

        GroupManagerImpl groupManager = new GroupManagerImpl();
        groupManager.setEntityManager(entityManager);
        this.groupManager = createWrapInTransactionProxy(groupManager, GroupManager.class);
    }

    @SuppressWarnings("unchecked")
    private <T> T createWrapInTransactionProxy(T original, Class<T> interfaze) {
        InvocationHandler invocationHandler = new WrapInTransactionHandler(entityManager, original);
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{ interfaze },
            invocationHandler);
    }

    @Test
    public void testListAllGroups_shouldContainTest() throws Exception {
        persist(new Role("test"));
        List<String> findAllGroups = groupManager.findAllGroups();
        assertThat(findAllGroups, hasItem("test"));
    }

    @Test
    public void testCreateGroup_shouldShowUpInGroupList() throws Exception {
        groupManager.createGroup("test", new ArrayList<GrantedAuthority>());
        List<String> findAllGroups = groupManager.findAllGroups();
        assertThat(findAllGroups, hasItem("test"));
    }

    @Test
    public void createGroupWithAuthorities_shouldContainAuthorities() throws Exception {
        Permission permission = new ServicePermission("asdf");
        GrantedAuthority permissionAuthority = new PermissionAuthority(permission);
        groupManager.createGroup("test", Arrays.asList(permissionAuthority));
        Role role = entityManager.find(Role.class, "test");
        assertThat(role.getPermissions(), hasItem(permission));
    }

    protected void persist(Object o) {
        entityManager.getTransaction().begin();
        entityManager.persist(o);
        entityManager.getTransaction().commit();
    }

}
