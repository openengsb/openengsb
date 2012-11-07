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

import java.util.Collections;
import java.util.Map;

import org.openengsb.core.api.Connector;
import org.openengsb.core.api.ConnectorInstanceFactory;

/**
 * Abstract baseclass that may help when implementing {@link ConnectorInstanceFactory}s
 *
 * It takes care of the type-conversion and implements validation-functions that do not do anything. If validation is
 * required these methods should be overridden.
 */
public abstract class AbstractConnectorInstanceFactory<ConnectorType extends Connector> implements
        ConnectorInstanceFactory {
    
    @SuppressWarnings("unchecked")
    @Override
    public Connector applyAttributes(Connector instance, Map<String, String> attributes) {
        return doApplyAttributes((ConnectorType) instance, attributes);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> getValidationErrors(Connector instance, Map<String, String> attributes) {
        return getValidationErrorsInternally((ConnectorType) instance, attributes);
    }

    public Map<String, String> getValidationErrorsInternally(ConnectorType instance, Map<String, String> attributes) {
        // do nothing. override to add validation
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getValidationErrors(Map<String, String> attributes) {
        // do nothing. override to add validation
        return Collections.emptyMap();
    }

    public ConnectorType doApplyAttributes(ConnectorType instance, Map<String, String> attributes) {
        return instance;
    }
}
