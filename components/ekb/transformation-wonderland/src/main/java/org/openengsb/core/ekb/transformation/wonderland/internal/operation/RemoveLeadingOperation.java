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

import org.openengsb.core.ekb.api.transformation.TransformationOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The remove leading operation is a string operation. It takes the string from the source field and removes all
 * elements at the start which match a regular expression until a maximum length.
 */
public class RemoveLeadingOperation extends AbstractStandardTransformationOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveLeadingOperation.class);
    private String regexStringParam = "regexString";
    private String lengthParam = "length";
    
    public RemoveLeadingOperation(String operationName) {
        super(operationName);
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
        if (input.size() != getOperationInputCount()) {
            throw new TransformationOperationException(
                "The input values are not matching with the operation input count.");
        }
        String value = input.get(0).toString();
        String regex = parameters.get(regexStringParam);
        String lengthString = parameters.get(lengthParam);
        Integer length = OperationUtils.parseIntString(lengthString, false, 0, LOGGER);
        Matcher matcher = OperationUtils.generateMatcher(regex, value, LOGGER);
        if (length != null && length != 0) {
            matcher.region(0, length);
        }
        if (matcher.find()) {
            String matched = matcher.group();
            value = value.substring(matched.length());
        }
        return value;
    }
}
