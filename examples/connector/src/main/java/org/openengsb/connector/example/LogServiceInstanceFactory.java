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
import org.openengsb.core.common.AbstractConnectorInstanceFactory;
import org.openengsb.domain.example.ExampleDomainEvents;

public class LogServiceInstanceFactory extends AbstractConnectorInstanceFactory<LogService> {

    private ExampleDomainEvents domainEventInterface;

    @Override
    public Domain createNewInstance(String id) {
        return new LogService(id, domainEventInterface);
    }

    @Override
    public void doApplyAttributes(LogService instance, Map<String, String> attributes) {
        if (attributes.containsKey("outputMode")) {
            instance.setOutputMode(attributes.get("outputMode"));
        }
        if (attributes.containsKey("prefix")) {
            instance.setPrefix(attributes.get("prefix"));
        }
    }

    public void setDomainEventInterface(ExampleDomainEvents domainEventInterface) {
        this.domainEventInterface = domainEventInterface;
    }
}
