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

package org.openengsb.core.security.internal;

import java.util.Arrays;
import java.util.Collection;

import org.openengsb.core.security.BundleAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public class SystemUserAuthenticationProvider implements AuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemUserAuthenticationProvider.class);

    private static final Collection<GrantedAuthority> AUTHORITIES = Arrays
        .asList(new GrantedAuthority[]{ new GrantedAuthorityImpl("ROLE_ADMIN"), });

    /*
     * For now, all bundles are allowed to do everything (using any API-key)
     */
    @Override
    public Authentication authenticate(Authentication authentication) {
        BundleAuthenticationToken token = (BundleAuthenticationToken) authentication;
        LOGGER.info("authenticating bundle {}", token.getPrincipal());
        LOGGER.info("apikey: {}", token.getCredentials());
        return new BundleAuthenticationToken(token, AUTHORITIES);
    }

    public boolean supports(Class<? extends Object> authentication) {
        return BundleAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
