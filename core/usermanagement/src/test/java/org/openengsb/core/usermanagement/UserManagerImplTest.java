/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.core.usermanagement;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceManager;
import org.openengsb.core.common.persistence.PersistenceService;
import org.openengsb.core.usermanagement.exceptions.UserExistsException;
import org.openengsb.core.usermanagement.exceptions.UserNotFoundException;
import org.openengsb.core.usermanagement.model.User;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class UserManagerImplTest {
    private UserManagerImpl userManager;
    private PersistenceService persistMock;

    @Before
    public void setUp() {
        userManager = new UserManagerImpl();
        persistMock = mock(PersistenceService.class);
        PersistenceManager persistManagerMock = mock(PersistenceManager.class);
        when(persistManagerMock.getPersistenceForBundle(any(Bundle.class))).thenReturn(persistMock);
        userManager.setPersistenceManager(persistManagerMock);
        BundleContext bundleMock = mock(BundleContext.class);
        userManager.setBundleContext(bundleMock);
        userManager.init();
        createTestUsers();
    }

    private void createTestUsers() {
        User testUser1 = new User("testUser1", "testPass");
        List<User> userList = new ArrayList<User>();
        when(persistMock.query(testUser1)).thenReturn(userList);

        User testUser2 = new User("testUser2", "testPass");
        List<User> userList2 = new ArrayList<User>();
        userList2.add(testUser2);
        when(persistMock.query(testUser2)).thenReturn(userList2);

        User testUser3 = new User("testUser3", "testPass");
        List<User> userList3 = new ArrayList<User>();
        userList3.add(testUser3);
        when(persistMock.query(testUser3)).thenReturn(userList3);
    }

    @Test
    public void testToCreateUser_ShouldWork() throws PersistenceException {
        User user = new User("testUser1", "testPass");
        userManager.createUser(user);
        verify(persistMock, times(1)).create(user);
    }

    @Test(expected = UserExistsException.class)
    public void testToCreateUserWhichAlreadyExists_shouldNotWork() throws PersistenceException {
        User user = new User("testUser2", "testPass");
        userManager.createUser(user);
    }

    @Test
    public void testToLoadAnExistingUser_ShouldWork() {
        User user = userManager.loadUserByUsername("testUser2");
        assertThat(user.getUsername(), is("testUser2"));
    }

    @Test(expected = UserNotFoundException.class)
    public void testToLoadAnNotExistingUser_ShouldThrowUserNotFoundException() {
        userManager.loadUserByUsername("testUser1");
    }

    @Test
    public void updateUser_ShouldWork() throws PersistenceException {
        User userOld = new User("testUser2", "testPass");
        User userNew = new User("testUser2", "testPassNew");
        userManager.updateUser(userOld, userNew);
        verify(persistMock, times(1)).update(userOld, userNew);
    }

    @Test(expected = UserNotFoundException.class)
    public void updateNotExistingUser_ShouldThrowUserNotFoundException() throws PersistenceException {
        User userOld = new User("testUser1", "testPass");
        User userNew = new User("testUser1", "testPassNew");
        userManager.updateUser(userOld, userNew);
        verify(persistMock, times(0)).update(userOld, userNew);
    }

    @Test
    public void deleteUser_ShouldWork() throws PersistenceException {
        userManager.deleteUser("testUser3");
        verify(persistMock, times(1)).delete(new User("testUser3", null));
    }

    @Test(expected = UserNotFoundException.class)
    public void deleteNotExistingUser_ShouldThrowUserNotFoundException() throws PersistenceException {
        userManager.deleteUser("testUser1");
        verify(persistMock, times(0)).delete(new User("testUser1", null));
    }

    @Test
    public void testInitMethodCreateNewUserIfNoUserIsPresent() throws PersistenceException {
        UserManagerImpl userManager = new UserManagerImpl();
        persistMock = mock(PersistenceService.class);
        PersistenceManager persistManagerMock = mock(PersistenceManager.class);
        when(persistManagerMock.getPersistenceForBundle(any(Bundle.class))).thenReturn(persistMock);
        userManager.setPersistenceManager(persistManagerMock);
        BundleContext bundleMock = mock(BundleContext.class);
        userManager.setBundleContext(bundleMock);

        User testUser1 = new User(null, null);
        when(persistMock.query(testUser1)).thenReturn(new ArrayList<User>());

        userManager.init();
        verify(persistMock, times(1)).query(new User(null, null));
        verify(persistMock, times(1)).create(new User("admin", "password"));

    }
}
