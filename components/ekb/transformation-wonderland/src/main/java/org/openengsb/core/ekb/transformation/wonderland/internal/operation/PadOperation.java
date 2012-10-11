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

import org.openengsb.core.ekb.api.transformation.TransformationOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * The pad operation is a string operation which takes the string of the source, pad it based on the parameters and
 * returns the result.
 */
public class PadOperation extends AbstractStandardTransformationOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(PadOperation.class);
    private String lengthParam = "length";
    private String charParam = "char";
    private String directionParam = "direction";

    public PadOperation(String operationName) {
        super(operationName);
    }

    @Override
    public String getOperationDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("The ").append(getOperationName()).append(" operation which takes the string of the source, ");
        builder.append("pad it based on the parameters and returns the result.");
        return builder.toString();
    }

    @Override
    public Integer getOperationInputCount() {
        return 1;
    }

    @Override
    public Map<String, String> getOperationParameterDescriptions() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(lengthParam, "The length param defines how long the resulting string should be.");
        parameters.put(charParam, "Defines which character should be used to fill the missing characters.");
        parameters.put(directionParam, "Define if the padding should be done at the start of the string or "
                + "at the end. The value for padding at the start is 'Start' and for the end it is 'End'");
        return parameters;
    }

    @Override
    public Object performOperation(List<Object> input, Map<String, String> parameters)
        throws TransformationOperationException {
        if (input.size() != getOperationInputCount()) {
            throw new TransformationOperationException(
                "The input values are not matching with the operation input count.");
        }

        String value = input.get(0).toString();
        String lengthString = parameters.get(lengthParam);
        String characterString = parameters.get(charParam);
        String directionString = parameters.get(directionParam);
        Integer length = OperationUtils.parseIntString(lengthString, true, 0, LOGGER);
        if (characterString == null || characterString.isEmpty()) {
            String message = "The given character string for the pad is empty. Step will be skipped.";
            LOGGER.error(message);
            throw new TransformationOperationException(message);
        }
        char character = characterString.charAt(0);
        if (characterString.length() > 0) {
            LOGGER.debug("The given character string is longer than one element. The first character is used.");
        }
        if (directionString == null || !(directionString.equals("Start") || directionString.equals("End"))) {
            LOGGER.debug("Unrecognized direction string. The standard value 'Start' will be used.");
            directionString = "Start";
        }

        if (directionString.equals("Start")) {
            value = Strings.padStart(value, length, character);
        } else {
            value = Strings.padEnd(value, length, character);
        }
        return value;
    }
}
