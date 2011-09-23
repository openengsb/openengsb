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

import org.openengsb.connector.serviceacl.ServicePermission;
import org.openengsb.connector.wicketacl.WicketPermission;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserExistsException;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.openengsb.core.security.internal.model.RootPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * if the user-database is empty, default-users are inserted
 */
public class UserDataInitializer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataInitializer.class);

    @Override
    public void run() {
        UserDataManager userManager = OpenEngSBCoreServices.getServiceUtilsService().getService(UserDataManager.class);
        if (!userManager.getUserList().isEmpty()) {
            return;
        }
        try {
            userManager.createUser("admin");
            userManager.createUser("user");
        } catch (UserExistsException e) {
            LOGGER.error("this should not happen... I just checked whether the userbase is empty", e);
            return;
        }
        try {
            userManager.setUserCredentials("admin", "password", "password");
            userManager.setUserCredentials("user", "password", "password");

            userManager.createPermissionSet("ROLE_ROOT", new RootPermission());
            userManager.storeUserPermissionSet("admin", "ROLE_ROOT");

            userManager.createPermissionSet("ROLE_USER");
            userManager.storeUserPermissionSet("user", "ROLE_USER");

            userManager.addPermissionToSet("ROLE_USER", new ServicePermission("domain.example",
                "something"));
            userManager.addPermissionToSet("ROLE_USER", new WicketPermission("SERVICE_USER"));

            userManager.storeUserPermission("user", new WicketPermission("SERVICE_EDITOR"));
        } catch (UserNotFoundException e) {
            LOGGER.error("this should not happen... I just created the user", e);
        }
    }
}
