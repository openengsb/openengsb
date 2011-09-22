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

package org.openengsb.connector.serviceacl.internal;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang.ClassUtils;
import org.openengsb.core.api.AliveState;
import org.openengsb.core.api.security.SecurityAttribute;
import org.openengsb.core.api.security.SecurityAttributeManager;
import org.openengsb.core.api.security.SecurityAttributes;
import org.openengsb.core.api.security.UserDataManager;
import org.openengsb.core.api.security.UserNotFoundException;
import org.openengsb.core.api.security.model.SecurityAttributeEntry;
import org.openengsb.core.common.AbstractOpenEngSBConnectorService;
import org.openengsb.domain.authorization.AuthorizationDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;

public class ServiceAclServiceImpl extends AbstractOpenEngSBConnectorService implements
        AuthorizationDomain {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceAclServiceImpl.class);

    private UserDataManager userManager;

    public ServiceAclServiceImpl() {
    }

    public ServiceAclServiceImpl(UserDataManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public AliveState getAliveState() {
        return AliveState.ONLINE;
    }

    @Override
    public Access checkAccess(String user, Object object) {
        if (!(object instanceof MethodInvocation)) {
            return Access.ABSTAINED;
        }
        Collection<ServicePermission> permissions;
        try {
            permissions = userManager.getAllUserPermissions(user, ServicePermission.class);
        } catch (UserNotFoundException e) {
            LOGGER.warn("user not found in acl-connector", e);
            return Access.ABSTAINED;
        }
        if (hasServiceTypeAccess(permissions, (MethodInvocation) object)) {
            return Access.GRANTED;
        }
        return Access.ABSTAINED;
    }

    private boolean hasServiceTypeAccess(Collection<ServicePermission> permissions, final MethodInvocation object) {
        Class<? extends Object> serviceClass = object.getThis().getClass();
        final Collection<String> typeNames = getTypeNamesForClass(serviceClass);
        final Collection<String> operationNames = getOperationNamesForMethod(object.getMethod());
        final Collection<String> instanceNames = getServiceInstanceNames(object.getThis());
        return Iterators.any(permissions.iterator(), new Predicate<ServicePermission>() {
            @Override
            public boolean apply(ServicePermission input) {
                if (!validatePermission(input)) {
                    LOGGER.error("invalid permission detected: {} - {}", input, input.describe());
                    return false;
                }
                String type = input.getType();
                if (type != null && !typeNames.contains(type)) {
                    return false;
                }
                String instanceId = input.getInstance();
                if (instanceId != null && !instanceNames.contains(instanceId)) {
                    return false;
                }
                if (input.getOperation() == null) {
                    return true;
                }
                return operationNames.contains(input.getOperation());
            }

            private boolean validatePermission(ServicePermission input) {
                if (input.getType() == null && input.getInstance() == null) {
                    return false;
                }
                return true;
            }
        });
    }

    private Collection<String> getServiceInstanceNames(Object this1) {
        Collection<SecurityAttributeEntry> attribute = SecurityAttributeManager.getAttribute(this1);
        if (attribute == null) {
            return Collections.emptySet();
        }
        Collection<SecurityAttributeEntry> filtered =
            Collections2.filter(attribute, new Predicate<SecurityAttributeEntry>() {
                @Override
                public boolean apply(SecurityAttributeEntry input) {
                    return input.getKey().equals("name");
                }
            });
        return Collections2.transform(filtered, new Function<SecurityAttributeEntry, String>() {
            @Override
            public String apply(SecurityAttributeEntry input) {
                return input.getValue();
            }
        });
    }

    private Collection<String> getOperationNamesForMethod(Method method) {
        Collection<String> result = Sets.newHashSet();
        result.add(method.getName());
        @SuppressWarnings("unchecked")
        List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(method.getDeclaringClass());
        for (Class<?> interfaze : allInterfaces) {
            Method method2;
            try {
                method2 = interfaze.getMethod(method.getName(), method.getParameterTypes());

            } catch (NoSuchMethodException e) {
                continue;
            }
            for (SecurityAttribute a : findAllSecurityAttributeAnnotations(method2)) {
                if (a.key().equals("name")) {
                    result.add(a.value());
                }
            }
        }
        return result;
    }

    private Collection<String> getTypeNamesForClass(Class<? extends Object> serviceClass) {
        Set<String> result = Sets.newHashSet();
        @SuppressWarnings("unchecked")
        List<Class<?>> allInterfaces = ClassUtils.getAllInterfaces(serviceClass);
        allInterfaces.add(serviceClass);
        for (Class<?> clazz : allInterfaces) {
            result.add(clazz.getName());
            result.addAll(findAllSecurityNames(clazz));
        }
        return result;
    }

    private Collection<String> findAllSecurityNames(Class<?> clazz) {
        Set<String> result = Sets.newHashSet();
        SecurityAttribute[] annotations = findAllSecurityAttributeAnnotations(clazz);
        for (SecurityAttribute a : annotations) {
            if (a.key().equals("name")) {
                result.add(a.value());
            }
        }
        return result;
    }

    private SecurityAttribute[] findAllSecurityAttributeAnnotations(AnnotatedElement serviceClass) {
        SecurityAttribute annotation = serviceClass.getAnnotation(SecurityAttribute.class);
        if (annotation != null) {
            return new SecurityAttribute[]{ annotation };
        }
        SecurityAttributes annotations = serviceClass.getAnnotation(SecurityAttributes.class);
        if (annotations == null) {
            return new SecurityAttribute[0];
        }
        return annotations.value();
    }
}
