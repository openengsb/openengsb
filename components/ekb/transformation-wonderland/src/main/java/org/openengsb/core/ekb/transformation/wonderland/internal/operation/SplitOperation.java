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
 * The split operation splits the given value of the source field based on the split string parameter and returns the
 * value for the given index.
 */
public class SplitOperation extends AbstractStandardTransformationOperation {
    private String splitStringParam = TransformationConstants.SPLIT_PARAM;
    private String resultIndexParam = TransformationConstants.INDEX_PARAM;

    public SplitOperation(String operationName) {
        super(operationName, SplitOperation.class);
    }

    @Override
    public String getOperationDescription() {
        return getOperationDescriptor().does("splits the given value of the source field based on the split ")
            .cnt("string parameter and returns the value for the given index.").toString();
    }

    @Override
    public Integer getOperationInputCount() {
        return 1;
    }

    @Override
    public Map<String, String> getOperationParameterDescriptions() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(splitStringParam, "The split string parameter is used for the split operation.");
        parameters.put(resultIndexParam, "The result index defines which result of the split operation is returned.");
        return parameters;
    }

    @Override
    public Object performOperation(List<Object> input, Map<String, String> parameters)
        throws TransformationOperationException {
        checkInputSize(input);
        String splitString = getParameterOrDefault(parameters, splitStringParam, "");
        String indexString = getParameterOrDefault(parameters, resultIndexParam, "0");
        return performSplitting(input.get(0).toString(), splitString, indexString);
    }

    /**
     * Performs the actual splitting operation. Throws a TransformationOperationException if the index string is not a
     * number or causes an IndexOutOfBoundsException.
     */
    private String performSplitting(String source, String splitString, String indexString)
        throws TransformationOperationException {
        try {
            Integer index = Integer.parseInt(indexString);
            return source.split(splitString)[index];
        } catch (NumberFormatException e) {
            throw new TransformationOperationException("The given result index parameter is not a number");
        } catch (IndexOutOfBoundsException e) {
            throw new TransformationOperationException(
                "The split result does not have that much results for the index parameter");
        }
    }
}
