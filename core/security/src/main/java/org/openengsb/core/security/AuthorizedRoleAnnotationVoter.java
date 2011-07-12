package org.openengsb.core.security;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections.CollectionUtils;
import org.openengsb.core.api.security.AuthorizedRoles;
import org.openengsb.core.security.internal.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class AuthorizedRoleAnnotationVoter extends AbstractAccessDecisionVoter {

    public static final Logger LOGGER = LoggerFactory.getLogger(AuthorizedRoleAnnotationVoter.class);

    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        final Set<String> retrieveAnnotations = retrieveAnnotations((MethodInvocation) object);
        UserDetails authenticatedUser = getAuthenticatedUser(authentication);
        if (authenticatedUser == null) {
            return ACCESS_ABSTAIN;
        }
        Collection<GrantedAuthority> userAuthorities = authenticatedUser.getAuthorities();

        try {
            GrantedAuthority role = Iterables.find(userAuthorities, new Predicate<GrantedAuthority>() {
                @Override
                public boolean apply(GrantedAuthority input) {
                    return retrieveAnnotations.contains(input.getAuthority());
                }
            });
            LOGGER.info("granted access because method was annotated for access by Role " + role.getAuthority());
            return ACCESS_GRANTED;
        } catch (NoSuchElementException e) {
            return ACCESS_ABSTAIN;
        }
    }

    private Set<String> retrieveAnnotations(MethodInvocation invocation) {
        LOGGER.trace("deciding with annotations: {}", invocation);
        Set<String> rolesFromMethodAnnotation = new HashSet<String>();

        for (Method method : ReflectionUtils.getAllMethodDeclarations(invocation)) {
            rolesFromMethodAnnotation.addAll(getRolesFromMethodAnnotation(method));
        }

        return rolesFromMethodAnnotation;
    }

    private Set<String> getRolesFromMethodAnnotation(Method method) {
        AuthorizedRoles annotation = method.getAnnotation(AuthorizedRoles.class);
        if (annotation == null) {
            return Sets.newHashSet();
        }
        return getRolesFromAnnotation(annotation);
    }

    private Set<String> getRolesFromAnnotation(AuthorizedRoles annotation) {
        Set<String> authorities = new HashSet<String>();
        CollectionUtils.addAll(authorities, annotation.value());
        LOGGER.debug("annotation-value: " + authorities);
        return authorities;
    }

}
