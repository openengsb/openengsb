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
package org.openengsb.core.security;

import org.apache.commons.lang.ObjectUtils;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.model.Authentication;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.domain.authentication.AuthenticationDomain;
import org.openengsb.domain.authentication.AuthenticationException;

import com.google.common.base.Preconditions;

public class UsernamePasswordAuthenticator extends AbstractOpenEngSBConnectorService implements AuthenticationDomain {

    private UserDataManager userManager;

    @Override
    public Authentication authenticate(String username, Object credentials) throws AuthenticationException {
        Preconditions.checkArgument(credentials instanceof String);
        Object actual = userManager.getUserCredentials(username, "password");
        if (ObjectUtils.notEqual(credentials, actual)) {
            throw new AuthenticationException("Bad credentials");
        }
        return new Authentication(username);
    }

    @Override
    public boolean supports(Object request) {
        return request.getClass().equals(String.class);
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    public void setUserManager(UserDataManager userManager) {
        this.userManager = userManager;
    }

}
