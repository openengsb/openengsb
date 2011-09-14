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

package org.openengsb.connector.wicketacl.internal;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.openengsb.connector.wicketacl.WicketPermission;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.security.SecurityAttribute;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.Permission;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.core.common.util.CollectionUtils2;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;

public class WicketAclServiceImpl extends AbstractOpenEngSBConnectorService implements
        AuthorizationDomain {

    private static final Logger LOGGER = LoggerFactory.getLogger(WicketAclServiceImpl.class);

    private UserDataManager userManager;

    public WicketAclServiceImpl() {
    }

    @Override
    public AliveState getAliveState() {
        if (userManager == null) {
            return AliveState.OFFLINE;
        }
        return AliveState.ONLINE;
    }

    @Override
    public Access checkAccess(String user, Object object) {
        if (!(object instanceof Map)) {
            return Access.ABSTAINED;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> actionData = (Map<String, Object>) object;
        SecurityAttribute[] securityAttributes = (SecurityAttribute[]) actionData.get("securityAnnotations");

        if (hasAccess(user, securityAttributes)) {
            return Access.GRANTED;
        }

        return Access.ABSTAINED;
    }

    private boolean hasAccess(String user, SecurityAttribute[] securityAttributes) {
        if (securityAttributes == null) {
            return false;
        }
        Collection<WicketPermission> filtered;
        try {
            filtered = getWicketPermissions(user);
        } catch (UserNotFoundException e) {
            LOGGER.warn("user not found", e);
            return false;
        }
        for (final SecurityAttribute a : securityAttributes) {
            boolean allowed = Iterators.any(filtered.iterator(), new Predicate<WicketPermission>() {
                @Override
                public boolean apply(WicketPermission input) {
                    if (ObjectUtils.notEqual(a.value(), input.getComponentName())) {
                        return false;
                    }
                    if (a.action() == null || input.getAction() == null) {
                        return true;
                    }
                    if (input.getAction().equals("ENABLE")) {
                        return true;
                    }
                    return input.getAction().equals(a.action());
                }
            });
            if (allowed) {
                return true;
            }
        }
        return false;
    }

    public Collection<WicketPermission> getWicketPermissions(String user) throws UserNotFoundException {
        Collection<Permission> userPermissions =
            userManager.getUserPermissions(user, WicketPermission.class.getName());
        Collection<WicketPermission> filtered =
            CollectionUtils2.filterCollectionByClass(userPermissions, WicketPermission.class);
        return filtered;
    }

    public void setUserManager(UserDataManager userManager) {
        this.userManager = userManager;
    }

}
