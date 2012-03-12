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

import org.apache.shiro.authc.AbstractAuthenticator;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.openengsb.core.api.security.Credentials;
import org.openengsb.core.api.security.model.Authentication;
import org.openengsb.domain.authentication.AuthenticationDomain;
/**
 * Authenticator to be used in Shiro {@link  org.apache.shiro.mgt.SecurityManager}
 */
public class OpenEngSBShiroAuthenticator extends AbstractAuthenticator {

    private AuthenticationDomain authenticator;

    @Override
    protected AuthenticationInfo doAuthenticate(AuthenticationToken token) throws AuthenticationException {
        try {
            Authentication authenticate =
                authenticator.authenticate(token.getPrincipal().toString(), (Credentials) token.getCredentials());
            return new SimpleAuthenticationInfo(authenticate.getUsername(), authenticate.getCredentials(),
                "openengsb");
        } catch (org.openengsb.domain.authentication.AuthenticationException e) {
            throw new AuthenticationException(e);
        }
    }

    public void setAuthenticator(AuthenticationDomain authenticator) {
        this.authenticator = authenticator;
    }
}
