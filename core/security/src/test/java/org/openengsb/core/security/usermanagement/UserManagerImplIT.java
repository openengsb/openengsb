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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserManager;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.User;
import org.openengsb.core.security.internal.UserDataInitializerBean;
import org.openengsb.core.security.internal.UserManagerImpl;
import org.openengsb.core.security.model.SimpleUser;
import org.openengsb.core.test.AbstractOpenEngSBTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserManagerImplIT extends AbstractOpenEngSBTest {

    @Rule
    public TemporaryFolder tmpFolder = new TemporaryFolder();

    private EntityManager entityManager;

    private UserManager userManager;

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
            (UserManager) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{ UserManager.class },
                invocationHandler);
    }

    @Test
    public void testToCreateUser_ShouldWork() throws Exception {
        User user = new User("testUser1", "testPass");
        userManager.createUser(user);
        User loadUserByUsername = userManager.loadUserByUsername("testUser1");
        assertThat(loadUserByUsername, is(user));
    }

    @Test(expected = UserExistsException.class)
    public void testToCreateUserWhichAlreadyExists_shouldNotWork() throws Exception {
        User user = new User("testUser2", "testPass");
        userManager.createUser(user);
    }

    @Test
    public void testToLoadAnExistingUser_ShouldWork() {
        User user = userManager.loadUserByUsername("testUser2");
        assertThat(user.getUsername(), is("testUser2"));
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testToLoadAnNotExistingUser_ShouldThrowUserNotFoundException() throws Exception {
        userManager.loadUserByUsername("testUser1");
    }

    @Test
    public void updateUser_ShouldWork() throws Exception {
        User userNew = new User("testUser2", "testPassNew");
        userManager.updateUser(userNew);
        assertThat(userManager.loadUserByUsername("testUser2").getPassword(), is("testPassNew"));
    }

    @Test(expected = UserNotFoundException.class)
    public void updateNotExistingUser_ShouldThrowUserNotFoundException() throws Exception {
        User userNew = new User("testUser1", "testPassNew");
        userManager.updateUser(userNew);
    }

    @Test
    public void deleteUser_ShouldWork() throws Exception {
        userManager.deleteUser("testUser3");
        assertNull(entityManager.find(SimpleUser.class, "testUser3"));
    }

    @Test(expected = UserNotFoundException.class)
    public void deleteNotExistingUser_ShouldThrowUserNotFoundException() throws Exception {
        userManager.deleteUser("testUser1");
    }

    @Test
    public void testInitMethodCreateNewUserIfNoUserIsPresent() throws Exception {
        entityManager.getTransaction().begin();
        entityManager.createQuery("DELETE FROM SimpleUser").executeUpdate();
        entityManager.getTransaction().commit();
        UserDataInitializerBean userDataInitializerBean = new UserDataInitializerBean();
        userDataInitializerBean.setUserManager(userManager);
        userDataInitializerBean.doInit();
        UserDetails loadUserByUsername = userManager.loadUserByUsername("admin");
        assertThat(loadUserByUsername.getPassword(), is("password"));
    }

    @Test
    public void testGetAllUser_ShouldContain3Users() {
        List<User> allUser = userManager.getAllUser();
        assertThat(allUser, hasItems(testUser2.toSpringUser(), testUser3.toSpringUser()));
    }
}
