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

import static junit.framework.Assert.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.User;
import org.openengsb.core.persistence.internal.DefaultPersistenceManager;
import org.openengsb.core.security.internal.UserManagerImpl;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class UserManagerImplTest {
    private UserManagerImpl userManager;

    @Before
    public void setUp() {
        userManager = new UserManagerImpl();
        DefaultPersistenceManager persistManager = new DefaultPersistenceManager();
        persistManager.setPersistenceRootDir("target/" + UUID.randomUUID().toString());
        userManager.setPersistenceManager(persistManager);
        BundleContext bundleContextMock = mock(BundleContext.class);
        Bundle bundleMock = mock(Bundle.class);
        when(bundleContextMock.getBundle()).thenReturn(bundleMock);
        when(bundleMock.getSymbolicName()).thenReturn(UUID.randomUUID().toString());
        userManager.setBundleContext(bundleContextMock);
        userManager.init();
    }

    @Test
    public void testToCreateUser_ShouldWork() throws Exception {
        User user = new User("testUser1", "testPass");
        userManager.createUser(user);
    }

    @Test(expected = UserExistsException.class)
    public void testToCreateUserWhichAlreadyExists_shouldNotWork() throws Exception {
        User user = new User("testUser2", "testPass");
        userManager.createUser(user);
        userManager.createUser(user);
    }

    @Test
    public void testToLoadAnExistingUser_ShouldWork() {
        userManager.createUser(new User("testUser2"));
        User user = userManager.loadUserByUsername("testUser2");
        assertThat(user.getUsername(), is("testUser2"));
    }

    @Test(expected = UserNotFoundException.class)
    public void testToLoadAnNotExistingUser_ShouldThrowUserNotFoundException() throws Exception {
        userManager.loadUserByUsername("testUser1");
    }

    @Test
    public void updateUser_ShouldWork() throws Exception {
        User userOld = new User("testUser2");
        userManager.createUser(userOld);
        User userNew = new User("testUser2", "testPassNew");
        userManager.updateUser(userNew);
        User loadUserByUsername = userManager.loadUserByUsername("testUser2");
        assertEquals("testPassNew", loadUserByUsername.getPassword());
    }

    @Test(expected = UserNotFoundException.class)
    public void updateNotExistingUser_ShouldThrowUserNotFoundException() throws Exception {
        User userNew = new User("testUser1", "testPassNew");
        userManager.updateUser(userNew);
    }

    @Test
    public void deleteUser_ShouldWork() throws Exception {
        userManager.createUser(new User("testUser3"));
        userManager.deleteUser("testUser3");
        try {
            userManager.loadUserByUsername("testUser3");
        } catch (UserNotFoundException e) {
            return;
        }
        fail();
    }

    @Test(expected = UserNotFoundException.class)
    public void deleteNotExistingUser_ShouldThrowUserNotFoundException() throws Exception {
        userManager.deleteUser("testUser1");
    }

    @Test
    public void testInitMethodCreateNewUserIfNoUserIsPresent() throws Exception {
        userManager.init();
    }

    @Test
    public void testGetAllUser_ShouldContain5Users() {
        User testUser1 = new User("testUser1", "testPass");
        User testUser2 = new User("testUser2", "testPass");
        User testUser3 = new User("testUser3", "testPass");
        userManager.createUser(testUser1);
        userManager.createUser(testUser2);
        userManager.createUser(testUser3);
        List<User> allUser = userManager.getAllUser();
        assertEquals(5, allUser.size());
    }

}
