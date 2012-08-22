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

package org.openengsb.core.services.internal.security;

import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.api.security.service.PermissionSetAlreadyExistsException;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.api.security.service.UserExistsException;
import org.openengsb.core.common.util.FilterUtils;
import org.openengsb.core.services.internal.security.model.RootPermission;
import org.osgi.framework.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * if the user-database is empty, default-users are inserted
 */
public class UserDataInitializer implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataInitializer.class);

    private OsgiUtilsService utilsService;

    public UserDataInitializer(OsgiUtilsService utilsService) {
        this.utilsService = utilsService;
    }

    @Override
    public void run() {
        Filter filter = FilterUtils.makeFilter(UserDataManager.class, "(internal=true)");
        UserDataManager userManager = (UserDataManager) utilsService.getService(filter);

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
            userManager.addPermissionSetToUser("admin", "ROLE_ROOT");
        } catch (PermissionSetAlreadyExistsException e) {
            LOGGER.error("this should not happen... I just checked whether the userbase is empty", e);
        }
    }
}
