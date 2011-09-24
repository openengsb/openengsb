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
package org.openengsb.core.common;

import org.openengsb.core.api.security.model.Authentication;
import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * This is used to wrap an OpenEngSB internal Authentication into Spring Authentication so it can be stored in springs
 * {@link org.springframework.security.core.context.SecurityContext}
 */
class OpenEngSBAuthentication extends AbstractAuthenticationToken {

    private static final long serialVersionUID = -3293065282606838713L;

    private Authentication authentication;

    public OpenEngSBAuthentication(Authentication authentication) {
        super(null);
        this.authentication = authentication;
    }

    @Override
    public Object getCredentials() {
        return authentication.getCredentials();
    }

    @Override
    public Object getPrincipal() {
        return authentication.getUsername();
    }

    public Authentication getAuthentication() {
        return authentication;
    }

}
