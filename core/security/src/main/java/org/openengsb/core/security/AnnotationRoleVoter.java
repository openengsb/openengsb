package org.openengsb.core.security;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

public class AnnotationRoleVoter extends AbstractAccessDecisionVoter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationRoleVoter.class);

    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        Set<GrantedAuthority> retrieveAnnotations = retrieveAnnotations((MethodInvocation) object);
        Collection<GrantedAuthority> userAuthorities = ((UserDetails) authentication.getPrincipal()).getAuthorities();
        @SuppressWarnings("unchecked")
        Collection<GrantedAuthority> intersection = CollectionUtils.intersection(retrieveAnnotations, userAuthorities);
        if (intersection.isEmpty()) {
            return ACCESS_ABSTAIN;
        }
        return ACCESS_GRANTED;
    }

    private Set<GrantedAuthority> retrieveAnnotations(MethodInvocation invocation) {
        LOGGER.trace("deciding with annotations: {}", invocation);
        Set<GrantedAuthority> rolesFromMethodAnnotation = new HashSet<GrantedAuthority>();

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

    private Set<GrantedAuthority> getRolesFromMethodAnnotation(Method method) {
        AuthorizedRoles annotation = method.getAnnotation(AuthorizedRoles.class);
        if (annotation == null) {
            return Sets.newHashSet();
        }
        Collection<GrantedAuthority> authorities =
            Collections2.transform(getRolesFromAnnotation(annotation), new Function<String, GrantedAuthority>() {
                @Override
                public GrantedAuthority apply(String input) {
                    return new GrantedAuthorityImpl(input);
                }
            });
        Set<GrantedAuthority> result = new HashSet<GrantedAuthority>(authorities.size());
        result.addAll(authorities);
        return result;
    }

    private Set<String> getRolesFromAnnotation(AuthorizedRoles annotation) {
        Set<String> authorities = new HashSet<String>();
        CollectionUtils.addAll(authorities, annotation.value());
        LOGGER.debug("annotation-value: " + authorities);
        return authorities;
    }
}
