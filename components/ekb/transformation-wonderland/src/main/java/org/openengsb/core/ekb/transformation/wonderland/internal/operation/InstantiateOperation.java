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

package org.openengsb.core.ekb.transformation.wonderland.internal.operation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.ekb.api.transformation.TransformationOperation;
import org.openengsb.core.ekb.api.transformation.TransformationOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The instantiate operation is used to support other field types than only strings. It instantiates an object of the
 * given type with the given parameter for the construction.
 */
public class InstantiateOperation implements TransformationOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstantiateOperation.class);
    private String operationName = "instantiate";
    private String typeParam = "targetType";
    private String initFuncParam = "targetTypeInit";

    @Override
    public String getOperationDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("The ").append(operationName).append(" operation is used to support other field types than ");
        builder.append("only strings. It instantiates an object of the given type with the given parameter ");
        builder.append("for the construction.");
        return builder.toString();
    }

    @Override
    public Integer getOperationInputCount() {
        return 1;
    }

    @Override
    public Map<String, String> getOperationParameterDescriptions() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(typeParam, "The fully qualified name of the class which shall be instantiated.");
        params.put(initFuncParam, "Defines which (static) function should be used to instantiate "
            + "the object. The source field value will be taken as argument for this function. If"
            + " this parameter is not set, the constructor of the class is used with the source "
            + "field value as parameter");
        return params;
    }

    @Override
    public Object performOperation(List<Object> input, Map<String, String> parameters)
        throws TransformationOperationException {
        if (input.size() != getOperationInputCount()) {
            throw new TransformationOperationException(
                "The input values are not matching with the operation input count.");
        }
        
        Object sourceObject = input.get(0);
        String targetType = parameters.get(typeParam);
        String targetTypeInit = parameters.get(initFuncParam);
        Object targetObject = null;
        Class<?> targetClass = null;
        try {
            targetClass = this.getClass().getClassLoader().loadClass(targetType);
        } catch (Exception e) {
            String message = "The class %s can't be found. The instantiate operation will be ignored.";
            message = String.format(message, targetType);
            LOGGER.error(message);
            throw new TransformationOperationException(message, e);
        }
        try {
            if (targetTypeInit == null) {
                Constructor<?> constr = targetClass.getConstructor(sourceObject.getClass());
                targetObject = constr.newInstance(sourceObject);
            } else {
                Method method = targetClass.getMethod(targetTypeInit, sourceObject.getClass());
                if (Modifier.isStatic(method.getModifiers())) {
                    targetObject = method.invoke(null, sourceObject);
                } else {
                    targetObject = method.invoke(targetClass.newInstance(), sourceObject);
                }
            }
        } catch (Exception e) {
            String message = "Unable to create the desired object. The instantiate operation will be ignored.";
            message = String.format(message, targetType);
            LOGGER.error(message);
            throw new TransformationOperationException(message, e);
        }
        return targetObject;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    @Override
    public String getOperationName() {
        return operationName;
    }
}
