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
package org.openengsb.connector.userprojects.file;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.openengsb.connector.userprojects.file.internal.Configuration;
import org.openengsb.connector.userprojects.file.internal.file.UserFileAccessObject;
import org.openengsb.domain.userprojects.model.User;

@RunWith(MockitoJUnitRunner.class)
public class UserFileAccessObjectTest {

    private static final String RESOURCES_DIR = "src/test/resources";
    private static final File USERS_FILE = new File(RESOURCES_DIR, "users");
    
    private List<User> users = new ArrayList<>();

    @Before
    public void setup() {
        Configuration.get().setUsersFile(USERS_FILE);
        setupUsers();
    }

    private void setupUsers() {
        User user = new User("user1");
        users.add(user);
        
        user = new User("user2");
        users.add(user);
    }

    @Test
    public void testFindAllUsers_shouldFindNonBlankUsers() {
        UserFileAccessObject fao = new UserFileAccessObject();
        assertEquals(users, fao.findAllUsers());
    }
}
