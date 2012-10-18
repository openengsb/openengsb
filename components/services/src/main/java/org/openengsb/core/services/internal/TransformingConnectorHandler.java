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
import java.util.Arrays;
import java.util.List;

import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.ekb.api.TransformationEngine;
import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.wiring.BundleWiring;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class TransformingConnectorHandler<ConnectorType> implements InvocationHandler {

    private TransformationEngine transformationEngine;

    private ConnectorType target;

    public TransformingConnectorHandler(TransformationEngine transformationEngine, ConnectorType target) {
        this.transformationEngine = transformationEngine;
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method targetMethod = findTargetMethod(method);
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
        return transformationEngine.performTransformation(getModelDescription(arg.getClass()),
                getModelDescription(targetType), arg);
    }

    private ModelDescription getModelDescription(Class<?> type) {
        return new ModelDescription(type, getClassVersion(type));
    }

    private String getClassVersion(Class<?> type) {
        Bundle bundle = FrameworkUtil.getBundle(type);
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        List<BundleCapability> capabilities = wiring.getCapabilities(BundleRevision.PACKAGE_NAMESPACE);
        for (BundleCapability capability : capabilities) {
            if (capability.getAttributes().get(BundleRevision.PACKAGE_NAMESPACE).equals(type.getPackage().getName())) {
                return (String) capability.getAttributes().get(Constants.VERSION_ATTRIBUTE);
            }
        }
        // just fallback, this shouldn't happen
        return bundle.getVersion().toString();
    }

    private Method findTargetMethod(final Method sourceMethod) {
        return Iterables.find(Arrays.asList(target.getClass().getMethods()), new Predicate<Method>() {
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
