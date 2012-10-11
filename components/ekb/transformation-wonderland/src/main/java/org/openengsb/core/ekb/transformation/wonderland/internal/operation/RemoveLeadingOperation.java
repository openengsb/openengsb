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
import java.util.regex.Matcher;

import org.openengsb.core.ekb.api.transformation.TransformationConstants;
import org.openengsb.core.ekb.api.transformation.TransformationOperationException;

/**
 * The remove leading operation is a string operation. It takes the string from the source field and removes all
 * elements at the start which match a regular expression until a maximum length.
 */
public class RemoveLeadingOperation extends AbstractStandardTransformationOperation {
    private String regexStringParam = TransformationConstants.REGEX_PARAM;
    private String lengthParam = TransformationConstants.REMOVELEADING_LENGTH_PARAM;
    
    public RemoveLeadingOperation(String operationName) {
        super(operationName, RemoveLeadingOperation.class);
    }

    @Override
    public String getOperationDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("The ").append(getOperationName()).append(" is a string operation. It takes the string ");
        builder.append("from the source field and removes all elements at the start which match a regular ");
        builder.append("expression until a maximum length.");
        return builder.toString();
    }

    @Override
    public Integer getOperationInputCount() {
        return 1;
    }

    @Override
    public Map<String, String> getOperationParameterDescriptions() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(regexStringParam,
            "The regular expression which is used to recognise which elements should be removed.");
        parameters.put(lengthParam, "The length parameter defines how long the removal will be done at maximum.");
        return parameters;
    }

    @Override
    public Object performOperation(List<Object> input, Map<String, String> parameters)
        throws TransformationOperationException {
        checkInputSize(input);
        
        String value = input.get(0).toString();
        Integer length = parseIntString(parameters.get(lengthParam), false, 0);
        Matcher matcher = generateMatcher(parameters.get(regexStringParam), value);
        return performRemoveLeading(value, length, matcher);
    }
    
    /**
     * Perform the remove leading operation. Returns the original string if the matcher does not match with the string
     */
    private String performRemoveLeading(String source, Integer length, Matcher matcher) {
        if (length != null && length != 0) {
            matcher.region(0, length);
        }
        if (matcher.find()) {
            String matched = matcher.group();
            return source.substring(matched.length());
        }
        return source;
    }
}
