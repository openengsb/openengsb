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
package org.openengsb.ui.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.request.component.IRequestableComponent;
import org.openengsb.core.api.security.AuthenticationContext;
import org.openengsb.core.api.security.SecurityAttributeProvider;
import org.openengsb.core.api.security.annotation.SecurityAttribute;
import org.openengsb.core.api.security.annotation.SecurityAttributes;
import org.openengsb.core.api.security.model.SecurityAttributeEntry;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.openengsb.domain.authorization.AuthorizationDomain.Access;
import org.openengsb.ui.api.UIAction;
import org.ops4j.pax.wicket.api.PaxWicketBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class DomainAuthorizationStrategy implements IAuthorizationStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(DomainAuthorizationStrategy.class);

    @PaxWicketBean(name = "authorizer")
    private AuthorizationDomain authorizer;

    @PaxWicketBean(name = "authenticationContext")
    private AuthenticationContext authenticationContext;

    @PaxWicketBean(name = "attributeProviders")
    private List<SecurityAttributeProvider> attributeProviders;

    @Override
    public boolean isActionAuthorized(Component arg0, Action arg1) {
        List<SecurityAttributeEntry> attributeList = Lists.newArrayList();
        if (hasSecurityAnnotation(arg0.getClass())) {
            attributeList.addAll(getSecurityAttributes(arg0.getClass()));
        }

        LOGGER.info(ArrayUtils.toString(attributeProviders.getClass().getInterfaces()));

        for (SecurityAttributeProvider p : attributeProviders) {
            Collection<SecurityAttributeEntry> runtimeAttributes = p.getAttribute(arg0);
            if (runtimeAttributes != null) {
                attributeList.addAll(runtimeAttributes);
            }
        }

        if (attributeList.isEmpty()) {
            return true;
        }

        String user = getAuthenticatedUser();
        if (user == null) {
            return false;
        }
        UIAction secureAction =
            new UIAction(attributeList, arg1.getName(), ImmutableMap.of("component", (Object) arg0));

        Access checkAccess = authorizer.checkAccess(user, secureAction);
        if (checkAccess != Access.GRANTED) {
            LOGGER.warn("User {} was denied action {} on component {}", new Object[]{ user, arg1.toString(),
                arg0.getClass().getName() });
        }
        return checkAccess == Access.GRANTED;
    }

    @Override
    public <T extends IRequestableComponent> boolean isInstantiationAuthorized(Class<T> componentClass) {
        if (!hasSecurityAnnotation(componentClass)) {
            return true;
        }

        String user = getAuthenticatedUser();
        if (user == null) {
            return false;
        }

        LOGGER.trace("security-attribute-annotation present on {}", componentClass);

        return authorizer.checkAccess(user, new UIAction(getSecurityAttributes(componentClass))) == Access.GRANTED;
    }

    private String getAuthenticatedUser() {
        Object principal = authenticationContext.getAuthenticatedPrincipal();
        if (principal == null) {
            return null;
        }
        return principal.toString();
    }

    private boolean hasSecurityAnnotation(Class<? extends IRequestableComponent> class1) {
        return class1.isAnnotationPresent(SecurityAttribute.class)
                || class1.isAnnotationPresent(SecurityAttributes.class);
    }

    private Collection<SecurityAttributeEntry> getSecurityAttributes(
            Class<? extends IRequestableComponent> componentClass) {
        SecurityAttribute annotation = componentClass.getAnnotation(SecurityAttribute.class);
        if (annotation != null) {
            return Arrays.asList(convertAnnotationToEntry(annotation));
        }
        SecurityAttributes annotation2 = componentClass.getAnnotation(SecurityAttributes.class);
        if (annotation2 != null) {
            Collection<SecurityAttributeEntry> result = Lists.newArrayList();
            for (SecurityAttribute a : annotation2.value()) {
                result.add(convertAnnotationToEntry(a));
            }
            return result;
        }
        return null;
    }

    private SecurityAttributeEntry convertAnnotationToEntry(SecurityAttribute annotation) {
        return new SecurityAttributeEntry(annotation.key(), annotation.value());
    }

    public void setAuthorizer(AuthorizationDomain authorizer) {
        this.authorizer = authorizer;
    }

}
