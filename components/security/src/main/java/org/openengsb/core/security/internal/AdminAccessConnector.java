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

import java.util.Collection;

import java.util.List;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.api.security.service.UserDataManager;
import org.openengsb.core.api.security.service.UserNotFoundException;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.security.internal.model.RootPermission;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

/**
 * Connector that checks if the user has {@link RootPermission}
 *
 * If so, it always returns {@link Access#GRANTED}.
 */
public class AdminAccessConnector extends AbstractOpenEngSBService implements AuthorizationDomain {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminAccessConnector.class);

    private UserDataManager userManager;

    @Override
    public Access checkAccess(String username, Object action) {
        try {
            Collection<Permission> userPermissions = userManager.getAllPermissionsForUser(username);
            boolean allowed = Iterators.any(userPermissions.iterator(), new Predicate<Permission>() {
                @Override
                public boolean apply(Permission input) {
                    return input.getClass().equals(RootPermission.class);
                }
            });
            if (allowed) {
                return Access.GRANTED;
            }
        } catch (UserNotFoundException e) {
            LOGGER.warn("user for access control decision was not found", e);
            // just let it abstain
        }
        return Access.ABSTAINED;
    }

    public void setUserManager(UserDataManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    @Override
    public void openXLinks(List<OpenEngSBModel> modelObjects, String viewId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


}
