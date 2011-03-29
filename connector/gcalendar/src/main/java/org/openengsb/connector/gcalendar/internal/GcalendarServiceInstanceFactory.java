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

package org.openengsb.connector.gcalendar.internal;

import java.util.HashMap;
import java.util.Map;

import org.openengsb.core.common.ServiceInstanceFactory;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.core.common.validation.MultipleAttributeValidationResultImpl;
import org.openengsb.domain.appointment.AppointmentDomain;

public class GcalendarServiceInstanceFactory
        implements ServiceInstanceFactory<AppointmentDomain, GcalendarServiceImpl> {

    public GcalendarServiceInstanceFactory() {
    }

    @Override
    public ServiceDescriptor getDescriptor(ServiceDescriptor.Builder builder) {
        builder.name("service.name").description("service.description");

        builder.attribute(
            builder.newAttribute().id("google.user").name("google.user.name").description("google.user.description")
                .build());
        builder.attribute(builder.newAttribute().id("google.password").name("google.password.name")
            .description("google.password.description").asPassword().build());

        return builder.build();
    }

    @Override
    public void updateServiceInstance(GcalendarServiceImpl instance, Map<String, String> attributes) {
        instance.setGoogleUser(attributes.get("google.user"));
        instance.setGooglePassword(attributes.get("google.password"));
    }

    @Override
    public MultipleAttributeValidationResult updateValidation(GcalendarServiceImpl instance,
            Map<String, String> attributes) {
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }

    @Override
    public GcalendarServiceImpl createServiceInstance(String id, Map<String, String> attributes) {
        GcalendarServiceImpl service = new GcalendarServiceImpl(id);
        updateServiceInstance(service, attributes);
        return service;
    }

    @Override
    public MultipleAttributeValidationResult createValidation(String id, Map<String, String> attributes) {
        return new MultipleAttributeValidationResultImpl(true, new HashMap<String, String>());
    }
}
