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

import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.common.ServiceInstanceFactory;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.core.common.validation.MultipleAttributeValidationResultImpl;
import org.openengsb.domains.example.ExampleDomain;
import org.openengsb.domains.example.ExampleDomainEvents;
import org.openengsb.domains.example.connector.internal.LogService;

public class LogServiceInstanceFactory implements ServiceInstanceFactory<ExampleDomain, LogService> {

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
    public void updateServiceInstance(LogService instance, Map<String, String> attributes) {
        if (attributes.containsKey("outputMode")) {
            instance.setOutputMode(attributes.get("outputMode"));
        }
    }

    @Override
    public LogService createServiceInstance(String id, Map<String, String> attributes) {
        LogService instance = new LogService(id, domainEventInterface);
        updateServiceInstance(instance, attributes);
        return instance;
    }

    @Override
    public MultipleAttributeValidationResult updateValidation(LogService instance, Map<String, String> attributes) {
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }

    @Override
    public MultipleAttributeValidationResult createValidation(String id, Map<String, String> attributes) {
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }

    public void setDomainEventInterface(ExampleDomainEvents domainEventInterface) {
        this.domainEventInterface = domainEventInterface;
    }
}
