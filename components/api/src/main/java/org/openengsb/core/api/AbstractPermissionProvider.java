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
package org.openengsb.core.api;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.openengsb.core.api.security.PermissionProvider;
import org.openengsb.core.api.security.model.Permission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Helps to provide a Simple PermissionProvider.
 *
 * Just derive this class and add all permission-classes to the super-call in the default-constructor
 */
public abstract class AbstractPermissionProvider implements PermissionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPermissionProvider.class);

    protected final Map<String, Class<? extends Permission>> supported;

    protected AbstractPermissionProvider(Class<?>... classes) {
        Map<String, Class<? extends Permission>> map = Maps.newHashMap();
        for (Class<?> clazz : classes) {
            Preconditions.checkArgument(Permission.class.isAssignableFrom(clazz),
                "Permissions must implement the Permission-interface");
            @SuppressWarnings("unchecked")
            Class<? extends Permission> permissionClass = (Class<? extends Permission>) clazz;
            map.put(clazz.getName(), permissionClass);
        }
        supported = Collections.unmodifiableMap(map);
    }

    @Override
    public Class<? extends Permission> getPermissionClass(String className) throws ClassNotFoundException {
        if (supported.containsKey(className)) {
            return supported.get(className);
        }
        LOGGER.warn("permission class not found: {}", className);
        throw new ClassNotFoundException("PermissionType " + className + " not found in this provider");
    }

    @Override
    public Collection<Class<? extends Permission>> getSupportedPermissionClasses() {
        return supported.values();
    }

}
