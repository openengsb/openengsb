package org.openengsb.core.security;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ClassUtils;
import org.openengsb.core.api.security.AuthorizedRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

public class AnnotationRoleVoter extends AbstractAccessDecisionVoter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationRoleVoter.class);

    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        final Set<String> retrieveAnnotations = retrieveAnnotations((MethodInvocation) object);
        Collection<GrantedAuthority> userAuthorities = ((UserDetails) authentication.getPrincipal()).getAuthorities();

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

        String methodName = invocation.getMethod().getName();
        Class<?>[] arguments = invocation.getMethod().getParameterTypes();

        for (Method method : getAllDeclarations(invocation.getThis().getClass(), methodName, arguments)) {
            rolesFromMethodAnnotation.addAll(getRolesFromMethodAnnotation(method));
        }

        return rolesFromMethodAnnotation;
    }

    private static List<Method> getAllDeclarations(Class<?> clazz, String methodName, Class<?>[] args) {
        List<Method> result = new ArrayList<Method>();
        @SuppressWarnings("unchecked")
        List<Class<?>> sum = ListUtils.sum(ClassUtils.getAllSuperclasses(clazz), ClassUtils.getAllInterfaces(clazz));
        sum.add(0, clazz);
        sum.remove(Object.class);
        LOGGER.trace("searching for annotation in clazzes {}", sum);
        for (Class<?> c : sum) {
            Method method;
            try {
                method = c.getMethod(methodName, args);
                LOGGER.trace(method.toString());
            } catch (NoSuchMethodException e) {
                continue;
            }
            if (!result.contains(method)) {
                result.add(method);
            }
        }
        return result;
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
