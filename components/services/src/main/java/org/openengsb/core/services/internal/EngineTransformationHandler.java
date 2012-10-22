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

import org.openengsb.core.api.model.ModelDescription;
import org.openengsb.core.api.model.OpenEngSBModel;
import org.openengsb.core.ekb.api.TransformationEngine;

public class EngineTransformationHandler extends TransformationHandler {

    protected TransformationEngine transformationEngine;

    public EngineTransformationHandler(TransformationEngine transformationEngine, Method targetMethod) {
        super(targetMethod);
        this.transformationEngine = transformationEngine;
    }

    protected Object[] transformArguments(Object[] args, Class<?>[] targetTypes) {
        if (args == null) {
            return null;
        }
        Object[] transformedArguments = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            transformedArguments[i] = transformObject(args[i], targetTypes[i]);
        }
        return transformedArguments;
    }

    protected Object transformObject(Object arg, Class<?> targetType) {
        if (arg == null) {
            return null;
        }
        if (!(isModel(arg) && isModel(targetType))) {
            return arg;
        }
        ModelDescription sourceModel = new ModelDescription(arg.getClass());
        ModelDescription targetModel = new ModelDescription(targetType);
        return transformationEngine.performTransformation(sourceModel, targetModel, arg);
    }


    protected boolean isModel(Class<?> targetType) {
        return OpenEngSBModel.class.isAssignableFrom(targetType);
    }

    protected boolean isModel(Object arg) {
        return OpenEngSBModel.class.isInstance(arg);
    }
}
