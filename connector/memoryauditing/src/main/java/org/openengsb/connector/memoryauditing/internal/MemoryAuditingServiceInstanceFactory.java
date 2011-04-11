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

package org.openengsb.connector.memoryauditing.internal;

import java.util.Map;

import org.openengsb.core.api.Domain;
import org.openengsb.core.api.descriptor.ServiceDescriptor;
import org.openengsb.core.api.descriptor.ServiceDescriptor.Builder;
import org.openengsb.core.common.NonValidatingServiceInstanceFactory;

public class MemoryAuditingServiceInstanceFactory extends NonValidatingServiceInstanceFactory {

    public MemoryAuditingServiceInstanceFactory() {
    }

    @Override
    public void updateServiceInstance(Domain instance, Map<String, String> attributes) {
    }

    @Override
    public Domain createServiceInstance(String id, Map<String, String> attributes) {
        MemoryAuditingServiceImpl service = new MemoryAuditingServiceImpl();
        updateServiceInstance(service, attributes);
        return service;
    }

    @Override
    public ServiceDescriptor getDescriptor(Builder builder) {
        builder.name("service.name").description("service.description");
        builder.attribute(builder.newAttribute().id("attr").name("service.attr.name")
            .description("service.attr.description").build());
        return builder.build();
    }

}
