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

import org.openengsb.core.ekb.api.transformation.TransformationConstants;
import org.openengsb.core.ekb.api.transformation.TransformationOperationException;

/**
 * The instantiate operation is used to support other field types than only strings. It instantiates an object of the
 * given type with the given parameter for the construction.
 */
public class InstantiateOperation extends AbstractStandardTransformationOperation {
    private String typeParam = TransformationConstants.INSTANTIATE_TARGETTYPE_PARAM;
    private String initFuncParam = TransformationConstants.INSTANTIATE_INITMETHOD_PARAM;

    public InstantiateOperation(String operationName) {
        super(operationName, InstantiateOperation.class);
    }

    @Override
    public String getOperationDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("The ").append(getOperationName()).append(" operation is used to support other ");
        builder.append("field types than only strings. It instantiates an object of the given type with ");
        builder.append("the given parameter for the construction.");
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
        checkInputSize(input);
        String targetType = getParameterOrException(parameters, typeParam);
        String initMethodName = getParameterOrDefault(parameters, initFuncParam, null);
        return tryInitiatingObject(targetType, initMethodName, input.get(0));
    }

    /**
     * Try to perform the actual initiating of the target class object. Returns the target class object or throws a
     * TransformationOperationException if something went wrong.
     */
    private Object tryInitiatingObject(String targetType, String initMethodName, Object fieldObject)
        throws TransformationOperationException {
        Class<?> targetClass = loadClassByName(targetType);
        try {
            if (initMethodName == null) {
                return initiateByConstructor(targetClass, fieldObject);
            } else {
                return initiateByMethodName(targetClass, initMethodName, fieldObject);
            }
        } catch (Exception e) {
            String message = "Unable to create the desired object. The instantiate operation will be ignored.";
            message = String.format(message, targetType);
            getLogger().error(message);
            throw new TransformationOperationException(message, e);
        }
    }

    /**
     * Tries to initiate an object of the target class through the given init method name with the given object as
     * parameter.
     */
    private Object initiateByMethodName(Class<?> targetClass, String initMethodName, Object object) throws Exception {
        Method method = targetClass.getMethod(initMethodName, object.getClass());
        if (Modifier.isStatic(method.getModifiers())) {
            return method.invoke(null, object);
        } else {
            return method.invoke(targetClass.newInstance(), object);
        }
    }

    /**
     * Tries to initiate an object of the target class through a constructor with the given object as parameter.
     */
    private Object initiateByConstructor(Class<?> targetClass, Object object) throws Exception {
        Constructor<?> constr = targetClass.getConstructor(object.getClass());
        return constr.newInstance(object);
    }

    /**
     * Tries to load the class with the given name. Throws a TransformationOperationException if this is not possible.
     */
    private Class<?> loadClassByName(String className) throws TransformationOperationException {
        try {
            return this.getClass().getClassLoader().loadClass(className);
        } catch (Exception e) {
            String message = "The class %s can't be found. The instantiate operation will be ignored.";
            message = String.format(message, className);
            getLogger().error(message);
            throw new TransformationOperationException(message, e);
        }
    }
}
