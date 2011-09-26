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

import com.google.common.collect.Maps;

public abstract class AbstractPermissionProvider implements PermissionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPermissionProvider.class);

    protected final Map<String, Class<? extends Permission>> supported;

    protected AbstractPermissionProvider(Class<? extends Permission> first, Class<? extends Permission>... classes) {
        Map<String, Class<? extends Permission>> map = Maps.newHashMap();
        map.put(first.getName(), first);
        for (Class<? extends Permission> p : classes) {
            map.put(p.getName(), p);
        }
        supported = Collections.unmodifiableMap(map);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends Permission> getPermissionClass(String className) {
        try {
            return (Class<? extends Permission>) this.getClass().getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            LOGGER.warn("permission class not found: ", e);
            return null;
        }
    }

    @Override
    public Collection<Class<? extends Permission>> getSupportedPermissionClasses() {
        return supported.values();
    }

}
