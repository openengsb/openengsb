/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.openengsb.domains.example.connector;

import java.util.Locale;
import java.util.Map;

import org.openengsb.core.config.ServiceInstanceFactory;
import org.openengsb.core.config.descriptor.AttributeDefinition;
import org.openengsb.core.config.descriptor.ServiceDescriptor;
import org.openengsb.core.config.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.config.util.BundleStrings;
import org.openengsb.domains.example.ExampleDomain;
import org.openengsb.domains.example.connector.internal.LogService;

public class LogServiceInstanceFactory implements ServiceInstanceFactory<ExampleDomain, LogService> {

    @Override
    public ServiceDescriptor getDescriptor(Builder builder, Locale locale, BundleStrings strings) {
        return ServiceDescriptor.builder()
            .id(LogService.class.getName())
            .implementsInterface(ExampleDomain.class.getName())
            .type(LogService.class)
            .name(strings.getString("log.name", locale))
            .description(strings.getString("log.description", locale))
            .attribute(AttributeDefinition.builder()
                    .id("prefix")
                    .name(strings.getString("log.prefix.name", locale))
                    .description(strings.getString("log.outputMode.description", locale))
                    .defaultValue("")
                    .build())
            .attribute(AttributeDefinition.builder()
                    .id("outputMode")
                    .name(strings.getString("log.outputMode.name", locale))
                    .description(strings.getString("log.outputMode.description", locale))
                    .defaultValue(strings.getString("log.outputMode.info"))
                    .option(strings.getString("log.outputMode.debug"), "DEBUG")
                    .option(strings.getString("log.outputMode.info"), "INFO")
                    .option(strings.getString("log.outputMode.warn"), "WARN")
                    .option(strings.getString("log.outputMode.error"), "ERROR")
                    .required()
                    .build())
            .attribute(AttributeDefinition.builder()
                    .id("flush")
                    .name(strings.getString("log.flush.name", locale))
                    .description(strings.getString("log.flush.description", locale))
                    .defaultValue("false")
                    .asBoolean()
                    .build())
            .build();
    }

    @Override
    public void updateServiceInstance(LogService instance, Map<String, String> attributes) {
        if (attributes.containsKey("outputMode")) {
            instance.setOutputMode(attributes.get("outputMode"));
        }
    }

    @Override
    public LogService createServiceInstance(String id, Map<String, String> attributes) {
        LogService instance = new LogService(id);
        updateServiceInstance(instance, attributes);
        return instance;
    }
}
