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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.openengsb.core.ekb.api.transformation.TransformationOperation;
import org.openengsb.core.ekb.api.transformation.TransformationOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The AbstractStandardTransformationOperation is the abstract class for every TransformationOperation provided by the
 * transformation wonderland bundle.
 */
public abstract class AbstractStandardTransformationOperation implements TransformationOperation {
    private Logger logger;
    private String operationName;

    public AbstractStandardTransformationOperation(String operationName, Class<?> clazz) {
        this.operationName = operationName;
        logger = LoggerFactory.getLogger(clazz);
    }

    @Override
    public String getOperationName() {
        return operationName;
    }

    /**
     * Returns the Logger instance for the actual transformation operation.
     */
    protected Logger getLogger() {
        return logger;
    }

    /**
     * Returns the pre defined DescriptionStringBuilder with the operation name of this operation. This object need to
     * be filled with the actual content.
     */
    protected DescriptionStringBuilder getOperationDescriptor() {
        return new DescriptionStringBuilder(operationName);
    }

    /**
     * Checks if the input size is matching with the operation defined input count. If not, a
     * TransformationOperationException is thrown.
     */
    protected void checkInputSize(List<Object> input) throws TransformationOperationException {
        int inputCount = getOperationInputCount();
        int inputSize = input.size();
        if (inputCount == -1 && input.size() < 1) {
            throw new TransformationOperationException("There must be at least one input value.");
        }
        if (inputCount != -1 && inputSize != inputCount) {
            throw new TransformationOperationException(
                "The input values are not matching with the operation input count.");
        }
    }

    /**
     * Get the parameter with the given parameter name from the parameter map. If the parameters does not contain such a
     * parameter, take the default value instead.
     */
    protected String getParameterOrDefault(Map<String, String> parameters, String paramName, String defaultValue)
        throws TransformationOperationException {
        return getParameter(parameters, paramName, false, defaultValue);
    }

    /**
     * Get the parameter with the given parameter name from the parameter map. If the parameters does not contain such a
     * parameter, the function throws a TransformationOperationException.
     */
    protected String getParameterOrException(Map<String, String> parameters, String paramName)
        throws TransformationOperationException {
        return getParameter(parameters, paramName, true, null);
    }

    /**
     * Get a parameter from the parameter map. If abortOnError is true, then a TransformationOperationException is
     * thrown in case that the asked parameter does not exists. If this value is false, the default value will be taken
     * instead of throwing an exception.
     */
    private String getParameter(Map<String, String> parameters, String paramName, boolean abortOnError,
            String defaultValue) throws TransformationOperationException {
        String value = parameters.get(paramName);
        if (value != null) {
            return value;
        }
        if (abortOnError) {
            String error = String.format("There is no parameter with the name %s present. The step will be ignored.",
                paramName);
            throw new TransformationOperationException(error);
        }
        logger.debug("There is no parameter with the name {} present. The value {} will be taken instead.", paramName,
            defaultValue);
        return defaultValue;
    }

    /**
     * Parses a string to an integer object. AbortOnError defines if the function should throw an exception if an error
     * occurs during the parsing. If it doesn't abort, the given default value is given back as result on error.
     */
    protected Integer parseIntString(String string, boolean abortOnError, int def)
        throws TransformationOperationException {
        Integer integer = def;
        if (string == null) {
            logger.debug("Given string is empty so the default value is taken.");
        }
        try {
            integer = Integer.parseInt(string);
        } catch (NumberFormatException e) {
            StringBuilder builder = new StringBuilder();
            builder.append("The string ").append(string).append(" is not a number. ");
            if (abortOnError) {
                builder.append("The step will be skipped.");
            } else {
                builder.append(def).append(" will be taken instead.");
            }

            logger.warn(builder.toString());
            if (abortOnError) {
                throw new TransformationOperationException(builder.toString());
            }
        }
        return integer;
    }

    /**
     * Generates a matcher for the given valueString with the given regular expression.
     */
    protected Matcher generateMatcher(String regex, String valueString)
        throws TransformationOperationException {
        if (regex == null) {
            throw new TransformationOperationException("No regex defined. The step will be skipped.");
        }
        try {
            Pattern pattern = Pattern.compile(regex);
            return pattern.matcher(valueString);
        } catch (PatternSyntaxException e) {
            String message =
                String.format("Given regex string %s can't be compiled. The step will be skipped.", regex);
            logger.warn(message);
            throw new TransformationOperationException(message);
        }
    }
}
