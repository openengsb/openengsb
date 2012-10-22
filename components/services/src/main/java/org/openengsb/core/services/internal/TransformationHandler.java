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
package org.openengsb.core.services.internal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.NoSuchElementException;

import org.openengsb.core.ekb.api.TransformationEngine;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public abstract class TransformationHandler {

    protected Method targetMethod;

    public static TransformationHandler newTransformationHandler(TransformationEngine transformationEngine,
            Method sourceMethod, Class<?> targetClass) {
        if (targetClass == null) {
            return new PassThroughTransformationHandler(sourceMethod);
        }
        Method targetMethod = findTargetMethod(sourceMethod, targetClass);
        return new EngineTransformationHandler(transformationEngine, targetMethod);
    }

    protected TransformationHandler(Method targetMethod) {
        this.targetMethod = targetMethod;
    }


    public Object[] transformArguments(Object[] args) {
        return transformArguments(args, targetMethod.getParameterTypes());
    }

    protected abstract Object[] transformArguments(Object[] args, Class<?>[] parameterTypes);

    public Object transformResult(Object result) {
        return transformObject(result, targetMethod.getReturnType());
    }

    protected abstract Object transformObject(Object result, Class<?> returnType);

    public Method getTargetMethod() {
        return targetMethod;
    }

    /**
     * tries to find a method that method in the class {@code target} with the same name and the same number of
     * arguments. It's assumed that the arguments can then be transformed.
     *
     * @throws java.util.NoSuchElementException
     *          if no matching method can be found
     */
    protected static Method findTargetMethod(final Method sourceMethod, Class<?> target) throws NoSuchElementException {
        return Iterables.find(Arrays.asList(target.getMethods()), new Predicate<Method>() {
            @Override
            public boolean apply(Method element) {
                if (!sourceMethod.getName().equals(element.getName())) {
                    return false;
                }
                if (sourceMethod.getParameterTypes().length != element.getParameterTypes().length) {
                    return false;
                }
                return true;
            }
        });
    }

}
