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

/**
 * The TransformationOperationLoader loads transformation operations from the OSGi environment.
 */
public interface TransformationOperationLoader {

    /**
     * Loads the transformation operation with the given name from the OSGi environment. If there is no such operation,
     * a TransformationOperationException is thrown.
     */
    TransformationOperation loadTransformationOperationByName(String operationName)
        throws TransformationOperationException;

    /**
     * Returns a list of all currently available transformation operations.
     */
    List<TransformationOperation> loadActiveTransformationOperations();
}
