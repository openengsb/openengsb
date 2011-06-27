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

import java.util.Collection;

import org.aopalliance.intercept.MethodInvocation;
import org.openengsb.core.security.model.OpenEngSBGrantedAuthority;
import org.openengsb.core.security.model.Permission;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

public class OpenEngSBAccessDecisionVoter extends AbstractAccessDecisionVoter {

    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        UserDetails user = getAuthenticatedUser(authentication);
        if (user == null) {
            return ACCESS_ABSTAIN;
        }

        Collection<OpenEngSBGrantedAuthority> openengsbAuthorities = getRelevantAuthorities(user);
        if (openengsbAuthorities.isEmpty()) {
            return ACCESS_ABSTAIN;
        }
        MethodInvocation invocation = (MethodInvocation) object;
        for (OpenEngSBGrantedAuthority g : openengsbAuthorities) {
            Collection<Permission> permissions = g.getPermissions();
            for (Permission p : permissions) {
                if (p.permits(invocation.getThis(), invocation.getMethod(), invocation.getArguments())) {
                    return ACCESS_GRANTED;
                }
            }
        }
        return ACCESS_ABSTAIN;
    }

    private Collection<OpenEngSBGrantedAuthority> getRelevantAuthorities(UserDetails user) {
        Collection<GrantedAuthority> filteredAuthorities =
            Collections2.filter(user.getAuthorities(), Predicates.instanceOf(OpenEngSBGrantedAuthority.class));
        Collection<OpenEngSBGrantedAuthority> openengsbAuthorities =
            Collections2.transform(filteredAuthorities, new Function<GrantedAuthority, OpenEngSBGrantedAuthority>() {
                @Override
                public OpenEngSBGrantedAuthority apply(GrantedAuthority input) {
                    return (OpenEngSBGrantedAuthority) input;
                }
            });
        return openengsbAuthorities;
    }
}
