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
import org.openengsb.core.ekb.transformation.wonderland.internal.operation.LengthOperation;
import org.openengsb.core.ekb.transformation.wonderland.internal.operation.MapOperation;
import org.openengsb.core.ekb.transformation.wonderland.internal.operation.SplitOperation;
import org.openengsb.core.ekb.transformation.wonderland.internal.operation.SplitRegexOperation;
import org.openengsb.core.ekb.transformation.wonderland.internal.operation.SubStringOperation;
import org.openengsb.core.ekb.transformation.wonderland.internal.operation.ToLowerOperation;
import org.openengsb.core.ekb.transformation.wonderland.internal.operation.ToUpperOperation;
import org.openengsb.core.ekb.transformation.wonderland.internal.operation.TrimOperation;
import org.openengsb.core.ekb.transformation.wonderland.internal.operation.ValueOperation;

public class TestTransformationOperationLoader implements TransformationOperationLoader {

    private List<TransformationOperation> operations;
    
    public TestTransformationOperationLoader() {
        operations = new ArrayList<TransformationOperation>();
        operations.add(new ForwardOperation());
        operations.add(new ConcatOperation());
        operations.add(new SplitOperation());
        operations.add(new SplitRegexOperation());
        operations.add(new MapOperation());
        operations.add(new TrimOperation());
        operations.add(new ToLowerOperation());
        operations.add(new ToUpperOperation());
        operations.add(new SubStringOperation());
        operations.add(new ValueOperation());
        operations.add(new LengthOperation());
    }
    
    @Override
    public List<TransformationOperation> loadActiveTransformationOperations() {
        return operations;
    }

    @Override
    public TransformationOperation loadTransformationOperationByName(String operationName)
        throws TransformationOperationException {
        for (TransformationOperation operation : operations) {
            if (operation.getOperationName().equals(operationName)) {
                return operation;
            }
        }
        return null;
    }

}
