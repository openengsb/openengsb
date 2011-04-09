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
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.common.NonValidatingServiceInstanceFactory;
import org.openengsb.domain.example.ExampleDomainEvents;

public class LogServiceInstanceFactory extends NonValidatingServiceInstanceFactory {

    private ExampleDomainEvents domainEventInterface;

    @Override
    public ServiceDescriptor getDescriptor(Builder builder) {
        builder.name("log.name").description("log.description");
        builder.attribute(builder.newAttribute().id("prefix").name("log.prefix.name")
            .description("log.outputMode.description").defaultValue("").build());
        builder.attribute(builder.newAttribute().id("outputMode").name("log.outputMode.name")
            .description("log.outputMode.description").defaultValue("log.outputMode.info")
            .option("log.outputMode.debug", "DEBUG").option("log.outputMode.info", "INFO")
            .option("log.outputMode.warn", "WARN").option("log.outputMode.error", "ERROR").required().build());
        builder.attribute(builder.newAttribute().id("flush").name("log.flush.name")
            .description("log.flush.description").defaultValue("false").asBoolean().build());
        return builder.build();
    }

    @Override
    public void updateServiceInstance(Domain instance, Map<String, String> attributes) {
        LogService internalInstance = (LogService) instance;
        if (attributes.containsKey("outputMode")) {
            internalInstance.setOutputMode(attributes.get("outputMode"));
        }
    }

    @Override
    public LogService createServiceInstance(String id, Map<String, String> attributes) {
        LogService instance = new LogService(id, domainEventInterface);
        updateServiceInstance(instance, attributes);
        return instance;
    }

    public void setDomainEventInterface(ExampleDomainEvents domainEventInterface) {
        this.domainEventInterface = domainEventInterface;
    }
}
