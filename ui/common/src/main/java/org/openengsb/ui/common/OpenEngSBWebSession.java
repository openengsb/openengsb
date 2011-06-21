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

import org.apache.wicket.Request;
import org.apache.wicket.Session;
import org.apache.wicket.authentication.AuthenticatedWebSession;
import org.apache.wicket.authorization.strategies.role.Roles;
import org.ops4j.pax.wicket.api.InjectorHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Common class of sessions for use in OpenEngSB and client applications. It enforces authentication and builds the
 * bridge to spring-security. Note: You must have an authenticationManager configured to start new sessions
 */
@SuppressWarnings("serial")
public abstract class OpenEngSBWebSession extends AuthenticatedWebSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenEngSBWebSession.class);

    public OpenEngSBWebSession(Request request) {
        super(request);
    }

    public static OpenEngSBWebSession get() {
        if (Session.get() instanceof OpenEngSBWebSession) {
            return (OpenEngSBWebSession) Session.get();
        } else {
            return null;
        }
    }

    protected abstract AuthenticationManager getAuthenticationManager();
    
    protected void ensureDependenciesNotNull() {
        if (getAuthenticationManager() == null) {
            throw new IllegalStateException("AdminSession requires an authenticationManager.");
        }
    }

    protected void injectDependencies() {
        InjectorHolder.getInjector().inject(this);
    }

    @Override
    public boolean authenticate(String username, String password) {
        boolean authenticated = false;
        try {
            Authentication authentication = getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(
                    username, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            authenticated = authentication.isAuthenticated();
        } catch (AuthenticationException e) {
            LOGGER.warn("User '{}' failed to login. Reason: ", username, e);
            authenticated = false;
        }
        return authenticated;
    }

    @Override
    public Roles getRoles() {
        Roles roles = new Roles();
        getRolesIfSignedIn(roles);
        return roles;
    }

    private void getRolesIfSignedIn(Roles roles) {
        if (isSignedIn()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            addRolesFromAuthentication(roles, authentication);
        }
    }

    private void addRolesFromAuthentication(Roles roles, Authentication authentication) {
        if (authentication == null) {
            signOut();
            return;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            roles.add(authority.getAuthority());
        }
    }

}
