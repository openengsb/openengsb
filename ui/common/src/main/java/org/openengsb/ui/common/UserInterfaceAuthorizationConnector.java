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
package org.openengsb.ui.common;

import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.security.SecurityAttribute;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.core.common.util.CollectionUtils2;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.openengsb.ui.common.model.UiPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

public class UserInterfaceAuthorizationConnector extends AbstractOpenEngSBConnectorService implements
        AuthorizationDomain {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserInterfaceAuthorizationConnector.class);

    private UserDataManager userManager;

    @Override
    public Access checkAccess(String user, Object action) {
        if (action instanceof Class) {
            Class<?> componentClass = (Class<?>) action;
            return checkComponentInitializeAccess(user, componentClass);
        }
        return Access.ABSTAINED;
    }

    private Access checkComponentInitializeAccess(String user, Class<?> componentClass) {
        SecurityAttribute annotation = componentClass.getAnnotation(SecurityAttribute.class);
        if (annotation == null) {
            return Access.GRANTED;
        }
        final String securityAttribute = annotation.value();
        try {
            Collection<Permission> userPermissions =
                userManager.getUserPermissions(user, UiPermission.class.getName());
            Collection<UiPermission> uiPermissions =
                CollectionUtils2.filterCollectionByClass(userPermissions, UiPermission.class);
            boolean hasPermission = Iterators.any(uiPermissions.iterator(), new Predicate<UiPermission>() {
                @Override
                public boolean apply(UiPermission input) {
                    return ObjectUtils.equals(securityAttribute, input.getSecurityAttribute());
                }
            });
            if (hasPermission) {
                return Access.GRANTED;
            }
        } catch (UserNotFoundException e) {
            LOGGER.warn("user for authorization-descion not found", e);
            return Access.ABSTAINED;
        }
        return Access.ABSTAINED;
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    public void setUserManager(UserDataManager userManager) {
        this.userManager = userManager;
    }

}
