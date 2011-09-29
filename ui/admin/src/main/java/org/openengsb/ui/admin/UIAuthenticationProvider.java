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
package org.openengsb.ui.admin;

import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.common.util.SpringSecurityContext;
import org.openengsb.domain.authentication.AuthenticationDomain;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

public class UIAuthenticationProvider implements AuthenticationProvider {

    private AuthenticationDomain authenticator;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            org.openengsb.core.api.security.model.Authentication authenticate =
                authenticator.authenticate((String) authentication.getPrincipal(),
                    new Password((String) authentication.getCredentials()));
            Authentication wrapToken = SpringSecurityContext.wrapToken(authenticate);
            SecurityContextHolder.getContext().setAuthentication(wrapToken);
            return wrapToken;
        } catch (org.openengsb.domain.authentication.AuthenticationException e) {
            throw new BadCredentialsException("could not authenticate", e);
        }
    }

    @Override
    public boolean supports(Class<? extends Object> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setAuthenticator(AuthenticationDomain authenticator) {
        this.authenticator = authenticator;
    }

}
