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

package org.openengsb.core.common;

import java.util.Map;

import org.openengsb.core.api.Domain;
import org.openengsb.core.api.ServiceInstanceFactory;
import org.openengsb.core.api.ServiceValidationFailedException;

public abstract class AbstractServiceInstanceFactory<ConnectorType extends Domain> implements ServiceInstanceFactory {

    @SuppressWarnings("unchecked")
    @Override
    public void applyAttributes(Domain instance, Map<String, String> attributes) {
        doApplyAttributes((ConnectorType) instance, attributes);
    }

    public abstract void doApplyAttributes(ConnectorType instance, Map<String, String> attributes);

    @SuppressWarnings("unchecked")
    @Override
    public void validate(Domain instance, Map<String, String> attributes) throws ServiceValidationFailedException {
        doValidate((ConnectorType) instance, attributes);
    }

    public void doValidate(ConnectorType instance, Map<String, String> attributes)
        throws ServiceValidationFailedException {
        // do nothing. override to add validation
    }

    @Override
    public void validate(Map<String, String> attributes) throws ServiceValidationFailedException {
        // do nothing. override to add validation
    }

}
