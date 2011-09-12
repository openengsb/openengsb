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
package org.openengsb.domain.usermanagement;

import java.util.Collection;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.ObjectUtils;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.OpenEngSBService;
import org.openengsb.core.api.context.ContextHolder;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.core.common.util.CollectionUtils2;
import org.openengsb.domain.authorization.AuthorizationDomain;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

public class ServicePermissionAccessConnector extends AbstractOpenEngSBService implements AuthorizationDomain {

    private UserDataManager userManager;

    @Override
    public Access checkAccess(String user, final MethodInvocation action) {
        Collection<Permission> permissions;
        try {
            permissions = userManager.getUserPermissions(user, "service");
        } catch (UserNotFoundException e) {
            throw new IllegalStateException(e);
        }

        Collection<ServicePermission> permissionObjects =
            CollectionUtils2.filterCollectionByClass(permissions, ServicePermission.class);

        boolean grant = Iterators.any(permissionObjects.iterator(), new Predicate<ServicePermission>() {
            @Override
            public boolean apply(ServicePermission input) {
                if (input.getContextId() != null
                        && !input.getContextId().equals(ContextHolder.get().getCurrentContextId())) {
                    return false;
                }
                if (!(action.getThis() instanceof OpenEngSBService)) {
                    return false;
                }
                OpenEngSBService service = (OpenEngSBService) action.getThis();
                return ObjectUtils.equals(input.getServiceId(), service.getInstanceId());
            }
        });
        return grant ? Access.GRANTED : Access.ABSTAINED;
    }

    public void setUserManager(UserDataManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

}
