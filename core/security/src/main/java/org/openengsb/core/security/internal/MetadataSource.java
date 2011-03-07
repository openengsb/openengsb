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

package org.openengsb.core.security.internal;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.method.MethodSecurityMetadataSource;

public class MetadataSource implements MethodSecurityMetadataSource {

    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) {
        return Collections.emptyList();
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return Collections.emptyList();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return MethodInvocation.class.isAssignableFrom(clazz);
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Method method, Class<?> targetClass) {
        return Collections.emptyList();
    }

}
