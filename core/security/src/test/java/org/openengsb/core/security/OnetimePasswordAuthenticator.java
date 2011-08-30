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
import org.apache.commons.lang.math.NumberRange;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.model.Authentication;
import org.openengsb.core.common.AbstractOpenEngSBService;
import org.openengsb.domain.authentication.AuthenticationDomain;
import org.openengsb.domain.authentication.AuthenticationException;

public class OnetimePasswordAuthenticator extends AbstractOpenEngSBService implements AuthenticationDomain {

    private static final Integer MAXCODE = 1000000;

    private UserDataManager userManager;

    @Override
    public Authentication authenticate(String username, Object credentials) throws AuthenticationException {
        Integer code = (Integer) credentials;
        Integer baseCode = (Integer) userManager.getUserCredentials(username, "basecode");
        Integer counter = (Integer) userManager.getUserCredentials(username, "counter");
        Integer expectedCode = (baseCode * counter) % MAXCODE;
        if (ObjectUtils.notEqual(code, expectedCode)) {
            throw new AuthenticationException("wrong auth-code");
        }
        userManager.setUserCredentials(username, "counter", counter + 1);
        return new Authentication(username);
    }

    @Override
    public boolean supports(Object credentials) {
        Integer code;
        try {
            code = (Integer) credentials;
        } catch (ClassCastException e) {
            return false;
        }
        return new NumberRange(0, MAXCODE).containsInteger(code);
    }

    @Override
    public AliveState getAliveState() {
        return userManager != null ? AliveState.ONLINE : AliveState.OFFLINE;
    }

    public void setUserManager(UserDataManager userManager) {
        this.userManager = userManager;
    }

}
