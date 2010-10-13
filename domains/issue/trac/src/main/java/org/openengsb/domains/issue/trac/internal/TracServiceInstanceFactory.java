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

package org.openengsb.domains.issue.trac.internal;

import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.common.ServiceInstanceFactory;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.core.common.validation.MultipleAttributeValidationResultImpl;
import org.openengsb.domains.issue.IssueDomain;
import org.openengsb.domains.issue.trac.internal.models.TicketHandlerFactory;

public class TracServiceInstanceFactory implements ServiceInstanceFactory<IssueDomain, TracConnector> {

    @Override
    public ServiceDescriptor getDescriptor(ServiceDescriptor.Builder builder) {
        builder.name("trac.name").description("trac.description");

        builder
            .attribute(buildAttribute(builder, "username", "username.outputMode", "username.outputMode.description"))
            .attribute(builder.newAttribute().id("userPassword").name("userPassword.outputMode")
                .description("userPassword.outputMode.description").defaultValue("").asPassword().build())
            .attribute(builder.newAttribute().id("serverUrl").name("serverUrl.outputMode")
                .description("serverUrl.outputMode.description").defaultValue("").required().build());

        return builder.build();
    }

    private AttributeDefinition buildAttribute(ServiceDescriptor.Builder builder, String id, String nameId,
                                               String descriptionId) {
        return builder.newAttribute().id(id).name(nameId).description(descriptionId).defaultValue("").required()
            .build();

    }

    @Override
    public void updateServiceInstance(TracConnector instance, Map<String, String> attributes) {
        TicketHandlerFactory ticketFactory = instance.getTicketHandlerFactory();
        updateTicketHandlerFactory(attributes, ticketFactory);
    }

    @Override
    public MultipleAttributeValidationResult updateValidation(TracConnector instance, Map<String, String> attributes) {
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }

    @Override
    public TracConnector createServiceInstance(String id, Map<String, String> attributes) {
        TicketHandlerFactory ticketFactory = new TicketHandlerFactory();
        updateTicketHandlerFactory(attributes, ticketFactory);
        TracConnector tracConnector = new TracConnector(id, ticketFactory);

        updateServiceInstance(tracConnector, attributes);
        return tracConnector;
    }

    @Override
    public MultipleAttributeValidationResult createValidation(String id, Map<String, String> attributes) {
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }

    private void updateTicketHandlerFactory(Map<String, String> attributes, TicketHandlerFactory ticketFactory) {
        if (attributes.containsKey("serverUrl")) {
            ticketFactory.setServerUrl(attributes.get("serverUrl"));
        }
        if (attributes.containsKey("user")) {
            ticketFactory.setUsername(attributes.get("user"));
        }
        if (attributes.containsKey("password")) {
            ticketFactory.setUserPassword(attributes.get("password"));
        }
    }
}
