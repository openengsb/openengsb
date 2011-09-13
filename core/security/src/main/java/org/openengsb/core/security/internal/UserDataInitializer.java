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

import org.openengsb.core.api.security.RoleManager;
import org.openengsb.core.api.security.UserManager;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.Users;
import org.openengsb.core.security.model.AllPermission;

/**
 * if the user-database is empty, default-users are inserted
 */
public class UserDataInitializer implements Runnable {
    @Override
    public void run() {
        UserManager userManager = OpenEngSBCoreServices.getServiceUtilsService().getService(UserManager.class);
        RoleManager roleManager = OpenEngSBCoreServices.getServiceUtilsService().getService(RoleManager.class);
        if (!userManager.getUsernameList().isEmpty()) {
            return;
        }
        roleManager.createRole("ROLE_ADMIN", new AllPermission());
        roleManager.createRole("ROLE_USER");
        userManager.createUser(Users.create("admin", "password"));

        roleManager.addRoleToUser("admin", "ROLE_ADMIN");
        roleManager.addRoleToUser("admin", "ROLE_USER");

        userManager.createUser(Users.create("user", "password"));
        roleManager.addRoleToUser("user", "ROLE_USER");

    }
}
