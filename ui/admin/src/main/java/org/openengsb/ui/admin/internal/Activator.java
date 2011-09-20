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
package org.openengsb.ui.admin.internal;

import org.openengsb.connector.wicketacl.WicketPermission;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.common.OpenEngSBCoreServices;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    @Override
    public void start(BundleContext context) throws Exception {
        UserDataManager userManager = OpenEngSBCoreServices.getServiceUtilsService().getService(UserDataManager.class);
        while (userManager.getUserList().isEmpty()) {
            LOGGER.warn("wait for userdatainitializer to run");
            Thread.sleep(1000);
        }
        if (!userManager.getAllPermissionsFromSet("ROLE_USER").isEmpty()) {
            return;
        }
        userManager.addPermissionToSet("ROLE_USER", new WicketPermission("SERVICE_USER"));
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // TODO Auto-generated method stub

    }

}
