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

package org.openengsb.core.ekb.transformation.wonderland;

import java.util.ArrayList;
import java.util.List;

import org.openengsb.core.ekb.api.transformation.TransformationOperation;
import org.openengsb.core.ekb.api.transformation.TransformationOperationException;
import org.openengsb.core.ekb.api.transformation.TransformationOperationLoader;
import org.openengsb.core.ekb.transformation.wonderland.internal.operation.ConcatOperation;
import org.openengsb.core.ekb.transformation.wonderland.internal.operation.ForwardOperation;

public class TestTransformationOperationLoader implements TransformationOperationLoader {

    private List<TransformationOperation> operations;
    
    public TestTransformationOperationLoader() {
        operations = new ArrayList<TransformationOperation>();
        operations.add(new ForwardOperation());
        operations.add(new ConcatOperation());
    }
    
    @Override
    public List<TransformationOperation> loadActiveTransformationOperations() {
        return operations;
    }

    @Override
    public TransformationOperation loadTransformationOperationByName(String operationName)
        throws TransformationOperationException {
        if (operationName.equals("forward")) {
            return operations.get(0);
        } else if (operationName.equals("concat")) {
            return operations.get(1);
        }
        return null;
    }

}
