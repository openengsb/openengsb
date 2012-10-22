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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.common.transformations.TransformationUtils;
import org.openengsb.core.ekb.api.TransformationEngine;

public class TransformingConnectorHandler<ConnectorType> implements InvocationHandler {

    private TransformationEngine transformationEngine;

    private ConnectorType target;

    public TransformingConnectorHandler(TransformationEngine transformationEngine, ConnectorType target) {
        this.transformationEngine = transformationEngine;
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method targetMethod = TransformationUtils.findTargetMethod(method, target.getClass());
        Class<?>[] targetTypes = targetMethod.getParameterTypes();
        Object[] transformed = transformArguments(args, targetTypes);
        Object result = targetMethod.invoke(target, transformed);
        return transformObject(result, method.getReturnType());
    }

    private Object[] transformArguments(Object[] args, Class<?>[] targetTypes) {
        if(args == null){
            return null;
        }
        Object[] transformedArguments = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            transformedArguments[i] = transformObject(args[i], targetTypes[i]);
        }
        return transformedArguments;
    }

    private Object transformObject(Object arg, Class<?> targetType) {
        if(arg == null){
            return null;
        }
        if (!(isModel(arg) && isModel(targetType))) {
            return arg;
        }
        ModelDescription sourceModel = TransformationUtils.retrieveModelDescriptionOf(arg.getClass());
        ModelDescription targetModel = TransformationUtils.retrieveModelDescriptionOf(targetType);
        return transformationEngine.performTransformation(sourceModel, targetModel, arg);
    }

    private boolean isModel(Class<?> targetType) {
        return OpenEngSBModel.class.isAssignableFrom(targetType);
    }

    private boolean isModel(Object arg) {
        return OpenEngSBModel.class.isInstance(arg);
    }

}
