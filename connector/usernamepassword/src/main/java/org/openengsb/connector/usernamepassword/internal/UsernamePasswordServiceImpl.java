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

package org.openengsb.connector.usernamepassword.internal;

import org.apache.commons.lang.ObjectUtils;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.Authentication;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.core.common.SpringSecurityContext;
import org.openengsb.domain.authentication.AuthenticationDomain;
import org.openengsb.domain.authentication.AuthenticationException;

public class UsernamePasswordServiceImpl extends AbstractOpenEngSBConnectorService implements
        AuthenticationDomain {

    private UserDataManager userManager;

    public UsernamePasswordServiceImpl() {
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    @Override
    public Authentication authenticate(String username, Object credentials) throws AuthenticationException {
        String actualPassword;
        try {
            actualPassword = userManager.getUserCredentials(username, "password");
        } catch (UserNotFoundException e) {
            throw new AuthenticationException(e);
        }
        if (ObjectUtils.notEqual(credentials, actualPassword)) {
            throw new AuthenticationException("wrong password");
        }
        Authentication authentication = new Authentication(username);
        SpringSecurityContext.getInstance().setAuthentication(authentication);
        return authentication;

    }

    @Override
    public boolean supports(Object credentials) {
        return credentials instanceof String;
    }

    public void setUserManager(UserDataManager userManager) {
        this.userManager = userManager;
    }

}
