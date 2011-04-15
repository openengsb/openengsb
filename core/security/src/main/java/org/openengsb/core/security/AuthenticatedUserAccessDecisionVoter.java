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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ClassUtils;
import org.openengsb.core.api.OpenEngSBService;
import org.openengsb.core.api.persistence.PersistenceManager;
import org.openengsb.core.api.persistence.PersistenceService;
import org.openengsb.core.api.security.AuthorizedRoles;
import org.openengsb.core.api.security.model.ServiceAuthorizedList;
import org.openengsb.core.api.security.model.User;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

public class AuthenticatedUserAccessDecisionVoter implements AccessDecisionVoter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticatedUserAccessDecisionVoter.class);

    private PersistenceService persistence;
    private PersistenceManager persistenceManager;
    private BundleContext bundleContext;

    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        MethodInvocation invocation = (MethodInvocation) object;
        LOGGER.info("intercepted call: {} on Object {} of type {}", new Object[] {invocation.getMethod().getName(),
            invocation.getThis(), invocation.getThis().getClass()});
        OpenEngSBService service = (OpenEngSBService) invocation.getThis();
        String instanceId = service.getInstanceId();

        List<ServiceAuthorizedList> query = persistence.query(new ServiceAuthorizedList(instanceId));
        Collection<GrantedAuthority> allowedAuthorities = new HashSet<GrantedAuthority>();
        allowedAuthorities.add(new GrantedAuthorityImpl("ROLE_ADMIN"));
        allowedAuthorities.addAll(retrieveAnnotations(invocation));

        for (ServiceAuthorizedList l : query) {
            allowedAuthorities.addAll(l.getAuthorities());
        }
        if (allowedAuthorities.isEmpty()) {
            return ACCESS_DENIED;
        }

        Object user = authentication.getPrincipal();
        Collection<GrantedAuthority> userAuthorities = getAuthorities(user);
        if (userAuthorities == null) {
            LOGGER.error("No authorities could be found");
            return ACCESS_DENIED;
        }
        if (LOGGER.isDebugEnabled()) {
            @SuppressWarnings("unchecked")
            Collection<GrantedAuthority> authorities =
                CollectionUtils.intersection(userAuthorities, allowedAuthorities);
            LOGGER.debug("Intersection of Sets: {}", authorities);
        }
        if (!Collections.disjoint(allowedAuthorities, userAuthorities)) {
            return ACCESS_GRANTED;
        }
        return ACCESS_DENIED;
    }

    private List<GrantedAuthority> retrieveAnnotations(MethodInvocation invocation) {
        List<GrantedAuthority> result = new ArrayList<GrantedAuthority>();
        addRolesFromMethodAnnotation(result, invocation.getMethod());
        String methodName = invocation.getMethod().getName();
        Class<?>[] arguments = invocation.getMethod().getParameterTypes();
        for (Class<?> interfaze : getAllInterfaces(invocation.getThis().getClass())) {
            try {
                Method method = interfaze.getMethod(methodName, arguments);
                addRolesFromMethodAnnotation(result, method);
            } catch (SecurityException e) {
                // This exception should not happen and points to a real problem somewhere
                LOGGER.error("error while looping through interfaces: ", e);
            } catch (NoSuchMethodException e) {
                // Well, this exception istn't really an error and should be logged at trace-level
                LOGGER.trace("error while looping through interfaces: ", e);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Class<?>> getAllInterfaces(Class<? extends Object> clazz) {
        return ClassUtils.getAllInterfaces(clazz);
    }

    private void addRolesFromMethodAnnotation(List<GrantedAuthority> result, Method method) {
        AuthorizedRoles annotation = method.getAnnotation(AuthorizedRoles.class);
        result.addAll(getRolesFromAnnotation(annotation));
    }

    private Set<GrantedAuthority> getRolesFromAnnotation(AuthorizedRoles annotation) {
        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        if (annotation != null) {
            for (String role : annotation.value()) {
                authorities.add(new GrantedAuthorityImpl(role));
            }
        }
        return authorities;
    }

    private Collection<GrantedAuthority> getAuthorities(Object user) {
        Collection<GrantedAuthority> userAuthorities = null;
        if (user instanceof User) {
            User castUser = (User) user;
            LOGGER.info("authenticated as {}", castUser.getUsername());
            userAuthorities = castUser.getAuthorities();
        } else if (user instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User castUser =
                (org.springframework.security.core.userdetails.User) user;
            LOGGER.info("authenticated as {}", castUser.getUsername());
            userAuthorities = castUser.getAuthorities();
        }
        return userAuthorities;
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

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;

    }

}
