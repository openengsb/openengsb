package org.openengsb.core.security.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership.
 * The AASTI licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

public class ReflectionUtils {

    private static Logger LOGGER = LoggerFactory.getLogger(ReflectionUtils.class);

    public static List<Method> getAllMethodDeclarations(MethodInvocation invocation) {
        Class<?> clazz = invocation.getThis().getClass();
        String methodName = invocation.getMethod().getName();
        Class<?>[] args = invocation.getMethod().getParameterTypes();

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

}
