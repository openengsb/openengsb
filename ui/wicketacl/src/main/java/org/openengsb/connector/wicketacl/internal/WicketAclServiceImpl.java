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

import org.apache.commons.lang.ObjectUtils;
import org.openengsb.connector.wicketacl.WicketPermission;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.GenericControlledObject;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.SecurityAttributeEntry;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;

public class WicketAclServiceImpl extends AbstractOpenEngSBConnectorService implements
        AuthorizationDomain {

    private static final Logger LOGGER = LoggerFactory.getLogger(WicketAclServiceImpl.class);

    private UserDataManager userManager;

    public WicketAclServiceImpl() {
    }

    public WicketAclServiceImpl(UserDataManager userManager) {
        this.userManager = userManager;
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
        if (!(object instanceof GenericControlledObject)) {
            return Access.ABSTAINED;
        }
        GenericControlledObject actionData = (GenericControlledObject) object;
        if (actionData.getSecurityAttributes() == null) {
            return Access.ABSTAINED;
        }
        if (hasAccess(user, actionData)) {
            return Access.GRANTED;
        }

        return Access.ABSTAINED;
    }

    private boolean hasAccess(String user, GenericControlledObject actionData) {
        Collection<WicketPermission> filtered;
        try {
            filtered = getWicketPermissions(user);
        } catch (UserNotFoundException e) {
            LOGGER.warn("user not found", e);
            return false;
        }

        for (final SecurityAttributeEntry a : getRelevantSecurityAttributes(actionData)) {
            boolean allowed = Iterators.any(filtered.iterator(), new Predicate<WicketPermission>() {
                @Override
                public boolean apply(WicketPermission input) {
                    if (ObjectUtils.notEqual(a.getComponentName(), input.getComponentName())) {
                        return false;
                    }
                    if (a.getAction() == null || input.getAction() == null) {
                        return true;
                    }
                    if (input.getAction().equals("ENABLE")) {
                        return true;
                    }
                    return input.getAction().equals(a.getAction());
                }
            });
            if (allowed) {
                return true;
            }
        }
        return false;
    }

    private Collection<SecurityAttributeEntry> getRelevantSecurityAttributes(GenericControlledObject actionData) {
        Collection<SecurityAttributeEntry> allAttributes = actionData.getSecurityAttributes();
        if (actionData.getAction() == null) {
            return allAttributes;
        }
        if (actionData.getAction() == "RENDER") {
            return allAttributes;
            // Collections2.filter(allAttributes, new Predicate<SecurityAttributeEntry>() {
            // @Override
            // public boolean apply(SecurityAttributeEntry input) {
            // return !"ENABLE".equals(input.getAction());
            // }
            // });
        }
        return Collections2.filter(allAttributes, new Predicate<SecurityAttributeEntry>() {
            @Override
            public boolean apply(SecurityAttributeEntry input) {
                return !"RENDER".equals(input.getAction());
            }
        });
    }

    public Collection<WicketPermission> getWicketPermissions(String user) throws UserNotFoundException {
        return userManager.getUserPermissions(user, WicketPermission.class);
    }

    public void setUserManager(UserDataManager userManager) {
        this.userManager = userManager;
    }

}
