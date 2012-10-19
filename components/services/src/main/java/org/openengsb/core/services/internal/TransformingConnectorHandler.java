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
        Object[] transformed = null;
        if(args != null){
            transformed = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                transformed[i] = transformArgument(args[i], targetTypes[i]);
            }
        }
        Object result = targetMethod.invoke(target, transformed);
        if(result != null){
            return transformArgument(result, method.getReturnType());
        }
        return null;
    }

    private Object transformArgument(Object arg, Class<?> targetType) {
        if (!(OpenEngSBModel.class.isInstance(arg) && OpenEngSBModel.class.isAssignableFrom(targetType))) {
            return arg;
        }
        return transformationEngine.performTransformation(TransformationUtils.toModelDescription(arg.getClass()),
                TransformationUtils.toModelDescription(targetType), arg);
    }

}
