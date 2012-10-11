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
 * The split operation splits the given value of the source field based on the given regular expression split string
 * parameter and returns the value for the given index.
 */
public class SplitRegexOperation extends AbstractStandardTransformationOperation {
    private String regexStringParam = TransformationConstants.REGEX_PARAM;
    private String resultIndexParam = TransformationConstants.INDEX_PARAM;

    public SplitRegexOperation(String operationName) {
        super(operationName, SplitRegexOperation.class);
    }

    @Override
    public String getOperationDescription() {
        return theOperation().does("splits the given value of the source field based on the regular ")
            .cnt("expression split string parameter and returns the value for the given index.").toString();
    }

    @Override
    public Integer getOperationInputCount() {
        return 1;
    }

    @Override
    public Map<String, String> getOperationParameterDescriptions() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(regexStringParam,
            "The regular expression split string parameter is used for the split operation.");
        parameters.put(resultIndexParam, "The result index defines which result of the split operation is returned.");
        return parameters;
    }

    @Override
    public Object performOperation(List<Object> input, Map<String, String> parameters)
        throws TransformationOperationException {
        checkInputSize(input);

        String splitString = getParameterOrDefault(parameters, regexStringParam, "");
        String indexString = parameters.get(resultIndexParam);
        return performSplitting(input.get(0).toString(), splitString, indexString);
    }

    /**
     * Performs the actual splitting operation. Throws a TransformationOperationException if the index string is not a
     * number or causes an IndexOutOfBoundsException.
     */
    private String performSplitting(String source, String splitString, String indexString)
        throws TransformationOperationException {
        Integer index = parseIntString(indexString, false, 0);
        Matcher matcher = generateMatcher(splitString, source);
        for (int i = 0; i <= index; i++) {
            matcher.find();
        }
        String value = matcher.group();
        if (value == null) {
            getLogger().warn("No result for given regex and index. The empty string will be taken instead.");
            value = "";
        }
        return value;
    }
}
