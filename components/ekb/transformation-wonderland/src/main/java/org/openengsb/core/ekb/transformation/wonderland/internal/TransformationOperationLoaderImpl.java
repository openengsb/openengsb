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

package org.openengsb.core.ekb.transformation.wonderland.internal;

import java.util.List;

import org.openengsb.core.api.OsgiUtilsService;
import org.openengsb.core.ekb.api.transformation.TransformationOperation;
import org.openengsb.core.ekb.api.transformation.TransformationOperationException;
import org.openengsb.core.ekb.api.transformation.TransformationOperationLoader;
import org.openengsb.core.util.DefaultOsgiUtilsService;
import org.openengsb.core.util.FilterUtils;
import org.osgi.framework.BundleContext;

/**
 * Standard implementation of the transformation operation loader.
 */
public class TransformationOperationLoaderImpl implements TransformationOperationLoader {
    private OsgiUtilsService service;

    public TransformationOperationLoaderImpl(BundleContext context) {
        service = new DefaultOsgiUtilsService(context);
    }

    @Override
    public List<TransformationOperation> loadActiveTransformationOperations() {
        return service.listServices(TransformationOperation.class);
    }

    @Override
    public TransformationOperation loadTransformationOperationByName(String operationName)
        throws TransformationOperationException {
        return (TransformationOperation) service.getService(FilterUtils.makeFilter(TransformationOperation.class,
            String.format("transformation.operation=%s", operationName)));
    }

}
