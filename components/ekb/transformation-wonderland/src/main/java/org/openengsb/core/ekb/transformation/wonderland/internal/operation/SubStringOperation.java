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

/**
 * The substring operation creates a substring of the given string based on the given range parameters from and to.
 */
public class SubStringOperation extends AbstractStandardTransformationOperation {
    private String fromParam = TransformationConstants.SUBSTRING_FROM_PARAM;
    private String toParam = TransformationConstants.SUBSTRING_TO_PARAM;

    public SubStringOperation(String operationName) {
        super(operationName, SubStringOperation.class);
    }

    @Override
    public String getOperationDescription() {
        return getOperationDescriptor().does("creates a substring of the given string based on the given ")
            .cnt("range parameters from and to.").toString();
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
        Integer from = getFromParameter(parameters);
        Integer to = getToParameter(parameters, source.length());

        checkBounds(source, from, to);

        return source.substring(from, to);
    }

    /**
     * Checks if the from and the to parameters are valid for the given source
     */
    private void checkBounds(String source, Integer from, Integer to) throws TransformationOperationException {
        Integer length = source.length();
        if (from > to) {
            throw new TransformationOperationException(
                "The from parameter is bigger than the to parameter");
        }
        if (from < 0 || from > length) {
            throw new TransformationOperationException(
                "The from parameter is not fitting to the size of the source");
        }
        if (to < 0 || to > length) {
            throw new TransformationOperationException(
                "The to parameter is not fitting to the size of the source");
        }
    }

    /**
     * Get the 'from' parameter from the parameters. If the parameter is not set 0 is taken instead.
     */
    private Integer getFromParameter(Map<String, String> parameters) throws TransformationOperationException {
        return getSubStringParameter(parameters, fromParam, 0);
    }

    /**
     * Get the 'to' parameter from the parameters. If the parameter is not set the defaultValue is taken instead.
     */
    private Integer getToParameter(Map<String, String> parameters, Integer defaultValue)
        throws TransformationOperationException {
        return getSubStringParameter(parameters, toParam, defaultValue);
    }

    /**
     * Returns the substring parameter with the given parameter or the given default value if the parameter name is not
     * set.
     */
    private Integer getSubStringParameter(Map<String, String> parameters, String parameterName, Integer defaultValue)
        throws TransformationOperationException {
        String parameter = parameters.get(parameterName);
        if (parameter == null) {
            getLogger().debug("The {} parameter is not set, so the default value {} is taken.", parameterName,
                defaultValue);
            return defaultValue;
        }
        try {
            return Integer.parseInt(parameter);
        } catch (NumberFormatException e) {
            throw new TransformationOperationException("The " + parameterName + " parameter is not a number");
        }
    }
}
