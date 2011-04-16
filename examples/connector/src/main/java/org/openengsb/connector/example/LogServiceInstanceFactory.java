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

package org.openengsb.connector.example;

import java.util.Map;

import org.openengsb.connector.example.internal.LogService;
import org.openengsb.core.api.Domain;
import org.openengsb.core.api.ServiceInstanceFactory;
import org.openengsb.core.api.ServiceValidationFailedException;
import org.openengsb.domain.example.ExampleDomainEvents;

public class LogServiceInstanceFactory implements ServiceInstanceFactory {

    private ExampleDomainEvents domainEventInterface;

    @Override
    public Domain createNewInstance(String id) {
        return new LogService(id, domainEventInterface);
    }

    public void setDomainEventInterface(ExampleDomainEvents domainEventInterface) {
        this.domainEventInterface = domainEventInterface;
    }

    @Override
    public void applyAttributes(Domain instance, Map<String, String> attributes) {
        LogService internalInstance = (LogService) instance;
        if (attributes.containsKey("outputMode")) {
            internalInstance.setOutputMode(attributes.get("outputMode"));
        }
        if (attributes.containsKey("prefix")) {
            internalInstance.setPrefix(attributes.get("prefix"));
        }
    }

    @Override
    public void validate(Domain instance, Map<String, String> attributes) throws ServiceValidationFailedException {
        // do nothing
    }

    @Override
    public void validate(Map<String, String> attributes) throws ServiceValidationFailedException {
        // do nothing
    }

}
