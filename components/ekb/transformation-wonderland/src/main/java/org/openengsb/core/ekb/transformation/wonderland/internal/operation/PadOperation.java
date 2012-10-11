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

import com.google.common.base.Strings;

/**
 * The pad operation is a string operation which takes the string of the source, pad it based on the parameters and
 * returns the result.
 */
public class PadOperation extends AbstractStandardTransformationOperation {
    private String lengthParam = TransformationConstants.PAD_LENGTH_PARAM;
    private String charParam = TransformationConstants.PAD_CHARACTER_PARAM;
    private String directionParam = TransformationConstants.PAD_DIRECTION_PARAM;

    public PadOperation(String operationName) {
        super(operationName, PadOperation.class);
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
        checkInputSize(input);

        String value = input.get(0).toString();
        Integer length = parseIntString(parameters.get(lengthParam), true, 0);
        Character padChar = getPadCharacter(getParameterOrException(parameters, charParam));
        String directionString = getDirectionString(parameters.get(directionParam));

        return performPadOperation(value, length, padChar, directionString);
    }

    /**
     * Returns the character which is used for the padding. If the character string is longer than one char, the first
     * char will be used.
     */
    private Character getPadCharacter(String characterString) {
        if (characterString.length() > 0) {
            getLogger().debug("The given character string is longer than one element. The first character is used.");
        }
        return characterString.charAt(0);
    }

    /**
     * Returns the direction in which the padding will be done. If the direction string is null or invalid, 'Start' will
     * be taken instead.
     */
    private String getDirectionString(String direction) {
        if (direction == null || !(direction.equals("Start") || direction.equals("End"))) {
            getLogger().debug("Unrecognized direction string. The standard value 'Start' will be used.");
            return "Start";
        }
        return direction;
    }

    /**
     * Perform the pad operation itself and returns the result
     */
    private String performPadOperation(String source, Integer length, Character padChar, String direction) {
        if (direction.equals("Start")) {
            return Strings.padStart(source, length, padChar);
        } else {
            return Strings.padEnd(source, length, padChar);
        }
    }
}
