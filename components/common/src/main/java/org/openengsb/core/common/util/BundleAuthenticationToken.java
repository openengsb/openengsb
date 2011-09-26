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

package org.openengsb.core.common.util;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * used internally to authenticate external bundles as actors bundles may perform administrative tasks, and are trusted
 * to handle this securely. When passed to the AuthenticationManager, authentication takes place.
 * 
 * SystemUserAuthenticationProvider takes care of authenticating such a token.
 * 
 * @see org.openengsb.core.security.internal.SystemUserAuthenticationProvider
 */
public class BundleAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = -7398391691027175944L;

    private String principal;
    private String apiKey;

    public BundleAuthenticationToken(String bundle, String apiKey) {
        super(null);
        this.principal = bundle;
        this.apiKey = apiKey;
        super.setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public Object getCredentials() {
        return apiKey;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        throw new UnsupportedOperationException(
            "Cannot set authenticated, directly. Please use an AuthenticationProvider");
    }

}
