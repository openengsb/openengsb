/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package ${package}.internal;

import java.util.Collections;
import java.util.Map;

import org.openengsb.core.common.ServiceInstanceFactory;
import org.openengsb.core.common.descriptor.AttributeDefinition;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.core.common.validation.MultipleAttributeValidationResultImpl;
import ${package}.internal.${connectorName}ServiceImpl;

import ${domainPackage}.${domainInterface};

public class ${connectorName}ServiceInstanceFactory implements ServiceInstanceFactory<${domainInterface}, ${connectorName}ServiceImpl> {

    public ${connectorName}ServiceInstanceFactory() {
    }

    @Override
    public void updateServiceInstance(${connectorName}ServiceImpl instance, Map<String, String> attributes) {
        if (attributes.containsKey("attr")) {
            instance.setAttr(attributes.get("attr"));
        }
    }

    @Override
    public ${connectorName}ServiceImpl createServiceInstance(String id, Map<String, String> attributes) {
        ${connectorName}ServiceImpl service = new ${connectorName}ServiceImpl();
        updateServiceInstance(service, attributes);
        return service;
    }

    @Override
    public ServiceDescriptor getDescriptor(ServiceDescriptor.Builder builder) {
        builder.name("service.name").description("service.description");
        builder.attribute(builder.newAttribute().id("attr").name("service.attr.name").description("service.attr.description").build());
        return builder.build();
    }
    
    @Override
    public MultipleAttributeValidationResult updateValidation(${connectorName}ServiceImpl instance,
            Map<String, String> attributes) {
        // TODO Auto-generated method stub
        Map<String, String> emptyMap = Collections.emptyMap();
        return new MultipleAttributeValidationResultImpl(true, emptyMap);
    }

    @Override
    public MultipleAttributeValidationResult createValidation(String id, Map<String, String> attributes) {
        // TODO Auto-generated method stub
        Map<String, String> emptyMap = Collections.emptyMap();
        return new MultipleAttributeValidationResultImpl(true, emptyMap);
    }
}
