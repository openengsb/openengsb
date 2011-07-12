package org.openengsb.core.security;

import java.lang.reflect.Method;
import java.util.Collection;

import org.aopalliance.intercept.MethodInvocation;
import org.openengsb.core.api.security.Public;
import org.openengsb.core.security.internal.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class PublicAnnotationVoter extends AbstractAccessDecisionVoter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublicAnnotationVoter.class);

    private static final Predicate<Method> PREDICATE = new Predicate<Method>() {
        @Override
        public boolean apply(Method input) {
            return input.isAnnotationPresent(Public.class);
        };
    };

    @Override
    public int vote(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {

        MethodInvocation invocation = (MethodInvocation) object;
        if (Iterables.any(ReflectionUtils.getAllMethodDeclarations(invocation), PREDICATE)) {
            LOGGER.trace("granting access for {} because it is annotated @Public", invocation);
            return ACCESS_GRANTED;
        }
        return ACCESS_ABSTAIN;
    }
}
