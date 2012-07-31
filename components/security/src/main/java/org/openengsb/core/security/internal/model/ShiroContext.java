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
package org.openengsb.core.security.internal.model;

import java.util.List;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.api.security.Credentials;
import org.openengsb.core.security.internal.OpenEngSBAuthenticationToken;

public class ShiroContext implements AuthenticationContext {

    @Override
    public void login(String username, Credentials credentials) {
        OpenEngSBAuthenticationToken token = new OpenEngSBAuthenticationToken(username, credentials);
        SecurityUtils.getSubject().login(token);
    }

    @Override
    public void logout() {
        Subject subject = ThreadContext.getSubject();
        if (subject == null) {
            return;
        }
        subject.logout();
    }

    @Override
    public Object getAuthenticatedPrincipal() {
        Subject subject = ThreadContext.getSubject();
        if (subject == null) {
            return null;
        }
        return subject.getPrincipal();
    }

    @Override
    public List<Object> getAllAuthenticatedPrincipals() {
        Subject subject = ThreadContext.getSubject();
        if (subject == null) {
            return null;
        }
        return subject.getPrincipals().asList();
    }
}
