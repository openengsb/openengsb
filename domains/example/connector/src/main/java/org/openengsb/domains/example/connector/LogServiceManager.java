/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.domains.example.connector;

import org.openengsb.core.common.AbstractServiceManager;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.core.common.validation.MultipleAttributeValidationResultImpl;
import org.openengsb.domains.example.ExampleDomain;
import org.openengsb.domains.example.connector.internal.LogService;

import java.util.HashMap;
import java.util.Map;

public class LogServiceManager extends AbstractServiceManager<ExampleDomain, LogService> {

    public LogServiceManager() {
        super(new LogServiceInstanceFactory());
    }

    @Override
    public MultipleAttributeValidationResult updateWithValidation(String id, Map<String, String> attributes) {
        this.update(id, attributes);
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }
}
