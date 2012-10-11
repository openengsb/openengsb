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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.ekb.api.transformation.TransformationConstants;
import org.openengsb.core.ekb.api.transformation.TransformationOperationException;

/**
 * The length operation can calculate the length of an element in the source field and return the amount as string.
 */
public class LengthOperation extends AbstractStandardTransformationOperation {
    private String lengthFuncParam = TransformationConstants.LENGTH_FUNCTION_PARAM;

    public LengthOperation(String operationName) {
        super(operationName, LengthOperation.class);
    }

    @Override
    public String getOperationDescription() {
        return theOperation().does("can calculate the length of an element in the source field ")
            .cnt("and return the amount as string.").toString();
    }

    @Override
    public Integer getOperationInputCount() {
        return 1;
    }

    @Override
    public Map<String, String> getOperationParameterDescriptions() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(lengthFuncParam, "Defines the function which should be used to calculate the length."
                + " 'length' is standard. E.g. In case of a list, put 'size' in this parameter.");
        return params;
    }

    @Override
    public Object performOperation(List<Object> input, Map<String, String> parameters)
        throws TransformationOperationException {
        checkInputSize(input);
        String function = getParameterOrDefault(parameters, lengthFuncParam, "length");
        return "" + getLengthOfObject(input.get(0), function);
    }

    /**
     * Returns the length of the given object got through the method of the given function name
     */
    private Object getLengthOfObject(Object object, String functionName) throws TransformationOperationException {
        try {
            Method method = object.getClass().getMethod(functionName);
            return method.invoke(object);
        } catch (NoSuchMethodException e) {
            StringBuilder builder = new StringBuilder();
            builder.append("The type of the given field for the length step doesn't support ");
            builder.append(functionName).append(" method. So 0 will be used as standard value.");
            throw new TransformationOperationException(builder.toString(), e);
        } catch (IllegalArgumentException e) {
            throw new TransformationOperationException("Can't get length of the source field", e);
        } catch (IllegalAccessException e) {
            throw new TransformationOperationException("Can't get length of the source field", e);
        } catch (InvocationTargetException e) {
            throw new TransformationOperationException("Can't get length of the source field", e);
        }
    }
}
