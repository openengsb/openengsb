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

package org.openengsb.core.security.internal;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
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
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.security.model.UserData;
import org.openengsb.core.test.AbstractOpenEngSBTest;
import org.openengsb.domain.authorization.AuthorizationDomain.Access;

public class UserDataManagerImplIT extends AbstractOpenEngSBTest {

    public static class TestPermission implements Permission {
        private String desiredResult;

        public TestPermission() {
        }

        public TestPermission(Access desiredResult) {
            super();
            this.desiredResult = desiredResult.name();
        }

        @Override
        public String describe() {
            return "for testing purposes";
        }

        public String getDesiredResult() {
            return desiredResult;
        }

        public void setDesiredResult(String desiredResult) {
            this.desiredResult = desiredResult;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((desiredResult == null) ? 0 : desiredResult.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TestPermission other = (TestPermission) obj;
            if (desiredResult == null) {
                if (other.desiredResult != null)
                    return false;
            } else if (!desiredResult.equals(other.desiredResult))
                return false;
            return true;
        }
    }

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private EntityManager entityManager;

    private UserDataManager userManager;

    private UserData testUser2;
    private UserData testUser3;

    @Before
    public void setUp() throws Exception {
        setupPersistence();
        setupUserManager();
        testUser2 = new UserData("testUser2");
        entityManager.persist(testUser2);
        testUser3 = new UserData("testUser3");
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
        final UserDataManagerImpl userManager = new UserDataManagerImpl();
        userManager.setEntityManager(entityManager);
        InvocationHandler invocationHandler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                entityManager.getTransaction().begin();
                Object result;
                try {
                    result = method.invoke(userManager, args);
                } catch (InvocationTargetException e) {
                    entityManager.getTransaction().rollback();
                    throw e.getCause();
                }
                entityManager.getTransaction().commit();
                return result;
            }
        };
        this.userManager =
            (UserDataManager) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class<?>[]{ UserDataManager.class }, invocationHandler);
    }

    @Test
    public void testCreateUserWithPassword_shouldHavePassword() throws Exception {
        userManager.createUser("admin2");
        userManager.setUserCredentials("admin2", "password", "testpassword");
        String userCredentials = userManager.getUserCredentials("admin2", "password");
        assertThat(userCredentials, is("testpassword"));
    }

    @Test(expected = UserNotFoundException.class)
    public void testCreateAndDeleteUser_shouldNotFindUser() throws Exception {
        userManager.createUser("admin2");
        userManager.setUserCredentials("admin2", "password", "testpassword");
        userManager.deleteUser("admin2");
        userManager.getUserCredentials("admin2", "password");
    }

    @Test
    public void testStoreUserPermission_shouldBeStored() throws Exception {
        userManager.createUser("admin2");
        Permission permission = new TestPermission(Access.GRANTED);
        userManager.storeUserPermission("admin2", permission);
        Collection<Permission> userPermissions =
            userManager.getUserPermissions("admin2");
        assertThat(userPermissions, hasItem(equalTo(permission)));
    }

    @Test
    public void testStoreUserPermissionAndDeleteAgain_shouldBeDeleted() throws Exception {
        userManager.createUser("admin2");
        Permission permission = new TestPermission(Access.GRANTED);
        userManager.storeUserPermission("admin2", permission);
        userManager.removeUserPermission("admin2", permission);
        Collection<Permission> userPermissions =
            userManager.getUserPermissions("admin2");
        assertThat(userPermissions, not(hasItem(equalTo(permission))));
    }

    @Test
    public void testStoreUserPermissionSet_shouldBeStored() throws Exception {
        userManager.createUser("admin2");
        Permission permission = new TestPermission(Access.GRANTED);
        userManager.createPermissionSet("ROLE_ADMIN", permission);
        userManager.storeUserPermissionSet("admin2", "ROLE_ADMIN");
        Collection<String> userPermissions = userManager.getUserPermissionSets("admin2");
        assertThat(userPermissions, hasItem("ROLE_ADMIN"));
    }

    @Test
    public void testStoreUserPermissionSet_shouldGrantAllPermissions() throws Exception {
        userManager.createUser("admin2");
        Permission permission = new TestPermission(Access.GRANTED);
        userManager.createPermissionSet("ROLE_ADMIN", permission);
        userManager.storeUserPermissionSet("admin2", "ROLE_ADMIN");
        Collection<Permission> allUserPermissions = userManager.getAllUserPermissions("admin2");
        assertThat(allUserPermissions, hasItem(permission));
    }

    @Test
    public void addPermissionSetToSet_shouldBeListedAsMember() throws Exception {
        userManager.createPermissionSet("ROLE_ADMIN");
        userManager.createPermissionSet("ROLE_ROOT");
        userManager.addSetToPermissionSet("ROLE_ROOT", "ROLE_ADMIN");
        Collection<String> memberPermissionSets = userManager.getMemberPermissionSets("ROLE_ROOT");
        assertThat(memberPermissionSets, hasItem("ROLE_ADMIN"));
    }

    @Test
    public void addPermissionSetToSet_shouldGrantAllPermissions() throws Exception {
        userManager.createUser("admin2");
        Permission permission = new TestPermission(Access.GRANTED);
        userManager.createPermissionSet("ROLE_PROJECTMEMBER", permission);
        userManager.createPermissionSet("ROLE_MANAGER");
        userManager.addSetToPermissionSet("ROLE_MANAGER", "ROLE_PROJECTMEMBER");
        userManager.storeUserPermissionSet("admin2", "ROLE_MANAGER");
        Collection<Permission> allUserPermissions = userManager.getAllUserPermissions("admin2");
        assertThat(allUserPermissions, hasItem(permission));
    }

    @Test
    public void addSingleUserAttribute_shouldContainAttribute() throws Exception {
        userManager.createUser("admin1");
        userManager.createUser("admin2");
        userManager.setUserAttribute("admin1", "test", 42);
        userManager.setUserAttribute("admin2", "test", 21);

        List<Object> admin1Attribute = userManager.getUserAttribute("admin1", "test");
        assertAttributeValue(admin1Attribute, 42);

        List<Object> admin2Attribute = userManager.getUserAttribute("admin2", "test");
        assertAttributeValue(admin2Attribute, 21);
    }

    @Test
    public void addAndRemoveUserAttribute_shouldNotBeSetAnymore() throws Exception {
        userManager.createUser("admin1");
        userManager.setUserAttribute("admin1", "test", 42);
        userManager.removeUserAttribute("admin1", "test");
        assertThat(userManager.getUserAttribute("admin1", "test"), nullValue());
    }

    private void assertAttributeValue(List<Object> actual, Object... expected) {
        assertThat(actual, is(Arrays.asList(expected)));
    }
}