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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openengsb.core.ekb.api.transformation.TransformationConstants;
import org.openengsb.core.ekb.api.transformation.TransformationOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The substring operation creates a substring of the given string based on the given range parameters from and to.
 */
public class SubStringOperation extends AbstractStandardTransformationOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubStringOperation.class);
    private String fromParam = TransformationConstants.SUBSTRING_FROM_PARAM;
    private String toParam = TransformationConstants.SUBSTRING_TO_PARAM;
    
    public SubStringOperation(String operationName) {
        super(operationName);
    }

    @Override
    public String getOperationDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("The ").append(getOperationName()).append(" operation creates a substring of the given");
        builder.append(" string based on the given range parameters from and to.");
        return builder.toString();
    }

    @Override
    public Integer getOperationInputCount() {
        return 1;
    }

    @Override
    public Map<String, String> getOperationParameterDescriptions() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(fromParam, "The from parameter defines from which index the substring should start."
                + " If this parameter is not set, the default value is 0.");
        parameters.put(toParam, "The to parameter defines at which index the substring should end."
                + " If this parameter is not set, the default value is the size of the string.");
        return parameters;
    }

    @Override
    public Object performOperation(List<Object> input, Map<String, String> parameters)
        throws TransformationOperationException {
        if (input.size() != getOperationInputCount()) {
            throw new TransformationOperationException(
                "The input values are not matching with the operation input count.");
        }
        String source = input.get(0).toString();
        String fromString = parameters.containsKey(fromParam) ? parameters.get(fromParam) : "0";
        String toString = parameters.containsKey(toParam) ? parameters.get(toParam) : "" + source.length();
        if (!parameters.containsKey(fromParam)) {
            LOGGER.debug("The from parameter is not set, so the default value 0 is taken.");
        }
        if (!parameters.containsKey(toParam)) {
            LOGGER.debug("The from parameter is not set, so the default value {} is taken.", source.length());
        }

        Object value = null;
        try {
            Integer from = Integer.parseInt(fromString);
            Integer to = Integer.parseInt(toString);
            value = source.substring(from, to);
        } catch (NumberFormatException e) {
            throw new TransformationOperationException("The from and/or the to parameter is not a number");
        } catch (IndexOutOfBoundsException e) {
            throw new TransformationOperationException(
                "The from and/or the to parameter is not fitting because of the size of the source");
        }
        return value;
    }
}
