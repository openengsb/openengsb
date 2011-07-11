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

import javax.sql.DataSource;

import org.openengsb.core.api.security.RoleManager;
import org.openengsb.core.api.security.UserManager;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.common.util.Users;
import org.openengsb.core.security.model.AllPermission;

public class UserDataInitializerBean {

    private UserManager userManager;
    private RoleManager roleManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }

    public void init() {
        /*
         * FIXME [OPENENGSB-1301] This is done in background because integration-tests would break otherwise. It makes
         * no difference for the runtime-system. This can hack can be removed as soon as OPENENGSB-1301 is fixed.
         */
        new Thread() {
            @Override
            public void run() {
                OpenEngSBCoreServices.getServiceUtilsService().getService(DataSource.class);
                doInit();
            }
        }.start();
    }

    public void doInit() {
        if (userManager.getUsernameList().isEmpty()) {
            roleManager.createRole("ROLE_ADMIN");
            roleManager.addPermissionsToRole("ROLE_ADMIN", (Permission) new AllPermission());
            roleManager.createRole("ROLE_USER");
            userManager.createUser(Users.create("admin", "password"));
            roleManager.addRoleToUser("admin", "ROLE_ADMIN");
            roleManager.addRoleToUser("admin", "ROLE_USER");
            userManager.createUser(Users.create("user", "password"));
            roleManager.addRoleToUser("user", "ROLE_USER");
        }
    }
}
