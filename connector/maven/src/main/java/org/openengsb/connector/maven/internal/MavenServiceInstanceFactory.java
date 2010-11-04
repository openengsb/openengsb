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

package org.openengsb.connector.maven.internal;

import java.util.Collections;
import java.util.Map;

import org.openengsb.core.common.ServiceInstanceFactory;
import org.openengsb.core.common.descriptor.ServiceDescriptor;
import org.openengsb.core.common.validation.MultipleAttributeValidationResult;
import org.openengsb.core.common.validation.MultipleAttributeValidationResultImpl;
import org.openengsb.domain.test.TestDomain;

public class MavenServiceInstanceFactory implements ServiceInstanceFactory<TestDomain, MavenServiceImpl> {

    public MavenServiceInstanceFactory() {
    }

    @Override
    public void updateServiceInstance(MavenServiceImpl instance, Map<String, String> attributes) {
        if (attributes.containsKey("projectPath")) {
            instance.setProjectPath(attributes.get("projectPath"));
        }
    }

    @Override
    public MavenServiceImpl createServiceInstance(String id, Map<String, String> attributes) {
        MavenServiceImpl service = new MavenServiceImpl();
        updateServiceInstance(service, attributes);
        return service;
    }

    @Override
    public ServiceDescriptor getDescriptor(ServiceDescriptor.Builder builder) {
        builder.name("service.name").description("service.description");
        builder.attribute(builder.newAttribute().id("projectPath").name("service.projectPath.name")
            .description("service.projectPath.description").build());
        return builder.build();
    }

    @Override
    public MultipleAttributeValidationResult updateValidation(MavenServiceImpl instance,
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
