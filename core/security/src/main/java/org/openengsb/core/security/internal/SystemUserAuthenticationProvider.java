/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.security.BundleAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public class SystemUserAuthenticationProvider implements AuthenticationProvider {

    private Log log = LogFactory.getLog(SystemUserAuthenticationProvider.class);

    private static final Collection<GrantedAuthority> AUTHORITIES = Arrays
        .asList(new GrantedAuthority[]{ new GrantedAuthorityImpl("ROLE_ADMIN"), });

    @Override
    public Authentication authenticate(Authentication authentication) {
        BundleAuthenticationToken token = (BundleAuthenticationToken) authentication;
        log.info("authenticating bundle " + token.getPrincipal());
        log.info("apikey: " + token.getCredentials());
        return new BundleAuthenticationToken(token, AUTHORITIES);
    }

    public boolean supports(Class<? extends Object> authentication) {
        return BundleAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
