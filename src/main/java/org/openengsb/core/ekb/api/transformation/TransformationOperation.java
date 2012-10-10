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

package org.openengsb.core.ekb.api.transformation;

import java.util.List;
import java.util.Map;

/**
 * The TransformOperation interface defines the functions for operations which can be used for the wonderland
 * transformation description language. Every operation which want to be supported, need to be exported with this
 * interface in the OSGi environment.
 */
public interface TransformationOperation {

    /**
     * Performs the operation which is defined with this transformation operation on the elements in the input list. The
     * result of the operation is returned. Throws a TransformationOperationException if something went wrong in the operation.
     */
    public Object performOperation(List<Object> input, Map<String, String> parameters) throws TransformationOperationException;

    /**
     * Returns a description for the operation this transformation operation is implementing.
     */
    public String getOperationDescription();

    /**
     * Returns a map where the key is a parameter for this operation and the value is a description for this parameter.
     */
    public Map<String, String> getOperationParameterDescriptions();

    /**
     * Returns the number of input objects this operation takes. Returns -1 if the amount of input objects is 1 or
     * higher.
     */
    public Integer getOperationInputCount();
    
    /**
     * Returns the name of the operation
     * @return
     */
    public String getOperationName();
}
