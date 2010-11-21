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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openengsb.core.common.persistence.PersistenceException;
import org.openengsb.core.common.persistence.PersistenceService;
import org.openengsb.core.persistence.internal.NeodatisPersistenceService;
import org.openengsb.core.usermanagement.exceptions.UserExistsException;
import org.openengsb.core.usermanagement.model.User;
import org.springframework.security.core.GrantedAuthority;

public class UserManagerImplTest {
    private UserManagerImpl userManager;
    private PersistenceService persistMock;


    @Before
    public void setUp() {
        userManager = new UserManagerImpl();
        persistMock = mock(PersistenceService.class);
        userManager.setPersistence(persistMock);
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
    }

    @Test
    public void testToCreateUser_shouldWork() throws PersistenceException {
        User user = new User("testUser1", "testPass");
        userManager.createUser(user);
        verify(persistMock, times(1)).create(user);
    }

    @Test(expected = UserExistsException.class)
    public void testToCreateUserWhichAlreadyExists_shouldNotWork() throws PersistenceException {
        User user = new User("testUser2", "testPass");
        userManager.createUser(user);
    }

    
}
