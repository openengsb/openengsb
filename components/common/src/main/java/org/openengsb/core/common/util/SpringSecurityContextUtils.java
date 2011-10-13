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

import org.springframework.security.core.Authentication;

/**
 * Util-methods to help interact with spring-security
 */
public final class SpringSecurityContextUtils {

    /**
     * convert a OpenEngSB-authentication-token to a spring one so that it can be stored in a
     * {@link org.springframework.security.core.context.SecurityContext}
     */
    public static Authentication wrapToken(org.openengsb.core.api.security.model.Authentication authentication) {
        return new OpenEngSBAuthentication(authentication);
    }

    /**
     * extract the OpenEngSB-authentication-token from a spring-authentication token. If it cannot be extracted, null is
     * returned.
     */
    public static org.openengsb.core.api.security.model.Authentication unwrapToken(Authentication authentication) {
        if (authentication instanceof OpenEngSBAuthentication) {
            return ((OpenEngSBAuthentication) authentication).getAuthentication();
        }
        return null;
    }

    private SpringSecurityContextUtils() {
    }

}
