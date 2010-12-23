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

package org.openengsb.core.security;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openengsb.core.common.OpenEngSBService;
import org.openengsb.core.common.persistence.PersistenceManager;
import org.openengsb.core.common.persistence.PersistenceService;
import org.openengsb.core.common.security.model.ServiceAuthorizedList;
import org.openengsb.core.common.security.model.User;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public class AuthenticatedUserAccessDecisionVoter implements AccessDecisionVoter, BundleContextAware {

    private Log log = LogFactory.getLog(AuthenticatedUserAccessDecisionVoter.class);

    private PersistenceService persistence;
    private PersistenceManager persistenceManager;
    private BundleContext bundleContext;

    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        MethodInvocation invocation = (MethodInvocation) object;
        log.info(String.format("intercepted call: %s on Object %s of type %s", invocation.getMethod().getName(),
            invocation.getThis(), invocation.getThis().getClass()));
        OpenEngSBService service = (OpenEngSBService) invocation.getThis();
        String instanceId = service.getInstanceId();

        List<ServiceAuthorizedList> query = persistence.query(new ServiceAuthorizedList(instanceId));
        Collection<GrantedAuthority> allowedAuthorities = new HashSet<GrantedAuthority>();
        allowedAuthorities.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
        for (ServiceAuthorizedList l : query) {
            allowedAuthorities.addAll(l.getAuthorities());
        }
        if (allowedAuthorities.isEmpty()) {
            return ACCESS_DENIED;
        }

        User user = (User) authentication.getPrincipal();
        log.info(String.format("authenticated as %s", user.getUsername()));
        Collection<GrantedAuthority> userAuthorities = user.getAuthorities();
        if (log.isDebugEnabled()) {
            @SuppressWarnings("unchecked")
            Collection<GrantedAuthority> authorities = CollectionUtils.intersection(userAuthorities, allowedAuthorities);
            log.debug("Intersection of Sets: " + authorities);
        }
        if (!Collections.disjoint(allowedAuthorities, userAuthorities)) {
            return ACCESS_GRANTED;
        }
        return ACCESS_DENIED;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return MethodInvocation.class.isAssignableFrom(clazz);
    }

    @Override
    public boolean supports(ConfigAttribute attribute) {
        return false;
    }

    public void init() {
        persistence = persistenceManager.getPersistenceForBundle(bundleContext.getBundle());
    }

    public void setPersistence(PersistenceService persistence) {
        this.persistence = persistence;
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;

    }

}
