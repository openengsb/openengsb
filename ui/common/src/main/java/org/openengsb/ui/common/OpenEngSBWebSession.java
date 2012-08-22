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

import org.apache.shiro.authc.AuthenticationException;
import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.Request;
import org.openengsb.connector.usernamepassword.Password;
import org.openengsb.core.api.security.AuthenticationContext;
import org.ops4j.pax.wicket.api.InjectorHolder;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common class of sessions for use in OpenEngSB and client applications. It enforces authentication and builds the
 * bridge to spring-security. Note: You must have an authenticationManager configured to start new sessions
 */
@SuppressWarnings("serial")
public class OpenEngSBWebSession extends AuthenticatedWebSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenEngSBWebSession.class);

    @PaxWicketBean(name = "authenticationContext")
    private AuthenticationContext authenticationContext;

    public OpenEngSBWebSession(Request request) {
        super(request);
        injectDependencies();
    }

    public static OpenEngSBWebSession get() {
        if (Session.get() instanceof OpenEngSBWebSession) {
            return (OpenEngSBWebSession) Session.get();
        } else {
            return null;
        }
    }

    protected void injectDependencies() {
        InjectorHolder.getInjector().inject(this, getClass());
    }

    @Override
    public boolean authenticate(String username, String password) {
        try {
            authenticationContext.login(username, new Password(password));
        } catch (AuthenticationException e) {
            LOGGER.error("Authentication failed");
            LOGGER.info("Reason: ", e);
            return false;
        }
        return true;
    }

    @Override
    public void signOut() {
        super.signOut();
        authenticationContext.logout();
    }

    @Override
    public Roles getRoles() {
        Roles roles = new Roles();
        return roles;
    }

}
