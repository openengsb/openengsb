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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.openengsb.core.ekb.api.transformation.TransformationOperation;
import org.openengsb.core.ekb.api.transformation.TransformationOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The split operation splits the given value of the source field based on the given regular expression split string
 * parameter and returns the value for the given index.
 */
public class SplitRegexOperation implements TransformationOperation {
    private static final Logger LOGGER = LoggerFactory.getLogger(SplitRegexOperation.class);
    private String operationName = "splitRegex";
    private String regexStringParam = "regexString";
    private String resultIndexParam = "resultIndex";

    @Override
    public String getOperationDescription() {
        StringBuilder builder = new StringBuilder();
        builder.append("The ").append(operationName).append(" operation splits the given value of the source field ");
        builder
            .append("based on the regular expression split string parameter and returns the value for the given index.");
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
            "The regular expression split string parameter is used for the split operation.");
        parameters.put(resultIndexParam, "The result index defines which result of the split operation is returned.");
        return parameters;
    }

    @Override
    public Object performOperation(List<Object> input, Map<String, String> parameters)
        throws TransformationOperationException {
        if (input.size() != getOperationInputCount()) {
            throw new TransformationOperationException(
                "The input values are not matching with the operation input count.");
        }
        Object value = null;
        String splitString = parameters.containsKey(regexStringParam) ? parameters.get(regexStringParam) : "";
        String indexString = parameters.containsKey(resultIndexParam) ? parameters.get(resultIndexParam) : "0";
        try {
            Integer index = Integer.parseInt(indexString);
            Pattern pattern = Pattern.compile(splitString);
            Matcher matcher = pattern.matcher(input.get(0).toString());
            for (int i = 0; i <= index; i++) {
                matcher.find();
            }
            value = matcher.group();
            if (value == null) {
                LOGGER.warn("No result for given regex and index. The empty string will be taken instead.");
                value = "";
            }
        } catch (PatternSyntaxException e) {
            throw new TransformationOperationException("Given regex string " + splitString
                    + " can't be compiled. Step will be skipped", e);
        } catch (NumberFormatException e) {
            throw new TransformationOperationException("The given result index parameter is not a number");
        } catch (IndexOutOfBoundsException e) {
            throw new TransformationOperationException(
                "The split result does not have that much results for the index parameter");
        }
        return value;
    }

    @Override
    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

}
